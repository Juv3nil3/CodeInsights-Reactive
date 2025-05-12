package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileDTO {
    private String fileName;
    private List<ClassDTO> classes;
}
