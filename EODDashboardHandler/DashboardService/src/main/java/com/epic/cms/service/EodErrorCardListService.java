/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 11:26 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.EodErrorCardBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EodErrorCardListService {
    List<EodErrorCardBean> getEodErrorCardList(Long eodId) throws Exception;
}
