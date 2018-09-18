package com.redhat.ads.openshift.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.SerializationUtils;

public class MD5Util {

    public  static String getMD5Hash(byte[] bytes) {
        return DigestUtils.md5Hex(bytes);
    }

    public static String getMD5Hash(Object object) {
        if (object instanceof String) {
            return DigestUtils.md5Hex((String) object);
        } else {
            return getMD5Hash(SerializationUtils.serialize(object));
        }
    }
}
