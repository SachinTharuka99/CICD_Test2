/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 10:26 PM
 * Project Name : ecms_eod_product - Copy
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODPAYMENTFILE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodPaymentInputFileRepo  extends JpaRepository<EODPAYMENTFILE, String>, JpaSpecificationExecutor<EODPAYMENTFILE> {
    @Query("from EODPAYMENTFILE where EODID =?1")
    List<EODPAYMENTFILE> findPaymentInputFileByEodId(Long eodId);
}
