package com.epic.cms.repository;

import com.epic.cms.model.entity.RECATMFILEINVALID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecAtmFileInvalidRepo extends PagingAndSortingRepository<RECATMFILEINVALID, Integer> {
    Page<RECATMFILEINVALID> findRECATMFILEINVALIDByEODID(Long EODID, Pageable pageable);
}