package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.MethodData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MethodDataRepository extends R2dbcRepository<MethodData, Long> {

    Flux<MethodData> findAllByClassDataId(Long classDataId);

}
