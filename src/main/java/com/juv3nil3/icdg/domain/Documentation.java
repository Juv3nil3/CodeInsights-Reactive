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

    @Transient
    private BranchMetadata branchMetadata;

    @Transient
    private List<PackageData> packages = new ArrayList<>();

    // Getters and Setters


    public BranchMetadata getBranchMetadata() {
        return branchMetadata;
    }

    public void setBranchMetadata(BranchMetadata branchMetadata) {
        this.branchMetadata = branchMetadata;
    }

    public Long getId() {
        return id;
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

    public List<PackageData> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageData> packages) {
        this.packages = packages;
    }



}

