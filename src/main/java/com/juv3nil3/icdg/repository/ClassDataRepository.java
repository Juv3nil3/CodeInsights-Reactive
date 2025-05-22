package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.ClassData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassDataRepository extends R2dbcRepository<ClassData, UUID> {

    Flux<ClassData> findAllByFileDataId(UUID fileDataId);


}
