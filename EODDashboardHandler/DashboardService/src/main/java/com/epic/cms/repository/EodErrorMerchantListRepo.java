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
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.PagingAndSortingRepository;


import java.util.List;

@Repository
public interface EodErrorMerchantListRepo extends PagingAndSortingRepository<EODERRORMERCHANT, Integer>{
    Page<EODERRORMERCHANT> findEODERRORMERCHANTByEODID(Long eodId, Pageable pageable);
}
