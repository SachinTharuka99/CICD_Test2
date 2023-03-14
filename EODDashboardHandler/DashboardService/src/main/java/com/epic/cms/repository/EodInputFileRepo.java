package com.epic.cms.repository;

import com.epic.cms.model.entity.EODINPUTFILES;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EodInputFileRepo extends JpaRepository<EODINPUTFILES, Long>, JpaSpecificationExecutor<EODINPUTFILES> {
    Optional<EODINPUTFILES> findById(Long id);
}