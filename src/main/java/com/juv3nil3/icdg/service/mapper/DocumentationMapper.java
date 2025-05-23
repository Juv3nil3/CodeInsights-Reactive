package com.juv3nil3.icdg.service.mapper;


import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DocumentationMapper {

    private static Logger log = LoggerFactory.getLogger(DocumentationMapper.class);

    public DocumentationDTO toDto(Documentation documentation) {
        if (documentation == null) {
            log.warn("📄 Documentation is null, returning null DTO.");
            return null;
        }

        log.info("📄 Mapping documentation for repo={}, branch={}",
                documentation.getBranchMetadata().getRepositoryMetadata().getRepoName(),
                documentation.getBranchMetadata().getBranchName());

        DocumentationDTO dto = new DocumentationDTO();
        dto.setBranchName(documentation.getBranchMetadata().getBranchName());
        dto.setRepoName(documentation.getBranchMetadata().getRepositoryMetadata().getRepoName());
        dto.setOwner(documentation.getBranchMetadata().getRepositoryMetadata().getOwner());
        dto.setDescription(documentation.getBranchMetadata().getRepositoryMetadata().getDescription());
        dto.setCreatedAt(documentation.getCreatedAt());
        dto.setUpdatedAt(documentation.getUpdatedAt());

        List<PackageData> allPackages = documentation.getPackages();
        log.info("📦 Found {} total packages", allPackages.size());

        Map<UUID, List<PackageData>> parentMap = allPackages.stream()
                .filter(pkg -> pkg.getParentPackageId() != null)
                .collect(Collectors.groupingBy(PackageData::getParentPackageId));

        List<PackageDTO> rootPackages = allPackages.stream()
                .filter(pkg -> pkg.getParentPackageId() == null)
                .map(pkg -> toPackageDto(pkg, parentMap))
                .collect(Collectors.toList());

        dto.setPackages(rootPackages);
        log.info("✅ Mapped documentation DTO with {} root packages", rootPackages.size());
        return dto;
    }

    private PackageDTO toPackageDto(PackageData pkg, Map<UUID, List<PackageData>> parentMap) {
        log.debug("➡️ Mapping package: {}", pkg.getPackageName());

        PackageDTO dto = new PackageDTO();
        dto.setPackageName(pkg.getPackageName());

        List<BranchFileAssociation> associations = Optional.ofNullable(pkg.getFileAssociations()).orElse(Collections.emptyList());
        log.debug("📎 Package {} has {} file associations", pkg.getPackageName(), associations.size());

        List<FileDTO> fileDtos = associations.stream()
                .map(BranchFileAssociation::getFile)
                .filter(Objects::nonNull)
                .map(this::toFileDto)
                .collect(Collectors.toList());
        dto.setFiles(fileDtos);

        List<PackageData> subPackages = parentMap.getOrDefault(pkg.getId(), Collections.emptyList());
        log.debug("📦 Package {} has {} sub-packages", pkg.getPackageName(), subPackages.size());

        List<PackageDTO> subDtos = subPackages.stream()
                .map(child -> toPackageDto(child, parentMap))
                .collect(Collectors.toList());
        dto.setSubPackages(subDtos);

        return dto;
    }

    private FileDTO toFileDto(FileData file) {
        log.debug("📝 Mapping file: {}", file.getFileName());

        FileDTO dto = new FileDTO();
        dto.setFileName(file.getFileName());

        List<ClassDTO> classDtos = Optional.ofNullable(file.getClasses())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toClassDto)
                .collect(Collectors.toList());
        log.debug("📘 File {} has {} classes", file.getFileName(), classDtos.size());

        dto.setClasses(classDtos);
        return dto;
    }

    private ClassDTO toClassDto(ClassData classData) {
        log.debug("📗 Mapping class: {}", classData.getName());

        ClassDTO dto = new ClassDTO();
        dto.setName(classData.getName());
        dto.setComment(classData.getComment());

        List<MethodDTO> methodDtos = Optional.ofNullable(classData.getMethods())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toMethodDto)
                .collect(Collectors.toList());
        log.debug("🔧 Class {} has {} methods", classData.getName(), methodDtos.size());
        dto.setMethods(methodDtos);

        List<FieldDTO> fieldDtos = Optional.ofNullable(classData.getFields())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toFieldDto)
                .collect(Collectors.toList());
        log.debug("🔑 Class {} has {} fields", classData.getName(), fieldDtos.size());
        dto.setFields(fieldDtos);

        return dto;
    }

    private MethodDTO toMethodDto(MethodData method) {
        log.trace("⚙️ Mapping method: {}", method.getName());
        MethodDTO dto = new MethodDTO();
        dto.setName(method.getName());
        dto.setComment(method.getComment());
        return dto;
    }

    private FieldDTO toFieldDto(FieldData field) {
        log.trace("📐 Mapping field: {}", field.getName());
        FieldDTO dto = new FieldDTO();
        dto.setName(field.getName());
        dto.setComment(field.getComment());
        return dto;
    }
}
