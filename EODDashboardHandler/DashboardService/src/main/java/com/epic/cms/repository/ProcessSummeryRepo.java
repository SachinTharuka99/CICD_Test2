/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:05 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.bean.ProcessSummeryBean;
import com.epic.cms.model.entity.EODPROCESS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProcessSummeryRepo extends JpaRepository<EODPROCESS, Integer>, JpaSpecificationExecutor<EODPROCESS> {
    @Query("SELECT new com.epic.cms.model.bean.ProcessSummeryBean(ep.PROCESSID,ep.DESCRIPTION,eps.STATUS,eps.PROCESSPROGRESS) FROM EODPROCESSSUMMERY eps INNER JOIN EODPROCESS ep ON eps.PROCESSID=ep.PROCESSID INNER JOIN EODPROCESSFLOW epf ON ep.PROCESSID=epf.PROCESSID WHERE eps.EODID = ?1 AND ep.EODMODULE = ?2 ORDER BY epf.STEPID ASC")
    List<ProcessSummeryBean> findProcessSummeryListById(Long eodId, String eodEngine);
}
