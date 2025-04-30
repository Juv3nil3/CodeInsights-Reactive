package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageDataRepository extends R2dbcRepository<PackageData, Long> {
}
