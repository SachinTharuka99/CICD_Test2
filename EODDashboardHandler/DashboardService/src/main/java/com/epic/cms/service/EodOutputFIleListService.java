/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 2:37 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.EodOutputFileBean;
import org.springframework.stereotype.Service;

import java.util.List;

public interface EodOutputFIleListService {
    List<EodOutputFileBean> getEodOutputFIleList(Long eodId) throws Exception;
}
