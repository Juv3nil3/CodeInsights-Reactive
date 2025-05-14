package com.juv3nil3.icdg.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import org.springframework.data.annotation.Id;

@Entity
public class AnnotationData {

    @Id
    private Long id;

    private String annotation;

    private Long classDataId;  // Nullable if it's for a method
    private Long methodDataId; // Nullable if it's for a class
    private Long fieldDataId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Long getClassDataId() {
        return classDataId;
    }

    public void setClassDataId(Long classDataId) {
        this.classDataId = classDataId;
    }

    public Long getMethodDataId() {
        return methodDataId;
    }

    public void setMethodDataId(Long methodDataId) {
        this.methodDataId = methodDataId;
    }

    public Long getFieldDataId() {
        return fieldDataId;
    }

    public void setFieldDataId(Long fieldDataId) {
        this.fieldDataId = fieldDataId;
    }
}
