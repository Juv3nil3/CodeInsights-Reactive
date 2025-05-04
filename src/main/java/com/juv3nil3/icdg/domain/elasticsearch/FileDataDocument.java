package com.juv3nil3.icdg.domain.elasticsearch;


import lombok.Builder;

import java.util.List;

@Builder
public class FileDataDocument {
    private String filePath;
    private List<ClassDataDocument> classes;


    public FileDataDocument() {
    }

    public FileDataDocument(String filePath, List<ClassDataDocument> classes) {
        this.filePath = filePath;
        this.classes = classes;
    }

    public List<ClassDataDocument> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassDataDocument> classes) {
        this.classes = classes;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
