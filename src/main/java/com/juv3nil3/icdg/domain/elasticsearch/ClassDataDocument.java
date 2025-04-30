package com.juv3nil3.icdg.domain.elasticsearch;

import java.util.List;

public class ClassDataDocument {

    private String className;
    private String modifiers;
    private String superClassName;
    private List<String> interfaces;
    private List<MethodDataDocument> methods;

    // Getters and Setters

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public List<MethodDataDocument> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodDataDocument> methods) {
        this.methods = methods;
    }
}
