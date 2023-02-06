/**
 * Author : yasiru_l
 * Date : 12/15/2022
 * Time : 9:39 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CardBillingInfoBean;
import com.epic.cms.model.bean.EomCardBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetEOMCardListRowMapper implements RowMapper<EomCardBean> {
    @Override
    public EomCardBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        EomCardBean eomCardBean = new EomCardBean();
        eomCardBean.setAccNo(rs.getString("ACCOUNTNO"));
        eomCardBean.setCardNo(new StringBuffer(rs.getString("CARDNUMBER")));
        eomCardBean.setAccStatus(rs.getString("STATUS"));
        eomCardBean.setInterestRate(rs.getDouble("INTERESTRATE"));
        eomCardBean.setInterestPeriod(rs.getInt("INTERESTPERIODVALUE"));
        return eomCardBean;
    }
}
