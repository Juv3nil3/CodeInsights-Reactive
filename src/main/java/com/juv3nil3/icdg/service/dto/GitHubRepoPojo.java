package com.juv3nil3.icdg.service.dto;

// Minimal POJO to deserialize GitHub repo objects
public class GitHubRepoPojo {
    private String name;
    private String language;

    // Getters and setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
}