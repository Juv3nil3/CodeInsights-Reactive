package com.juv3nil3.icdg.service.mapper;

import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.service.dto.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DocumentationMapper {
    public DocumentationDTO toDto(Documentation documentation) {
        if (documentation == null) return null;

        DocumentationDTO dto = new DocumentationDTO();
        dto.setBranchName(documentation.getBranchMetadata().getBranchName());
        dto.setRepoName(documentation.getBranchMetadata().getRepositoryMetadata().getRepoName());
        dto.setOwner(documentation.getBranchMetadata().getRepositoryMetadata().getOwner());
        dto.setDescription(documentation.getBranchMetadata().getRepositoryMetadata().getDescription());
        dto.setCreatedAt(documentation.getCreatedAt());
        dto.setUpdatedAt(documentation.getUpdatedAt());

        // Convert packages
        List<PackageDTO> packageDTOs = documentation.getPackages().stream()
                .map(this::toPackageDto)
                .collect(Collectors.toList());
        dto.setPackages(packageDTOs);

        return dto;
    }

    private PackageDTO toPackageDto(PackageData packageData) {
        PackageDTO dto = new PackageDTO();
        dto.setPackageName(packageData.getPackageName());

        // Files inside the package (from branchFileAssociation)
        List<FileDTO> fileDTOs = Optional.ofNullable(packageData.getFileAssociations())
                .orElse(Collections.emptyList())
                .stream()
                .map(BranchFileAssociation::getFile)
                .map(this::toFileDto)
                .collect(Collectors.toList());

        dto.setFiles(fileDTOs);

        // Sub-packages (optional)
        List<PackageDTO> subPackageDTOs = packageData.getSubPackages().stream()
                .map(this::toPackageDto)
                .collect(Collectors.toList());
        dto.setSubPackages(subPackageDTOs);

        return dto;
    }

    private FileDTO toFileDto(FileData fileData) {
        FileDTO dto = new FileDTO();
        dto.setFileName(fileData.getFileName());

        List<ClassDTO> classDTOs = fileData.getClasses().stream()
                .map(this::toClassDto)
                .collect(Collectors.toList());
        dto.setClasses(classDTOs);

        return dto;
    }

    private ClassDTO toClassDto(ClassData classData) {
        ClassDTO dto = new ClassDTO();
        dto.setName(classData.getName());
        dto.setComment(classData.getComment());
        dto.setAnnotations(classData.getAnnotations());

        List<MethodDTO> methodDTOs = classData.getMethods().stream()
                .map(this::toMethodDto)
                .collect(Collectors.toList());
        dto.setMethods(methodDTOs);

        List<FieldDTO> fieldDTOs = classData.getFields().stream()
                .map(this::toFieldDto)
                .collect(Collectors.toList());
        dto.setFields(fieldDTOs);

        return dto;
    }

    private MethodDTO toMethodDto(MethodData methodData) {
        MethodDTO dto = new MethodDTO();
        dto.setName(methodData.getName());
        dto.setComment(methodData.getComment());
        dto.setAnnotations(methodData.getAnnotations());
        return dto;
    }

    private FieldDTO toFieldDto(FieldData fieldData) {
        FieldDTO dto = new FieldDTO();
        dto.setName(fieldData.getName());
        dto.setComment(fieldData.getComment());
        dto.setAnnotations(fieldData.getAnnotations());
        return dto;
    }
}
