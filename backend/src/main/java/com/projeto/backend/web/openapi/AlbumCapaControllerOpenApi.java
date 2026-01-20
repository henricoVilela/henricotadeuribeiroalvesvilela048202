package com.projeto.backend.web.openapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.backend.domain.album.TipoCapa;
import com.projeto.backend.web.dto.album.AlbumCapaResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
}
