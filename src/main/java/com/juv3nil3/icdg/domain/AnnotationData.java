package com.juv3nil3.icdg.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Entity
public class AnnotationData {

    @Id
    private UUID id;


    private String annotation;

    private UUID classDataId;  // Nullable if it's for a method
    private UUID methodDataId; // Nullable if it's for a class
    private UUID fieldDataId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public UUID getClassDataId() {
        return classDataId;
    }

    public void setClassDataId(UUID classDataId) {
        this.classDataId = classDataId;
    }

    public UUID getMethodDataId() {
        return methodDataId;
    }

    public void setMethodDataId(UUID methodDataId) {
        this.methodDataId = methodDataId;
    }

    public UUID getFieldDataId() {
        return fieldDataId;
    }

    public void setFieldDataId(UUID fieldDataId) {
        this.fieldDataId = fieldDataId;
    }
}
