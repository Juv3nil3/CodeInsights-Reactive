package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class BranchMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchName;
    private String latestCommitHash;

    @ManyToOne
    @JoinColumn(name = "repository_metadata_id")
    private RepositoryMetadata repositoryMetadata;

    @OneToOne(mappedBy = "branchMetadata", cascade = CascadeType.ALL)
    private Documentation documentation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getLatestCommitHash() {
        return latestCommitHash;
    }

    public void setLatestCommitHash(String latestCommitHash) {
        this.latestCommitHash = latestCommitHash;
    }

    public RepositoryMetadata getRepositoryMetadata() {
        return repositoryMetadata;
    }

    public void setRepositoryMetadata(RepositoryMetadata repositoryMetadata) {
        this.repositoryMetadata = repositoryMetadata;
    }

    public Documentation getDocumentation() {
        return documentation;
    }

    public void setDocumentation(Documentation documentation) {
        this.documentation = documentation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

