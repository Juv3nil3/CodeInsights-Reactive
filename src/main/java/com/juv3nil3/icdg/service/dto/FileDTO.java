package com.juv3nil3.icdg.service.dto;

import lombok.Data;

@Data
public class FileDTO {
    private String fileName;
    private List<ClassDTO> classes;
}
