package com.projeto.backend.domain.album;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.projeto.backend.domain.artista.Artista;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entidade que representa um álbum musical.
 * Um álbum pertence a um único artista.
 */
@Entity
@Table(name = "album")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artista_id", nullable = false)
    private Artista artista;

    @Column(name = "nome", nullable = false, length = 300)
    private String nome;

    @Column(name = "nome_busca", nullable = false, length = 300)
    private String nomeBusca;

    @Column(name = "ano_lancamento")
    private Integer anoLancamento;

    @Column(name = "gravadora", length = 150)
    private String gravadora;

    @Column(name = "genero", length = 100)
    private String genero;

    @Column(name = "total_faixas")
    private Integer totalFaixas;

    @Column(name = "duracao_total")
    private Integer duracaoTotal;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AlbumCapa> capas = new ArrayList<>();

    public Album() {
    }

    public Album(String nome, Artista artista) {
        this.nome = nome;
        this.artista = artista;
        this.nomeBusca = normalizarNome(nome);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.nomeBusca == null || this.nomeBusca.isEmpty()) {
            this.nomeBusca = normalizarNome(this.nome);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.nomeBusca = normalizarNome(this.nome);
    }

    /**
     * Normaliza o nome para busca (remove acentos e converte para minúsculas).
     */
    private String normalizarNome(String nome) {
        if (nome == null) return "";
        return java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();
    }

    /**
     * Retorna a quantidade de capas do álbum.
     */
    public int getTotalCapas() {
        return capas != null ? capas.size() : 0;
    }

    /**
     * Adiciona uma capa ao álbum.
     */
    public void addCapa(AlbumCapa capa) {
        capas.add(capa);
        capa.setAlbum(this);
    }

    /**
     * Remove uma capa do álbum.
     */
    public void removeCapa(AlbumCapa capa) {
        capas.remove(capa);
        capa.setAlbum(null);
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Artista getArtista() {
        return artista;
    }

    public void setArtista(Artista artista) {
        this.artista = artista;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        this.nomeBusca = normalizarNome(nome);
    }

    public String getNomeBusca() {
        return nomeBusca;
    }

    public void setNomeBusca(String nomeBusca) {
        this.nomeBusca = nomeBusca;
    }

    public Integer getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(Integer anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public String getGravadora() {
        return gravadora;
    }

    public void setGravadora(String gravadora) {
        this.gravadora = gravadora;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getTotalFaixas() {
        return totalFaixas;
    }

    public void setTotalFaixas(Integer totalFaixas) {
        this.totalFaixas = totalFaixas;
    }

    public Integer getDuracaoTotal() {
        return duracaoTotal;
    }

    public void setDuracaoTotal(Integer duracaoTotal) {
        this.duracaoTotal = duracaoTotal;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<AlbumCapa> getCapas() {
        return capas;
    }

    public void setCapas(List<AlbumCapa> capas) {
        this.capas = capas;
    }
}
