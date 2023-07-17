/**
 * Author : rasintha_j
 * Date : 7/13/2023
 * Time : 9:59 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.PaymentFileDataBean;

import java.util.ArrayList;

public interface DCFFileClearingDao {
    public ArrayList<String> getNameFields(String fileType) throws Exception;
    public ArrayList<PaymentFileDataBean> getDCFFileList() throws Exception;
    public int insertToRECDCFINPUTROWDATA(String fileid, int noofrecords, String recordContent, int batchNo, String fieldType) throws Exception;
    public void dcfFileSplitter() throws Exception;
    public int updateEODDCFFILE(int noofrecords, String status, String fileid) throws Exception;
    public int updateEODDCFFILE(String fileid) throws Exception;
}
