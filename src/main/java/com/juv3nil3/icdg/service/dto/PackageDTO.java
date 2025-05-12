package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PackageDTO {

    private String packageName;
    private List<FileDTO> files;
    private List<PackageDTO> subPackages;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<FileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<FileDTO> files) {
        this.files = files;
    }

    public List<PackageDTO> getSubPackages() {
        return subPackages;
    }

    public void setSubPackages(List<PackageDTO> subPackages) {
        this.subPackages = subPackages;
    }
}
