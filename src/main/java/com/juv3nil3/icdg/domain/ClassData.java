package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ClassData{
    @Id
    private Long id;

    private String name;
    private String comment;
    private Long fileDataId;

    @Transient
    private FileData fileData;

    @Transient
    private List<MethodData> methods = new ArrayList<>();

    @Transient
    private List<FieldData> fields = new ArrayList<>();

    @Transient
    private List<AnnotationData> annotations = new ArrayList<>();

    public ClassData() {}


    // Getters and Setters


    public Long getId() {
        return id;
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

    public List<MethodData> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodData> methods) {
        this.methods = methods;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }

    public List<FieldData> getFields() {
        return fields;
    }

    public void setFields(List<FieldData> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "ClassData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", annotations=" + annotations +
                ", numberOfFields=" + (fields != null ? fields.size() : 0) +
                ", numberOfMethods=" + (methods != null ? methods.size() : 0) +
                '}';
    }
}