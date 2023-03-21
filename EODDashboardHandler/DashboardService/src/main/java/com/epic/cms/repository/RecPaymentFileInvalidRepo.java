package com.epic.cms.repository;

import com.epic.cms.model.entity.RECPAYMENTFILEINVALID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface RecPaymentFileInvalidRepo extends JpaRepository<RECPAYMENTFILEINVALID, Integer>, JpaSpecificationExecutor<RECPAYMENTFILEINVALID> {
    Page<RECPAYMENTFILEINVALID> findRECPAYMENTFILEINVALIDByEODID(Long eodId, Pageable page);
}