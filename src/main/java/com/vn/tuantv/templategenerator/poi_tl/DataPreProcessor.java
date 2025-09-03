package com.vn.tuantv.templategenerator.poi_tl;

import com.deepoove.poi.data.Pictures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coobird.thumbnailator.Thumbnails;

public class DataPreProcessor {

    public static Map<String, Object> normalize(Map<String, Object> src) {
        Map<String, Object> map = new HashMap<>(src);

        // 1) Ảnh: hỗ trợ dataURL / URL / base64 -> resize -> PictureRenderData
        Object customerObj = map.get("customer");
        if (customerObj instanceof Map) {
            Map<String, Object> customer = (Map<String, Object>) customerObj;
            Object photo = customer.get("photo");
            if (photo != null) {
                byte[] raw = toImageBytes(photo.toString());
                if (raw != null && raw.length > 0) {
                    byte[] resized = downscale(raw, 240, 240, 0.85f); // giảm dung lượng
                    customer.put("photo", Pictures.ofBytes(resized).size(120, 120).create());
                } else customer.remove("photo");
            }
        }

        // 2) Định dạng giá (nếu muốn hiển thị dạng text)
        Object itemsObj = map.get("items");
        if (itemsObj instanceof List<?>) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;
            for (Map<String, Object> it : items) {
                Object price = it.get("price");
                if (price instanceof Number) {
                    it.put("price", String.format("%,d ₫", ((Number) price).longValue()));
                }
            }
        }
        return map;
    }

    private static byte[] toImageBytes(String v) {
        try {
            if (v.startsWith("data:image")) {
                return Base64.getDecoder().decode(v.substring(v.indexOf(',') + 1));
            } else if (v.startsWith("http")) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder(URI.create(v)).GET().build();
                HttpResponse<byte[]> rsp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
                return rsp.statusCode() == 200 ? rsp.body() : null;
            } else {
                return Base64.getDecoder().decode(v);
            }
        } catch (Exception e) { return null; }
    }

    private static byte[] downscale(byte[] input, int maxW, int maxH, float quality) {
        try (ByteArrayInputStream bin = new ByteArrayInputStream(input);
             ByteArrayOutputStream bout = new ByteArrayOutputStream(Math.min(input.length, 256 * 1024))) {

            BufferedImage img = ImageIO.read(bin);
            if (img == null) return input;

            BufferedImage out = Thumbnails.of(img).size(maxW, maxH).outputQuality(quality).asBufferedImage();
            ImageIO.write(out, "jpg", bout); // jpg thường nhỏ hơn png cho ảnh chụp
            return bout.toByteArray();
        } catch (IOException e) {
            return input;
        }
    }

}
