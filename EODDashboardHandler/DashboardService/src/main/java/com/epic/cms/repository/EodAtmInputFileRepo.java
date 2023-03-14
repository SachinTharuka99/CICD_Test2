/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 10:25 PM
 * Project Name : ecms_eod_product - Copy
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODATMFILE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EodAtmInputFileRepo extends JpaRepository<EODATMFILE, String>, JpaSpecificationExecutor<EODATMFILE> {
    @Query("from EODATMFILE where EODID =?1")
    List<EODATMFILE> findAtmInputFileByEodId(Long eodId);
}
