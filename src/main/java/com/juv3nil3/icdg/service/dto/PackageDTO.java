package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PackageDTO {

    private String packageName;
    private List<FileDTO> files;
    private List<PackageDTO> subPackages;
}
