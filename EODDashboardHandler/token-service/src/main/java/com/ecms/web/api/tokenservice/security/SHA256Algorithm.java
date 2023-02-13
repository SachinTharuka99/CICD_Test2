package com.ecms.web.api.tokenservice.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;

@Component
public class SHA256Algorithm {

    public String makeHash(String input) throws Exception {
        try {
            Provider p = new BouncyCastleProvider();
            Security.addProvider(p);
            MessageDigest msd = MessageDigest.getInstance("SHA-256", p);
            byte dPassword[] = msd.digest(input.getBytes());
            return convertToHex(dPassword);
        } catch (Exception e) {
            return null;
        }
    }

    private static String convertToHex(byte[] data) {
        try {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                int halfbyte = (data[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9)) {
                        buf.append((char) ('0' + halfbyte));
                    } else {
                        buf.append((char) ('a' + (halfbyte - 10)));
                    }
                    halfbyte = data[i] & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        } catch (Exception e) {
            throw e;
        }
    }
}
