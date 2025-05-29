package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileDTO {
    private String fileName;
    private List<ClassDTO> classes;
    private List<String> imports;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<ClassDTO> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassDTO> classes) {
        this.classes = classes;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }
}
