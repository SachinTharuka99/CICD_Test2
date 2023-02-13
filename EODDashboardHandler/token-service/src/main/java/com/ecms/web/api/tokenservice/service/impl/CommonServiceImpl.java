package com.ecms.web.api.tokenservice.service.impl;

import com.ecms.web.api.tokenservice.repository.CommonRepository;
import com.ecms.web.api.tokenservice.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private CommonRepository commonRepository;

    @Override
    public Date getSysDate() throws ParseException {
        Date sysDateTime = null;
        sysDateTime = commonRepository.getSysDate();
        return sysDateTime;
    }
}
