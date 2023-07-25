/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:09 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.bean.StatementGenSummeryBean;
import com.epic.cms.model.entity.EOD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EodIdInfoRepo extends JpaRepository<EOD, Long>, JpaSpecificationExecutor<EOD> {
    Optional<EOD> findById(Long id);

    @Query("SELECT EODID FROM EOD WHERE STATUS ='INIT' OR STATUS = 'EROR'")
    Long findByNextRunnindEodId();

    List<EOD> findAllByOrderByEODIDDesc();

    @Query("SELECT COUNT(ps.PROCESSID) AS count FROM EODPROCESS ps INNER JOIN EODPROCESSFLOW ep ON ps.PROCESSID = ep.PROCESSID WHERE ps.EODMODULE = ?1 AND ps.STATUS = ?2")
    int countAllByProcessCount(String eodEngine, String status);

}
