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
}
