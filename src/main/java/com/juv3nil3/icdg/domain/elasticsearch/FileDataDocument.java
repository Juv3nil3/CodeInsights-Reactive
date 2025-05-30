package com.juv3nil3.icdg.domain.elasticsearch;


import lombok.Builder;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Builder
public class FileDataDocument {
    private String filePath;

    @Field(type = FieldType.Nested)
    private List<ClassDataDocument> classes;

    // Constructors
    public FileDataDocument() {}

    public FileDataDocument(String filePath, List<ClassDataDocument> classes) {
        this.filePath = filePath;
        this.classes = classes;
    }

    // Getters and Setters
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<ClassDataDocument> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassDataDocument> classes) {
        this.classes = classes;
    }

    // Manual builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String filePath;
        private List<ClassDataDocument> classes;

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder classes(List<ClassDataDocument> classes) {
            this.classes = classes;
            return this;
        }

        public FileDataDocument build() {
            return new FileDataDocument(filePath, classes);
        }
    }
}
