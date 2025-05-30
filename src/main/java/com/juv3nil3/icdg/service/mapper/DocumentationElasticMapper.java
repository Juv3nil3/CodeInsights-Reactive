package com.juv3nil3.icdg.service.mapper;

import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.domain.elasticsearch.ClassDataDocument;
import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import com.juv3nil3.icdg.domain.elasticsearch.FileDataDocument;
import com.juv3nil3.icdg.domain.elasticsearch.PackageDataDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DocumentationElasticMapper {

    DocumentationElasticMapper INSTANCE = Mappers.getMapper(DocumentationElasticMapper.class);

    @Mapping(target = "id", expression = "java(documentation.getId().toString())")
    @Mapping(source = "branchMetadataId", target = "branchMetadataId")
    @Mapping(source = "exportPath", target = "exportPath")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "packages", target = "packages")
    DocumentationDocument toDocument(Documentation documentation);

    default PackageDataDocument toDocument(PackageData packageData) {
        List<FileDataDocument> fileDocs = packageData.getFileAssociations().stream()
                .map(BranchFileAssociation::getFile)
                .filter(Objects::nonNull)
                .map(this::toDocument)
                .collect(Collectors.toList());

        return PackageDataDocument.builder()
                .packageName(packageData.getPackageName())
                .files(fileDocs)
                .build();
    }


    @Mapping(source = "filePath", target = "filePath")
    @Mapping(source = "classes", target = "classes")
    FileDataDocument toDocument(FileData fileData);

    @Mapping(source = "name", target = "className")
    @Mapping(source = "comment", target = "comment")
    ClassDataDocument toDocument(ClassData classData);
}

