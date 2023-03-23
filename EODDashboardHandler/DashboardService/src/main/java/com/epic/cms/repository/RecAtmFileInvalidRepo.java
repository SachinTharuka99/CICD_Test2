package com.epic.cms.repository;

import com.epic.cms.model.entity.RECATMFILEINVALID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecAtmFileInvalidRepo extends JpaRepository<RECATMFILEINVALID, Integer>, JpaSpecificationExecutor<RECATMFILEINVALID> {
    Page<RECATMFILEINVALID> findRECATMFILEINVALIDByEODID(Long eodId, Pageable page);
    List<RECATMFILEINVALID> findRECATMFILEINVALIDByEODID(Long eodId);
}