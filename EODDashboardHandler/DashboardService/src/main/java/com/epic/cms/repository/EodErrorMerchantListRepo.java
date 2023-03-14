/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 9:31 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODERRORMERCHANT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodErrorMerchantListRepo extends JpaRepository<EODERRORMERCHANT, Integer>, JpaSpecificationExecutor<EODERRORMERCHANT> {
    @Query("from EODERRORMERCHANT where EODID = ?1")
    List<EODERRORMERCHANT> findEodErrorMerchantByEodId(Long eodId);
}
