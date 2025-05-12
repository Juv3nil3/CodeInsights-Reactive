package com.juv3nil3.icdg.service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentationDTO {
    private String repoName;
    private String owner;
    private String branchName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PackageDTO> packages;
}
