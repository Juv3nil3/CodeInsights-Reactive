package com.juv3nil3.icdg.service.mapper;

import com.juv3nil3.icdg.domain.ClassData;
import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.FileData;
import com.juv3nil3.icdg.domain.PackageData;
import com.juv3nil3.icdg.domain.elasticsearch.ClassDataDocument;
import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import com.juv3nil3.icdg.domain.elasticsearch.FileDataDocument;
import com.juv3nil3.icdg.domain.elasticsearch.PackageDataDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

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

    PackageDataDocument toDocument(PackageData packageData);

    @Mapping(source = "filePath", target = "filePath")
    @Mapping(source = "classes", target = "classes")
    FileDataDocument toDocument(FileData fileData);

    @Mapping(source = "name", target = "className")
    @Mapping(source = "comment", target = "comment")
    ClassDataDocument toDocument(ClassData classData);
}

