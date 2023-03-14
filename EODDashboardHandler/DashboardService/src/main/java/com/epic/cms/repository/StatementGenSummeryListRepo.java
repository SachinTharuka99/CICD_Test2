/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 9:26 AM
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
public interface StatementGenSummeryListRepo extends JpaRepository<EODPROCESSSUMMERY, Integer>, JpaSpecificationExecutor<EODPROCESSSUMMERY> {
    @Query("SELECT DISTINCT new com.epic.cms.model.bean.StatementGenSummeryBean(ep.PROCESSID,ep.DESCRIPTION,ps.STATUS,ps.PROCESSPROGRESS,ps.SUCCESSCOUNT,ps.FAILEDCOUNT) FROM EODPROCESSSUMMERY ps INNER JOIN EODPROCESS ep ON ps.PROCESSID=ep.PROCESSID WHERE ps.EODID=?1")
    List<StatementGenSummeryBean> findStmtGenSummeryListByEodId(Long eodId);
}
