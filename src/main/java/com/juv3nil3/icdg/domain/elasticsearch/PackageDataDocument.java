package com.juv3nil3.icdg.domain.elasticsearch;

import java.util.List;

public class PackageDataDocument {

    private String packageName;
    private List<ClassDataDocument> classes;

    // Getters and Setters

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<ClassDataDocument> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassDataDocument> classes) {
        this.classes = classes;
    }
}
