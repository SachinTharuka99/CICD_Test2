package com.epic.cms.repository;

import com.epic.cms.model.entity.EODOUTPUTFILES;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodOutputFileRepo extends JpaRepository<EODOUTPUTFILES, String>, JpaSpecificationExecutor<EODOUTPUTFILES> {
    @Query("from EODOUTPUTFILES where EODID =?1")
    List<EODOUTPUTFILES> findById(Long id);
}