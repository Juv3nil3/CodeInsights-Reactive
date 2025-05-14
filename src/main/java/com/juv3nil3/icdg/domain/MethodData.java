package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MethodData {

        @Id
        private Long id;

        private String name;
        private String comment;
        private Long classDataId;

        @Transient
        private ClassData classData;

        @Transient
        private List<AnnotationData> annotations = new ArrayList<>();

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
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

        public Long getClassDataId() {
                return classDataId;
        }

        public void setClassDataId(Long classDataId) {
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


