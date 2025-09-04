package com.vn.tuantv.templategenerator.poi_tl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.deepoove.poi.policy.PictureRenderPolicy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class PoiTlGenerator {

    private static final String jsonData = "";

    private static final String templateFilePath = "";
    private static final String outputFilePath = "";

    private static final Configure configure = Configure.builder()
            // nếu bảng đơn giản có thể dùng LoopRowTableRenderPolicy để nhanh hơn
//            .bind("items", new HackLoopTableRenderPolicy())
            .bind("students", new LoopRowTableRenderPolicy(true))
            .bind("subjects", new LoopRowTableRenderPolicy(true))
//            .bind("user.avatar", new PictureRenderPolicy())
            .build();

    private final AppearanceProcessor appearanceProcessor;

    public void generate(String templateFilePath, String outputFilePath, Map<String, Object> data) {
        try {
            InputStream inputStream = new FileInputStream(templateFilePath);
            XWPFTemplate template = XWPFTemplate.compile(inputStream, configure);
            // Hậu xử lý: if-block + dọn đoạn/hàng rỗng (gói trong 1 lớp)
//            appearanceProcessor.process(template, data);
            template.render(data);
            template.writeToFile(outputFilePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
