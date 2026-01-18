package com.projeto.backend.web.openapi;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.projeto.backend.web.dto.artista.ArtistaResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Artistas", description = "Endpoints para gerenciamento de artistas")
@SecurityRequirement(name = "bearerAuth")
public interface ArtistaControllerOpenApi {

    @Operation(
            summary = "Listar artistas",
            description = "Retorna uma lista paginada de artistas com filtro por nome e ordenação"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de artistas retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Page<ArtistaResponse>> listar(
            @Parameter(description = "Filtrar por nome do artista", required = false) String nome,
            @Parameter(description = "Número da página (começa em 0)") int page,
            @Parameter(description = "Quantidade de itens por página") int size,
            @Parameter(description = "Campo para ordenação (nome, tipo, anoFormacao)") String sortBy,
            @Parameter(description = "Direção da ordenação (asc ou desc)") String sortDir
    );
    
    public ResponseEntity<ArtistaResponse> buscarPorId(@Parameter(description = "ID do artista") Long id);
}
