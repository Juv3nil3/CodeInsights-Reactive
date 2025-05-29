package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.ClassData;
import com.juv3nil3.icdg.domain.FileData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DependencyGraphBuilder {

    private static final String SOURCE_ROOT = "src/main/java/";

    /**
     * Builds a dependency graph where each node is a file,
     * and there’s an edge from file A → file B if A imports B's class.
     */
    public Map<FileData, Set<FileData>> buildDependencyGraph(List<FileData> allFiles) {
        Map<String, FileData> fqcnToFileMap = new HashMap<>();
        Map<FileData, Set<FileData>> graph = new HashMap<>();

        // Build map: FQCN → FileData
        for (FileData file : allFiles) {
            for (ClassData cls : Optional.ofNullable(file.getClasses()).orElse(List.of())) {
                String fqcn = getFQCN(file.getFilePath(), cls.getName());
                fqcnToFileMap.put(fqcn, file);
            }
        }

        // Build graph edges based on imports
        for (FileData file : allFiles) {
            Set<FileData> dependencies = new HashSet<>();
            for (String importStmt : Optional.ofNullable(file.getImports()).orElse(List.of())) {
                FileData importedFile = fqcnToFileMap.get(importStmt);
                if (importedFile != null && !importedFile.equals(file)) {
                    dependencies.add(importedFile);
                    System.out.println("Adding edge from " + file.getFilePath() + " to " + importedFile.getFilePath());
                } else {
                    System.out.println("Import not resolved: " + importStmt);
                }
            }
            graph.put(file, dependencies);
        }

        return graph;
    }

    /**
     * Converts a file path and class name into fully qualified class name (FQCN).
     *
     * @param filePath the path of the file, e.g. "src/main/java/com/example/Foo.java"
     * @param className the name of the class, e.g. "Foo"
     * @return the FQCN, e.g. "com.example.Foo"
     */
    private String getFQCN(String filePath, String className) {
        String path = filePath;
        if (path.startsWith(SOURCE_ROOT)) {
            path = path.substring(SOURCE_ROOT.length());
        }
        if (path.endsWith(".java")) {
            path = path.substring(0, path.length() - 5);
        }
        String packagePath = path.replace("/", ".");
        return packagePath; // Class name already matches the last part of path
    }
}
