package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class ClassData{

    @Id
    private UUID id;


    private String name;
    private String comment;
    private UUID fileDataId;

    @org.springframework.data.annotation.Transient
    private FileData fileData;

    @org.springframework.data.annotation.Transient
    private List<MethodData> methods = new ArrayList<>();

    @org.springframework.data.annotation.Transient
    private List<FieldData> fields = new ArrayList<>();

    @org.springframework.data.annotation.Transient
    private List<AnnotationData> annotations = new ArrayList<>();

    public ClassData() {}


    // Getters and Setters


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UUID getFileDataId() {
        return fileDataId;
    }

    public void setFileDataId(UUID fileDataId) {
        this.fileDataId = fileDataId;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }

    public List<MethodData> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodData> methods) {
        this.methods = methods;
    }

    public List<FieldData> getFields() {
        return fields;
    }

    public void setFields(List<FieldData> fields) {
        this.fields = fields;
    }

    public List<AnnotationData> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationData> annotations) {
        this.annotations = annotations;
    }
}