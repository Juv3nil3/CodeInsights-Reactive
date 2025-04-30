package com.juv3nil3.icdg.domain.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "documentation")
public class DocumentationDocument {

    @Id
    private String id;

    private Long branchMetadataId;
    private String exportPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PackageDataDocument> packages;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public List<PackageDataDocument> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageDataDocument> packages) {
        this.packages = packages;
    }
}
