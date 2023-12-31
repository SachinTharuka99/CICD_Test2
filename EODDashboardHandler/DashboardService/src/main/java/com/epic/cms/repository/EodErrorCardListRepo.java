/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 11:26 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODERRORCARDS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.PagingAndSortingRepository;



@Repository
public interface EodErrorCardListRepo extends PagingAndSortingRepository<EODERRORCARDS, Integer>{

    Page<EODERRORCARDS> findAllByEODID(long EODID,Pageable pageable);
}
