/**
 * Author : lahiru_p
 * Date : 11/17/2022
 * Time : 10:31 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.ExposureFileBean;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class ExposureFileService {

    public StringBuilder getFileContent(ExposureFileBean bean, int recordCount) throws Exception {
        StringBuilder str = new StringBuilder();

        str.append(bean.getProduct().concat("-").concat(ISOUtil.zeropad(Integer.toString(recordCount), 6))).append("~");
        str.append(bean.getExternalRef()).append("~");
        str.append(bean.getCapitalOutstanding()).append("~");
        str.append(bean.getFacilityType()).append("~");
        str.append(bean.getBranch()).append("~");
        str.append(bean.getCurrency()).append("~");
        str.append(bean.getStatus()).append("~");

        //maturity date db format is YYMM. need to get yyyyMMdd with dd=last date of month
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        Date convertedDate = dateFormat.parse(bean.getMaturityDate() + "01"); //get first date of month
        Calendar c = Calendar.getInstance();
        c.setTime(convertedDate);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)); //get last date of month
        dateFormat = new SimpleDateFormat("yyyyMMdd");
        String lastDate = dateFormat.format(c.getTime()); //get formatted final date
        str.append(lastDate).append("~");
        str.append(bean.getCreditLimit());
        str.append(System.lineSeparator());

        return str;
    }
}
