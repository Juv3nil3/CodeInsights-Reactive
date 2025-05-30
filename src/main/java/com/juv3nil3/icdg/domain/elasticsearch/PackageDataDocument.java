package com.juv3nil3.icdg.domain.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

public class PackageDataDocument {

    private String packageName;

    @Field(type = FieldType.Nested)
    private List<FileDataDocument> files;

    public PackageDataDocument() {
    }

    public PackageDataDocument(String packageName, List<FileDataDocument> files) {
        this.packageName = packageName;
        this.files = files;
    }

    // Getters and Setters

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<FileDataDocument> getFiles() {
        return files;
    }

    public void setFiles(List<FileDataDocument> files) {
        this.files = files;
    }

    // Manual builder implementation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String packageName;
        private List<FileDataDocument> files;

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder files(List<FileDataDocument> files) {
            this.files = files;
            return this;
        }

        public PackageDataDocument build() {
            return new PackageDataDocument(packageName, files);
        }
    }
}