/**
 * Author : rasintha_j
 * Date : 3/22/2023
 * Time : 4:06 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODPROCESSFLOW;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EodProcessFlowRepo extends JpaRepository<EODPROCESSFLOW, Integer>, JpaSpecificationExecutor<EODPROCESSFLOW> {
    int countByPROCESSCATEGORYIDNotIn(Collection<Integer> PROCESSCATEGORYID);
}
