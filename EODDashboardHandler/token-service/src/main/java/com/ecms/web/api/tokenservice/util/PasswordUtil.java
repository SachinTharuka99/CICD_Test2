package com.ecms.web.api.tokenservice.util;

import com.ecms.web.api.tokenservice.security.SHA256Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    @Autowired
    SHA256Algorithm sha256Algorithm;

    public boolean ValidatePassword(String password, String storedPassword) throws Exception {
        String hashPassword = sha256Algorithm.makeHash(password);

        if (hashPassword.equals(storedPassword))
            return true;

        return false;
    }

    public String getHashPassword(String password) throws Exception {
        String hashPassword = sha256Algorithm.makeHash(password);
        return hashPassword;
    }
}
