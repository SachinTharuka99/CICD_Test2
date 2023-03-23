package com.epic.cms.repository;

import com.epic.cms.model.entity.RECATMFILEINVALID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface RecAtmFileInvalidRepo extends PagingAndSortingRepository<RECATMFILEINVALID, Integer> {

    List<RECATMFILEINVALID> findRECATMFILEINVALIDByEODID(Long EODID, Pageable pageable);
}