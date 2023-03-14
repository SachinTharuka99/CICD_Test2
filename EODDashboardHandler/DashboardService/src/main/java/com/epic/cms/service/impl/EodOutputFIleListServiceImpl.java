/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 2:38 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.EodOutputFileBean;
import com.epic.cms.model.entity.EODOUTPUTFILES;
import com.epic.cms.repository.EodOutputFileRepo;
import com.epic.cms.service.EodOutputFIleListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class EodOutputFIleListServiceImpl implements EodOutputFIleListService {

    @Autowired
    EodOutputFileRepo eodOutputFileRepo;

    @Override
    public List<EodOutputFileBean> getEodOutputFIleList(Long eodId) {
        List<EodOutputFileBean> outputFileBeanList = new ArrayList<>();

        try {
            List<EODOUTPUTFILES> eodOutputFilesList = eodOutputFileRepo.findById(eodId);

            eodOutputFilesList.forEach(eod -> {
                EodOutputFileBean eodBean = new EodOutputFileBean();
                eodBean.setEodId(eodId);
                eodBean.setCreatedTime(eod.getCREATEDTIME());
                eodBean.setFileType(eod.getFILETYPE());
                eodBean.setNoOfRecords(eod.getNOOFRECORDS());
                eodBean.setFileName(eod.getFILENAME());
                eodBean.setSubFolder(eod.getSUBFOLDER());

                outputFileBeanList.add(eodBean);
            });
        } catch (Exception e) {
            dashboardErrorLogger.error("Get Eod OutputFIle List Error", e);
           throw e;
        }
        return outputFileBeanList;
    }
}
