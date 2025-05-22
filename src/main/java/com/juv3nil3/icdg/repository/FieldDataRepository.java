package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.FieldData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface FieldDataRepository extends R2dbcRepository<FieldData, UUID> {

    Flux<FieldData> findAllByClassDataId(UUID classDataId);

}
