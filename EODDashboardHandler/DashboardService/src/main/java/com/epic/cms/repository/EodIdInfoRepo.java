/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:09 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EOD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EodIdInfoRepo extends JpaRepository<EOD, Long>, JpaSpecificationExecutor<EOD> {
    Optional<EOD> findById(Long id);

    @Query("SELECT EODID FROM EOD WHERE STATUS ='INIT' OR STATUS = 'EROR'")
    Long findByNextRunnindEodId();
}
