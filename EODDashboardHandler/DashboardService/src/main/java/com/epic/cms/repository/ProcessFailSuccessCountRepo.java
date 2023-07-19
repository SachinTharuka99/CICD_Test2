/**
 * Author : rasintha_j
 * Date : 7/18/2023
 * Time : 8:21 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODPROCESSFLOW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessFailSuccessCountRepo extends JpaRepository<EODPROCESSFLOW, Integer>, JpaSpecificationExecutor<EODPROCESSFLOW> {
    @Query("SELECT count(ps.PROCESSID) as count FROM EODPROCESSSUMMERY ps INNER JOIN EODPROCESS ep ON ps.PROCESSID=ep.PROCESSID WHERE ps.EODID=?1 AND ep.EODMODULE = ?2 AND ps.STATUS = ?3")
    int countAllBySummary(Long eodId, String eodFileGeneration, String status);
}
