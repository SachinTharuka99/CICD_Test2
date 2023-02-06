/**
 * Created By Lahiru Sandaruwan
 * Date : 10/24/2022
 * Time : 10:48 PM
 * Project Name : ecms_eod_engine
 * Topic :
 */

package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.DailyInterestBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DailyInterestRowMapper implements RowMapper<DailyInterestBean> {

    @Override
    public DailyInterestBean mapRow(ResultSet result, int rowNum) throws SQLException {
        DailyInterestBean txnBean = new DailyInterestBean();
        txnBean.setNoOfDays(result.getInt("DATEDIFF")); // date difference from settlement date to end date(current eod date)
        txnBean.setAmount(result.getDouble("TOTALPAY")); // sum of amount for given date
        return txnBean;
    }
}
