package com.epic.cms.dao;

import java.util.HashMap;

public interface OverLimitFeeDao {

    HashMap<String, StringBuffer> getOverLimitAcc() throws Exception;
}
