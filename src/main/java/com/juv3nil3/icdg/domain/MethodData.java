package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class MethodData {

        @Id
        private UUID id;


        private String name;
        private String comment;
        private UUID classDataId;

        @org.springframework.data.annotation.Transient
        private ClassData classData;

        @org.springframework.data.annotation.Transient
        private List<AnnotationData> annotations = new ArrayList<>();

        public UUID getId() {
                return id;
        }

        public void setId(UUID id) {
                this.id = id;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getComment() {
                return comment;
        }

        public void setComment(String comment) {
                this.comment = comment;
        }

        public UUID getClassDataId() {
                return classDataId;
        }

        public void setClassDataId(UUID classDataId) {
                this.classDataId = classDataId;
        }

        public ClassData getClassData() {
                return classData;
        }

        public void setClassData(ClassData classData) {
                this.classData = classData;
        }

        public List<AnnotationData> getAnnotations() {
                return annotations;
        }

        public void setAnnotations(List<AnnotationData> annotations) {
                this.annotations = annotations;
        }
}


