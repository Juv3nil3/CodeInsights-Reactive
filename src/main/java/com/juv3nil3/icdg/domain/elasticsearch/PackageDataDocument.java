package com.juv3nil3.icdg.domain.elasticsearch;

import lombok.Builder;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Builder
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
}
