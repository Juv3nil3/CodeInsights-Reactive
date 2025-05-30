package com.juv3nil3.icdg.domain.elasticsearch;

import lombok.Builder;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Builder
public class ClassDataDocument {

    private String className;
    private String comment;

    // Constructors
    public ClassDataDocument() {}

    public ClassDataDocument(String className, String comment) {
        this.className = className;
        this.comment = comment;
    }

    // Getters and Setters
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Manual builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String className;
        private String comment;

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ClassDataDocument build() {
            return new ClassDataDocument(className, comment);
        }
    }
}
