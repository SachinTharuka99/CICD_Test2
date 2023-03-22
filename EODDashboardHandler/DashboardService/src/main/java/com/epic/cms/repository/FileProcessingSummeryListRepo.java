/**
 * Author : rasintha_j
 * Date : 3/21/2023
 * Time : 3:07 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.bean.StatementGenSummeryBean;
import com.epic.cms.model.entity.EODPROCESSSUMMERY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileProcessingSummeryListRepo extends JpaRepository<EODPROCESSSUMMERY, Integer>, JpaSpecificationExecutor<EODPROCESSSUMMERY> {
    @Query("SELECT new com.epic.cms.model.bean.StatementGenSummeryBean(ep.PROCESSID,ep.DESCRIPTION,ps.STATUS,ps.PROCESSPROGRESS,ps.SUCCESSCOUNT,ps.FAILEDCOUNT) FROM EODPROCESSSUMMERY ps INNER JOIN EODPROCESS ep ON ps.PROCESSID=ep.PROCESSID WHERE ps.EODID=?1 AND ps.EODMODULE = ?2 ORDER BY ps.STARTTIME")
    List<StatementGenSummeryBean> findProcessingSummeryListByEodId(Long eodId, String eodFileProcessing);
}
