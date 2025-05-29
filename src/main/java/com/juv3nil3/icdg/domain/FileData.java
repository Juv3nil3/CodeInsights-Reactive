package com.juv3nil3.icdg.domain;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class FileData {

    @Id
    private UUID id;


    private String fileName;
    private String repoName;
    private String filePath;

    private String contentHash;

    private String importsJson; // Persisted as JSON string

    @org.springframework.data.annotation.Transient
    private List<String> imports = new ArrayList<>();

    @org.springframework.data.annotation.Transient
    private List<ClassData> classes = new ArrayList<>();

    // Helper methods for managing the bidirectional relationship
    public void addClass(ClassData classData) {
        classes.add(classData);
        classData.setFileData(this);
    }

    public void removeClass(ClassData classData) {
        classes.remove(classData);
        classData.setFileData(null);
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
        this.importsJson = toJson(imports);
    }

    public List<String> getImports() {
        if (imports == null && importsJson != null) {
            this.imports = fromJson(importsJson);
        }
        return imports;
    }

    public String toJson(List<String> list) {
        try {
            return new ObjectMapper().writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize imports", e);
        }
    }

    public List<String> fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize importsJson", e);
        }
    }


    // Getters and Setters


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public List<ClassData> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassData> classes) {
        this.classes = classes;
    }

    public String getImportsJson() {
        return importsJson;
    }

    public void setImportsJson(String importsJson) {
        this.importsJson = importsJson;
    }
}

