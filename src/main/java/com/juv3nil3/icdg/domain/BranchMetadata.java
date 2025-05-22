package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class BranchMetadata {

    @Id
    private UUID id;


    private String branchName;
    private String latestCommitHash;

    private UUID repositoryMetadataId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @org.springframework.data.annotation.Transient
    private RepositoryMetadata repositoryMetadata;

    @org.springframework.data.annotation.Transient
    private Documentation documentation;

    public BranchMetadata(UUID id, String branchName, String latestCommitHash, UUID repositoryMetadataId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.branchName = branchName;
        this.latestCommitHash = latestCommitHash;
        this.repositoryMetadataId = repositoryMetadataId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public UUID getRepositoryMetadataId() {
        return repositoryMetadataId;
    }

    public void setRepositoryMetadataId(UUID repositoryMetadataId) {
        this.repositoryMetadataId = repositoryMetadataId;
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
}

