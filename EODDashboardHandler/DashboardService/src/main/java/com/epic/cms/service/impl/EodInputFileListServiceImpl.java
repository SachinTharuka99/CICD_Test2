/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 2:48 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.EodInputFileBean;
import com.epic.cms.model.entity.*;
import com.epic.cms.repository.*;
import com.epic.cms.service.EodInputFileListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class EodInputFileListServiceImpl implements EodInputFileListService {

    @Autowired
    EodAtmInputFileRepo eodAtmInputFileRepo;

    @Autowired
    EodMasterInputFileRepo eodMasterInputFileRepo;

    @Autowired
    EodVisaInputFileRepo eodVisaInputFileRepo;

    @Autowired
    EodPaymentInputFileRepo eodPaymentInputFileRepo;

    @Override
    public List<Object> getEodInputFIleList(Long eodId) {
        List<Object> eodInputFileObjectList = new ArrayList<>();

        try {
            List<EODATMFILE> atmInputFileList = eodAtmInputFileRepo.findAtmInputFileByEodId(eodId);
            List<EODPAYMENTFILE> paymentInputFileList = eodPaymentInputFileRepo.findPaymentInputFileByEodId(eodId);
            List<EODMASTERFILE> masterInputFileList = eodMasterInputFileRepo.findMasterInputFileByEodId(eodId);
            List<EODVISAFILE> visaInputFileList = eodVisaInputFileRepo.findVisaInputFileByEodId(eodId);


            atmInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("ATM");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            paymentInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("PAYMENT");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            masterInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("VISA");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            visaInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("MASTER");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });
        } catch (Exception e) {
            dashboardErrorLogger.error("Get Eod Input FIle List Error", e);
            throw e;
        }
        return eodInputFileObjectList;
    }
}
