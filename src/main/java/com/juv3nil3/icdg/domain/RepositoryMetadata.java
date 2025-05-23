package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class RepositoryMetadata {

    @Id
    private UUID id;


    private String owner; // Owner of the repository
    private String repoName; // Name of the repository
    private String description; // Description of the repository
    private String defaultBranch; // Default branch (e.g., "main" or "master")

    private LocalDateTime createdAt; // Timestamp when the metadata was created
    private LocalDateTime updatedAt; // Timestamp when the metadata was last updated

    @org.springframework.data.annotation.Transient
    private List<BranchMetadata> branches = new ArrayList<>();

    // Constructors, Getters, and Setters

    public RepositoryMetadata() {}

    public RepositoryMetadata(String owner, String repoName, String description, String defaultBranch) {
        this.owner = owner;
        this.repoName = repoName;
        this.description = description;
        this.defaultBranch = defaultBranch;
    }

    public RepositoryMetadata(UUID uuid, String owner, String repoName, String description, String defaultBranch, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = uuid;
        this.owner = owner;
        this.repoName = repoName;
        this.description = description;
        this.defaultBranch = defaultBranch;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
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

    public List<BranchMetadata> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchMetadata> branches) {
        this.branches = branches;
    }
}

