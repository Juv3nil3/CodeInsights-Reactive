package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.ClassData;
import com.juv3nil3.icdg.domain.FileData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DependencyGraphBuilder {

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
                String fqcn = file.getFilePath().replace("/", ".") + "." + cls.getName();
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
                }
            }
            graph.put(file, dependencies);
        }

        return graph;
    }
}
