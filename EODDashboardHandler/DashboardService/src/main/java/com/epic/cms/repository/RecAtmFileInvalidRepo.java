package com.epic.cms.repository;

import com.epic.cms.model.entity.RECATMFILEINVALID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecAtmFileInvalidRepo extends JpaRepository<RECATMFILEINVALID, Integer>, JpaSpecificationExecutor<RECATMFILEINVALID> {
    @Query(" from RECATMFILEINVALID where EODID = ?1")
    List<RECATMFILEINVALID> findRecAtmFileInvalidByEodId(Long eodId);
}