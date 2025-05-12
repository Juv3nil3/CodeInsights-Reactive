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
}
