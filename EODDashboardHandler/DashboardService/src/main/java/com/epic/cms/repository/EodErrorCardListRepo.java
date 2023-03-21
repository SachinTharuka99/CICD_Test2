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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EodErrorCardListRepo extends JpaRepository<EODERRORCARDS, Integer>, JpaSpecificationExecutor<EODERRORCARDS> {
    Page<EODERRORCARDS> findEODERRORCARDSByEODID(Long eodId, Pageable page);
}
