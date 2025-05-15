package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Documentation {

    @Id
    private Long id;

    private Long branchMetadataId;

    private String exportPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @org.springframework.data.annotation.Transient
    private BranchMetadata branchMetadata;

    @org.springframework.data.annotation.Transient
    private List<PackageData> packages = new ArrayList<>();

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBranchMetadataId() {
        return branchMetadataId;
    }

    public void setBranchMetadataId(Long branchMetadataId) {
        this.branchMetadataId = branchMetadataId;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
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

    public BranchMetadata getBranchMetadata() {
        return branchMetadata;
    }

    public void setBranchMetadata(BranchMetadata branchMetadata) {
        this.branchMetadata = branchMetadata;
    }

    public List<PackageData> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageData> packages) {
        this.packages = packages;
    }
}

