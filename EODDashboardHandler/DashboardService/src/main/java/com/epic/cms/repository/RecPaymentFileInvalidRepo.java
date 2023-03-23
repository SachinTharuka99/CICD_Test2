package com.epic.cms.repository;

import com.epic.cms.model.entity.RECPAYMENTFILEINVALID;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


@Repository
public interface RecPaymentFileInvalidRepo extends PagingAndSortingRepository<RECPAYMENTFILEINVALID, Integer>{
    List<RECPAYMENTFILEINVALID> findRECPAYMENTFILEINVALIDByEODID(Long eodId, Pageable pageable);
}