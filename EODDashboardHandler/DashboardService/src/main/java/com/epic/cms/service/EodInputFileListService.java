/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:10 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import org.springframework.stereotype.Service;

import java.util.List;

public interface EodInputFileListService {
    List<Object> getEodInputFIleList(Long eodId) throws Exception;
}
