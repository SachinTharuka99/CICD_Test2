/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 9:31 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODERRORMERCHANT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodErrorMerchantListRepo extends JpaRepository<EODERRORMERCHANT, Integer>, JpaSpecificationExecutor<EODERRORMERCHANT> {
    Page<EODERRORMERCHANT> findEODERRORMERCHANTByEODID(Long eodId, Pageable page);
}
