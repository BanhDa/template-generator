package com.vn.tuantv.templategenerator.poi_tl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.deepoove.poi.policy.PictureRenderPolicy;
import com.vn.tuantv.templategenerator.utils.JsonService;
import lombok.Data;
import org.springframework.asm.TypeReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class PoiTlGenerator {

    private static final String jsonData = "";

    private static final String templateFilePath = "";
    private static final String outputFilePath = "";

    private static final Configure configure = Configure.builder()
            // nếu bảng đơn giản có thể dùng LoopRowTableRenderPolicy để nhanh hơn
//            .bind("items", new HackLoopTableRenderPolicy())
            .bind("items", new LoopRowTableRenderPolicy())
            .bind("customer.photo", new PictureRenderPolicy())
            .build();

    @lombok.Data
    public static class Data {
        private Map<String, Object> data;
    }

    public static void main(String[] args) {
        try {
            Map<String, Object> data = JsonService.readValue(jsonData, Data.class).getData();
            generate(templateFilePath, outputFilePath, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void generate(String templateFilePath, String outputFilePath, Map<String, Object> data) throws Exception {
        try {
            InputStream inputStream = new FileInputStream(templateFilePath);
            XWPFTemplate template = XWPFTemplate.compile(inputStream, configure).render(data);
            // Hậu xử lý: if-block + dọn đoạn/hàng rỗng (gói trong 1 lớp)
            OnePassPostProcessor.process(template, data);
            template.writeToFile(outputFilePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
