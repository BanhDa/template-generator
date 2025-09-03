package com.vn.tuantv.templategenerator.poi_tl;

import org.springframework.util.StringUtils;

public class ImageService {

    public static byte[] convertBase64ToBytes(String base64) {
        try {
            if (StringUtils.isEmpty(base64)) {
                return null;
            }

            if (base64.startsWith("data:image")) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
