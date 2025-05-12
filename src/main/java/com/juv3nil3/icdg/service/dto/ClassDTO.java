package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClassDTO {
    private String name;
    private String comment;
    private List<String> annotations;
    private List<MethodDTO> methods;
    private List<FieldDTO> fields;

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

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public List<MethodDTO> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodDTO> methods) {
        this.methods = methods;
    }

    public List<FieldDTO> getFields() {
        return fields;
    }

    public void setFields(List<FieldDTO> fields) {
        this.fields = fields;
    }
}
