package com.epic.cms.repository;

import com.epic.cms.model.entity.EODOUTPUTFILES;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodOutputFileRepo extends JpaRepository<EODOUTPUTFILES, String>, JpaSpecificationExecutor<EODOUTPUTFILES> {
    List<EODOUTPUTFILES> findEODOUTPUTFILESByEODID(Long id);
}