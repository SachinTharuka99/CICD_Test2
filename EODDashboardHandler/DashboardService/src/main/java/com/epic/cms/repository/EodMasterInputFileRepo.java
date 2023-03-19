/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 10:26 PM
 * Project Name : ecms_eod_product - Copy
 */

package com.epic.cms.repository;

import com.epic.cms.model.entity.EODMASTERFILE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodMasterInputFileRepo extends JpaRepository<EODMASTERFILE, String>, JpaSpecificationExecutor<EODMASTERFILE> {
    List<EODMASTERFILE> findEODMASTERFILEByEODID(Long eodId);
}
