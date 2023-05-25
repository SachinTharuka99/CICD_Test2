package com.epic.cms.repository;

import com.epic.cms.model.bean.EodOutputFileBean;
import com.epic.cms.model.bean.StatementGenSummeryBean;
import com.epic.cms.model.entity.EODOUTPUTFILES;
import com.epic.cms.model.entity.EODOUTPUTFILESPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EodOutputFileRepo extends JpaRepository<EODOUTPUTFILES, EODOUTPUTFILESPK>, JpaSpecificationExecutor<EODOUTPUTFILES> {
//    List<EODOUTPUTFILES> findEODOUTPUTFILESByEODID(Long id);

//    @Query("SELECT new com.epic.cms.model.bean.EodOutputFileBean(ep.CREATEDTIME,ep.FILETYPE,ep.EODID,ep.NOOFRECORDS,ep.FILENAME,ep.SUBFOLDER) FROM EODOUTPUTFILES ep WHERE ep.EODID=?1")
//    List<EodOutputFileBean> findEODOUTPUTFILESByEODID(Long eodId);


    @Query(value = "SELECT CREATEDTIME,FILETYPE,EODID,NOOFRECORDS,FILENAME,SUBFOLDER FROM EODOUTPUTFILES WHERE EODID=?1",nativeQuery = true)
    List<Object[]> findEODOUTPUTFILESByEODID(Long eodId);
}