/**
 * Author : rasintha_j
 * Date : 7/18/2023
 * Time : 8:21 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.bean.FileGenFailSuccessCountBean;
import com.epic.cms.model.bean.FileProcessFailSuccessCountBean;
import com.epic.cms.model.entity.EODPROCESSFLOW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessFailSuccessCountRepo extends JpaRepository<EODPROCESSFLOW, Integer>, JpaSpecificationExecutor<EODPROCESSFLOW> {
    @Query("SELECT new com.epic.cms.model.bean.FileGenFailSuccessCountBean(ps.SUCCESSCOUNT,ps.FAILEDCOUNT) FROM EODPROCESSSUMMERY ps INNER JOIN EODPROCESS ep ON ps.PROCESSID=ep.PROCESSID WHERE ps.EODID=?1 AND ep.EODMODULE = ?2 ORDER BY ps.STARTTIME")
    List<FileGenFailSuccessCountBean> findFileGenSummary(Long eodId, String eodFileGeneration);

    @Query("SELECT new com.epic.cms.model.bean.FileProcessFailSuccessCountBean(ps.SUCCESSCOUNT,ps.FAILEDCOUNT) FROM EODPROCESSSUMMERY ps INNER JOIN EODPROCESS ep ON ps.PROCESSID=ep.PROCESSID WHERE ps.EODID=?1 AND ep.EODMODULE = ?2 ORDER BY ps.STARTTIME")
    List<FileProcessFailSuccessCountBean> findFileProcessSummary(Long eodId, String eodFileProcess);
}
