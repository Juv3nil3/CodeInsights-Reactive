package com.juv3nil3.icdg.service.dto;

import java.util.List;

public class DependencyGraphDTO {
    public static class Node {
        private String id;     // Usually file ID or file path
        private String label;  // Readable name (e.g. fileName or filePath)

        // Constructors, Getters, Setters


        public Node() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public static class Edge {
        private String source; // id of the source node
        private String target; // id of the dependent node

        // Constructors, Getters, Setters


        public Edge() {
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    private List<Node> nodes;
    private List<Edge> edges;

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
}
