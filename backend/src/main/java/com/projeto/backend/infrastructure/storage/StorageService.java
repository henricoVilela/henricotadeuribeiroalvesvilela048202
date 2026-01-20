package com.projeto.backend.infrastructure.storage;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name:album-covers}")
    private String bucketName;

    @Value("${minio.presigned-url-expiration:30}")
    private int presignedUrlExpiration;
    
    /**
     * Faz upload de um arquivo para o MinIO.
     *
     * @param file Arquivo a ser enviado
     * @param folder Pasta dentro do bucket (ex: "artista-1/album-1")
     * @return StorageResult com informações do arquivo salvo
     */
    public StorageResult upload(MultipartFile file, String folder) {
        try {
            validateFile(file);

            // Gera nome único para o arquivo
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            String objectKey = folder + "/" + uniqueFilename;

            // Calcula hash MD5
            String md5Hash = calculateMD5(file.getInputStream());

            // Faz upload
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            logger.info("Arquivo uploaded com sucesso: {}", objectKey);

            return new StorageResult(
                    objectKey,
                    originalFilename,
                    file.getContentType(),
                    file.getSize(),
                    md5Hash
            );

        } catch (Exception e) {
            logger.error("Erro ao fazer upload do arquivo: {}", e.getMessage());
            throw new StorageException("Falha ao fazer upload do arquivo", e);
        }
    }
    
    /**
     * Valida o arquivo antes do upload.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Arquivo não pode ser vazio");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new StorageException("Apenas arquivos de imagem são permitidos");
        }

        // Limite de 10MB
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new StorageException("Arquivo excede o tamanho máximo de 10MB");
        }
    }
    
    /**
     * Extrai a extensão do arquivo.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * Calcula o hash MD5 do arquivo.
     */
    private String calculateMD5(InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;

            inputStream.mark(Integer.MAX_VALUE);
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            inputStream.reset();

            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);

        } catch (Exception e) {
            logger.warn("Não foi possível calcular MD5: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Gera uma URL pré-assinada para acesso ao arquivo.
     * A URL expira conforme configuração (padrão: 30 minutos).
     *
     * @param objectKey Chave do objeto no bucket
     * @return URL pré-assinada
     */
    public String getPresignedUrl(String objectKey) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry(presignedUrlExpiration, TimeUnit.MINUTES)
                    .build()
            );

            logger.debug("URL pré-assinada gerada para: {}", objectKey);
            return url;

        } catch (Exception e) {
            logger.error("Erro ao gerar URL pré-assinada: {}", e.getMessage());
            throw new StorageException("Falha ao gerar URL de acesso", e);
        }
    }
    
    /**
     * Resultado do upload.
     */
    public record StorageResult(
        String objectKey,
        String originalFilename,
        String contentType,
        Long size,
        String md5Hash
    ) {}

}
