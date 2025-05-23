package com.juv3nil3.icdg.domain.elasticsearch;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(indexName = "documentation")
@Builder
public class DocumentationDocument {

    @Id
    private String id;

    private UUID branchMetadataId;
    private String exportPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Nested)
    private List<PackageDataDocument> packages;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getBranchMetadataId() {
        return branchMetadataId;
    }

    public void setBranchMetadataId(UUID branchMetadataId) {
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
