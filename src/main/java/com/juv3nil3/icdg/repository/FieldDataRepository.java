package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.FieldData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldDataRepository extends R2dbcRepository<FieldData,Long> {
}
