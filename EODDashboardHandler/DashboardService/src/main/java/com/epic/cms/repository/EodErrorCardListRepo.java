/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 11:26 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODERRORCARDS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodErrorCardListRepo extends JpaRepository<EODERRORCARDS, Integer>, JpaSpecificationExecutor<EODERRORCARDS> {
    @Query("from EODERRORCARDS where EODID =?1")
    List<EODERRORCARDS> findEodErrorCardByEodId(Long eodId);
}
