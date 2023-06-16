/**
 * Author : rasintha_j
 * Date : 3/18/2023
 * Time : 6:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;


import com.epic.cms.repository.EodOutputFileRepo;
import com.epic.cms.repository.StatementGenSummeryListRepo;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EODFileGenerationEngineDashboardService {

    @Autowired
    EodOutputFileRepo eodOutputFileRepo;

    @Autowired
    StatementGenSummeryListRepo genSummeryListRepo;

//    public List<EodOutputFileBean> getEodOutputFIleList(Long eodId) {
//        List<EodOutputFileBean> outputFileBeanList = new ArrayList<>();
//
//        try {
//            List<Object[]> eodOutputFilesList = eodOutputFileRepo.findEODOUTPUTFILESByEODID(eodId);
//
//            eodOutputFilesList.forEach(eod -> {
//                EodOutputFileBean eodBean = new EodOutputFileBean();
//                eodBean.setEodId(eodId);
//                eodBean.setCreatedTime((Date) eod[0]);
//                eodBean.setFileType((String) eod[1]);
//                eodBean.setNoOfRecords((int) ((BigDecimal) eod[3]).doubleValue());
//                eodBean.setFileName((String) eod[4]);
//                eodBean.setSubFolder((String) eod[5]);
//
//                outputFileBeanList.add(eodBean);
//            });
//        } catch (Exception e) {
//            throw e;
//        }
//        return outputFileBeanList;
//    }
//
//    public List<StatementGenSummeryBean> getStatementGenSummeryList(Long eodId) {
//        List<StatementGenSummeryBean> stmtGenSummeryList = new ArrayList<>();
//
//        try {
//            stmtGenSummeryList = genSummeryListRepo.findStmtGenSummeryListByEodId(eodId, Configurations.EOD_FILE_GENERATION);
//        } catch (Exception e) {
//            throw e;
//        }
//        return stmtGenSummeryList;
//    }
}
