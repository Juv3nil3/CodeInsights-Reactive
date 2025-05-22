package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.AnnotationData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface AnnotationDataRepository extends R2dbcRepository<AnnotationData, UUID> {

    Flux<AnnotationData> findAllByClassDataId(UUID classDataId);
    Flux<AnnotationData> findAllByMethodDataId(UUID methodDataId);
    Flux<AnnotationData> findAllByFieldDataId(UUID fieldDataId);


}
