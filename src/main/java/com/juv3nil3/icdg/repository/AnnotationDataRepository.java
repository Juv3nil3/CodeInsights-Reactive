package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.AnnotationData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AnnotationDataRepository extends R2dbcRepository<AnnotationData, Long> {

    Flux<AnnotationData> findAllByClassDataId(Long classDataId);
    Flux<AnnotationData> findAllByMethodDataId(Long methodDataId);
    Flux<AnnotationData> findAllByFieldDataId(Long fieldDataId);


}
