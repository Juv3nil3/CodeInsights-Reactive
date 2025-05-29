package com.juv3nil3.icdg.service;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.juv3nil3.icdg.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JavaFileParser {


    /** Parses a Java file's input stream using JavaParser and returns a FileData populated
     * with ClassData (which includes fields and methods).
     **/
    public Mono<FileData> parseJavaFile(InputStream inputStream) {
        return Mono.fromCallable(() -> {
            System.out.println("Starting Java file parsing...");

            FileData fileData = new FileData();

            try {
                CompilationUnit compilationUnit = parseCompilationUnit(inputStream);

                extractClassData(compilationUnit, fileData);
                extractImports(compilationUnit, fileData);

                System.out.println("Java file parsing completed.");
                return fileData;

            } catch (Exception e) {
                System.err.println("Error during Java file parsing: " + e.getMessage());
                throw new RuntimeException("Parsing failed", e);
            }
        });
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
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            ClassData classData = new ClassData();
            classData.setName(clazz.getNameAsString());
            classData.setFileData(fileData);

            //Initialize collections to avoid NullPointerException
            classData.setFields(new ArrayList<>());
            classData.setMethods(new ArrayList<>());
            classData.setAnnotations(new ArrayList<>());

            // Optional comment
            clazz.getComment().ifPresent(comment ->
                    classData.setComment(comment.getContent())
            );

            // Extract annotations on class
            List<AnnotationData> classAnnotations = clazz.getAnnotations().stream()
                    .map(this::toAnnotationData)
                    .collect(Collectors.toList());
            classData.getAnnotations().addAll(classAnnotations); // ✅ Use addAll for consistency

            // Extract fields
            clazz.getFields().forEach(field -> {
                FieldData fieldData = extractFieldData(field);
                fieldData.setClassData(classData);
                classData.getFields().add(fieldData);
            });

            // Extract methods
            clazz.getMethods().forEach(method -> {
                MethodData methodData = extractMethodData(method);
                methodData.setClassData(classData);
                classData.getMethods().add(methodData);
            });

            fileData.getClasses().add(classData);
        });
    }



    private FieldData extractFieldData(FieldDeclaration field) {
        FieldData fieldData = new FieldData();
        fieldData.setAnnotations(new ArrayList<>());

        field.getVariables().stream().findFirst().ifPresent(var -> {
            fieldData.setName(var.getNameAsString());
        });

        field.getComment().ifPresent(comment ->
                fieldData.setComment(comment.getContent())
        );

        List<AnnotationData> fieldAnnotations = field.getAnnotations().stream()
                .map(this::toAnnotationData)
                .collect(Collectors.toList());
        fieldData.getAnnotations().addAll(fieldAnnotations);

        return fieldData;
    }

    private MethodData extractMethodData(MethodDeclaration method) {
        MethodData methodData = new MethodData();
        methodData.setAnnotations(new ArrayList<>());
        methodData.setName(method.getNameAsString());

        method.getComment().ifPresent(comment ->
                methodData.setComment(comment.getContent())
        );

        List<AnnotationData> methodAnnotations = method.getAnnotations().stream()
                .map(this::toAnnotationData)
                .collect(Collectors.toList());
        methodData.getAnnotations().addAll(methodAnnotations);

        return methodData;
    }


    private AnnotationData toAnnotationData(AnnotationExpr expr) {
        AnnotationData annotation = new AnnotationData();
        annotation.setAnnotation(expr.getNameAsString());
        return annotation;
    }

    private void extractImports(CompilationUnit compilationUnit, FileData fileData) {
        List<String> projectImports = compilationUnit.getImports().stream()
                .map(ImportDeclaration::getNameAsString)
                .filter(importStr ->
                        !importStr.startsWith("java.") &&
                                !importStr.startsWith("javax.") &&
                                !importStr.startsWith("com.google") &&
                                !importStr.startsWith("jakarta.") &&
                                !importStr.startsWith("org.springframework.") &&
                                !importStr.startsWith("com.fasterxml.") &&
                                !importStr.startsWith("lombok."))
                .distinct()
                .collect(Collectors.toList());

        fileData.setImports(projectImports); // this also sets importsJson
    }



}

