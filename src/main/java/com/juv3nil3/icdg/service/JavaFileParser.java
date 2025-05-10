package com.juv3nil3.icdg.service;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.juv3nil3.icdg.domain.ClassData;
import com.juv3nil3.icdg.domain.FieldData;
import com.juv3nil3.icdg.domain.FileData;
import com.juv3nil3.icdg.domain.MethodData;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JavaFileParser {


    /** Parses a Java file's input stream using JavaParser and returns a FileData populated
     * with ClassData (which includes fields and methods).
     **/
    public FileData parseJavaFile(InputStream inputStream) throws Exception {
        // Log start
        System.out.println("Starting Java file parsing...");

        // Initialize FileData; ideally, also set the file path externally
        FileData fileData = new FileData();

        try {
            // Parse using JavaParser
            CompilationUnit compilationUnit = parseCompilationUnit(inputStream);
            // Extract package if available (set externally)
            Optional<PackageDeclaration> pkgDecl = compilationUnit.getPackageDeclaration();
            pkgDecl.ifPresent(pd -> fileData.setRepoName(pd.getNameAsString()));

            // Extract classes and populate FileData
            extractClassData(compilationUnit, fileData);

        } catch (Exception e) {
            System.err.println("Error during Java file parsing: " + e.getMessage());
            throw e;
        }

        System.out.println("Java file parsing completed.");
        return fileData;
    }

    private CompilationUnit parseCompilationUnit(InputStream inputStream) throws Exception {
        JavaParser parser = new JavaParser();
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        ParseResult<CompilationUnit> parseResult = parser.parse(content);
        if (!parseResult.isSuccessful()) {
            throw new IllegalArgumentException("Parsing issues: " + parseResult.getProblems());
        }
        return parseResult.getResult()
                .orElseThrow(() -> new IllegalArgumentException("Unable to parse Java content"));
    }

    private void extractClassData(CompilationUnit compilationUnit, FileData fileData) {
        if (fileData == null) {
            throw new IllegalArgumentException("FileData cannot be null");
        }

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(clazz -> {
                    try {
                        ClassData classData = new ClassData();
                        classData.setName(clazz.getNameAsString());
                        classData.setAnnotations(clazz.getAnnotations().stream()
                                .map(a -> a.getNameAsString())
                                .collect(Collectors.toList()));

                        clazz.getComment().ifPresent(comment ->
                                classData.setComment(comment.getContent())
                        );

                        // Extract fields (and methods) using helper methods
                        clazz.getFields().forEach(field -> {
                            FieldData fieldData = extractFieldData(field);
                            if (fieldData != null) {
                                classData.getFields().add(fieldData);
                            }
                        });
                        clazz.getMethods().forEach(method -> {
                            MethodData methodData = extractMethodData(method);
                            if (methodData != null) {
                                classData.getMethods().add(methodData);
                            }
                        });

                        // Associate classData with fileData and avoid duplicates
                        if (fileData.getClasses() == null) {
                            fileData.setClasses(new ArrayList<>());
                        }
                        boolean exists = fileData.getClasses().stream()
                                .anyMatch(existing -> existing.getName().equals(classData.getName()));
                        if (!exists) {
                            fileData.getClasses().add(classData);
                        }

                        classData.setFileData(fileData);

                    } catch (Exception e) {
                        System.err.println("Error extracting class data for file: " + fileData.getFilePath());
                        e.printStackTrace();
                    }
                });
    }

    private FieldData extractFieldData(FieldDeclaration field) {
        FieldData fieldData = new FieldData();

        field.getVariables().stream().findFirst().ifPresent(variable -> {
            fieldData.setName(variable.getNameAsString());
        });
        fieldData.setAnnotations(field.getAnnotations().stream()
                .map(a -> a.getNameAsString())
                .collect(Collectors.toList()));
        field.getComment().ifPresent(comment -> fieldData.setComment(comment.getContent()));
        return fieldData;
    }

    private MethodData extractMethodData(MethodDeclaration method) {
        MethodData methodData = new MethodData();
        methodData.setName(method.getNameAsString());
        methodData.setAnnotations(method.getAnnotations().stream()
                .map(a -> a.getNameAsString())
                .collect(Collectors.toList()));
        method.getComment().ifPresent(comment -> methodData.setComment(comment.getContent()));
        return methodData;
    }
}

