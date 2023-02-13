package com.ecms.web.api.tokenservice.repository;

import java.text.ParseException;
import java.util.Date;

public interface CommonRepository {
    Date getSysDate() throws ParseException;
}
