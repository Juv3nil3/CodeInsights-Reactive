package com.juv3nil3.icdg.domain.elasticsearch;

import lombok.Builder;

import java.util.List;

@Builder
public class MethodDataDocument {

    private String methodName;
    private String returnType;
    private List<String> parameters;
    private String modifiers;
    private String documentation;

    // Constructors
    public MethodDataDocument() {}

    public MethodDataDocument(String methodName, String returnType, List<String> parameters, String modifiers, String documentation) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.modifiers = modifiers;
        this.documentation = documentation;
    }

    // Getters and Setters
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    // Manual builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String methodName;
        private String returnType;
        private List<String> parameters;
        private String modifiers;
        private String documentation;

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder parameters(List<String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder modifiers(String modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }

        public MethodDataDocument build() {
            return new MethodDataDocument(methodName, returnType, parameters, modifiers, documentation);
        }
    }
}
