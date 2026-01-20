package com.projeto.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

@Configuration
public class MinioConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin123}")
    private String secretKey;

    @Value("${minio.bucket-name:album-covers}")
    private String bucketName;

    /**
     * Cria e configura o cliente MinIO.
     * Também garante que o bucket exista.
     *
     * @return MinioClient configurado
     */
    @Bean
    MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

            boolean bucketExists = client.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (!bucketExists) {
                client.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                logger.info("Bucket '{}' criado com sucesso", bucketName);
            } else {
                logger.info("Bucket '{}' já existe", bucketName);
            }

            logger.info("Cliente MinIO configurado com sucesso - Endpoint: {}", endpoint);
            return client;

        } catch (Exception e) {
            logger.error("Erro ao configurar cliente MinIO: {}", e.getMessage());
            throw new RuntimeException("Falha ao inicializar MinIO", e);
        }
    }
}