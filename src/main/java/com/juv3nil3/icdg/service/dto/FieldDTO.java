package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class FieldDTO {
    private String name;
    private String comment;
    private List<String> annotations;

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
}
