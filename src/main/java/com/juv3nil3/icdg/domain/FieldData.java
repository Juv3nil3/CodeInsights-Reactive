package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class FieldData {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name; // Field name

    private Long classDataId;

    @Transient
    private List<AnnotationData> annotations = new ArrayList<>(); // Annotations on the field

    @Column
    private String comment; // Optional field comment

    @Transient
    private ClassData classData; // Reference to the parent class

    public FieldData() {}

    public FieldData(String name, String comment, ClassData classData) {
        this.name = name;
        this.comment = comment;
        this.classData = classData;
    }

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getClassDataId() {
        return classDataId;
    }

    public void setClassDataId(Long classDataId) {
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