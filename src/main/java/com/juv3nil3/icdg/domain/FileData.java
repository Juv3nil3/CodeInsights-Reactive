package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

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
}

