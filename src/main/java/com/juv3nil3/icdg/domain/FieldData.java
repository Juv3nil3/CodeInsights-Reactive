package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
public class FieldData {

    @Id
    private UUID id;


    @Column(nullable = false)
    private String name; // Field name

    private UUID classDataId;

    @org.springframework.data.annotation.Transient
    private List<AnnotationData> annotations = new ArrayList<>(); // Annotations on the field

    @Column
    private String comment; // Optional field comment

    @org.springframework.data.annotation.Transient
    private ClassData classData; // Reference to the parent class

    public FieldData() {}

    public FieldData(String name, String comment, ClassData classData) {
        this.name = name;
        this.comment = comment;
        this.classData = classData;
    }

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

    public List<AnnotationData> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationData> annotations) {
        this.annotations = annotations;
    }

    public UUID getClassDataId() {
        return classDataId;
    }

    public void setClassDataId(UUID classDataId) {
        this.classDataId = classDataId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ClassData getClassData() {
        return classData;
    }

    public void setClassData(ClassData classData) {
        this.classData = classData;
    }
}