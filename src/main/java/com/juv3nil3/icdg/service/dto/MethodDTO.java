package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class MethodDTO {
    private String name;
    private String comment;
    private List<String> annotations;
}
