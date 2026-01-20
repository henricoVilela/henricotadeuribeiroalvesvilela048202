package com.projeto.backend.web.openapi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.backend.domain.album.TipoCapa;
import com.projeto.backend.web.dto.album.AlbumCapaResponse;
import com.projeto.backend.web.dto.album.AlbumCapaUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Capas de Álbuns", description = "Endpoints para gerenciamento de capas de álbuns")
@SecurityRequirement(name = "bearerAuth")
public interface AlbumCapaControllerOpenApi {
	@Operation(
        summary = "Upload de capa",
        description = "Faz upload de uma imagem de capa para um álbum. Aceita apenas arquivos de imagem (JPEG, PNG, GIF, WebP) com tamanho máximo de 10MB."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Capa enviada com sucesso",
                content = @Content(schema = @Schema(implementation = AlbumCapaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Álbum não encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<AlbumCapaResponse> upload(
        @Parameter(description = "ID do álbum") Long albumId,
        @Parameter(description = "Arquivo de imagem") MultipartFile file,
        @Parameter(description = "Tipo da capa", example = "FRENTE") TipoCapa tipoCapa
    );
	
	@Operation(
        summary = "Upload múltiplo de capas",
        description = "Faz upload de múltiplas imagens de capa para um álbum. Aceita apenas arquivos de imagem com tamanho máximo de 10MB cada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Capas enviadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo(s) inválido(s)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Álbum não encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<List<AlbumCapaResponse>> uploadMultiple(
        @Parameter(description = "ID do álbum") Long albumId,
        @Parameter(description = "Lista de arquivos de imagem") List<MultipartFile> files,
        @Parameter(description = "Tipo das capas", example = "FRENTE") TipoCapa tipoCapa
    );
	
	@Operation(
            summary = "Listar capas do álbum",
            description = "Retorna todas as capas de um álbum com URLs pré-assinadas (válidas por 30 minutos)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de capas retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Álbum não encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<List<AlbumCapaResponse>> listarPorAlbum(
        @Parameter(description = "ID do álbum") Long albumId
    );
	
	@Operation(
            summary = "Buscar capa por ID",
            description = "Retorna os dados de uma capa específica com URL pré-assinada"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Capa encontrada",
                    content = @Content(schema = @Schema(implementation = AlbumCapaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Capa não encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<AlbumCapaResponse> buscarPorId(
        @Parameter(description = "ID da capa") Long capaId
    );
	
	@Operation(
            summary = "Atualizar capa",
            description = "Atualiza o tipo e/ou ordem de uma capa existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Capa atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = AlbumCapaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Capa não encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<AlbumCapaResponse> atualizar(
            @Parameter(description = "ID da capa") Long capaId,
            @RequestBody AlbumCapaUpdateRequest request
    );
	
	@Operation(
        summary = "Remover capa",
        description = "Remove uma capa do álbum e do armazenamento"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Capa removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Capa não encontrada", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Void> deletar(
        @Parameter(description = "ID da capa") Long capaId
    );
	
	@Operation(
            summary = "Remover todas as capas",
            description = "Remove todas as capas de um álbum"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Capas removidas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Álbum não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Void> deletarTodas(
        @Parameter(description = "ID do álbum") Long albumId
    );
}
