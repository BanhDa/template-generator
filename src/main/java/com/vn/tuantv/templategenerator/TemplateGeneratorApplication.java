package com.vn.tuantv.templategenerator;

import com.vn.tuantv.templategenerator.dto.ExcelTemplateData;
import com.vn.tuantv.templategenerator.dto.WordTemplateData;
import com.vn.tuantv.templategenerator.poi_tl.ObjectUtils;
import com.vn.tuantv.templategenerator.poi_tl.PoiTlGenerator;
import com.vn.tuantv.templategenerator.template.ExcelGenerator;
import com.vn.tuantv.templategenerator.template.WordGenerator;
import com.vn.tuantv.templategenerator.utils.JsonService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
@AllArgsConstructor
public class TemplateGeneratorApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(TemplateGeneratorApplication.class, args);
  }

  private final ExcelGenerator excelGenerator;
  private final WordGenerator wordGenerator;
  private final PoiTlGenerator poiTlGenerator;

  @Override
  public void run(String... args) {
//    String excelTemplateFilePath = "excel_template.xlsx";
//    String excelOutputFilePath = "excel_output.xlsx";
//    String excelDataJsonFilePath = "excel_data_json.json";
//    ExcelTemplateData excelTemplateData = JsonService.readJsonFile(excelDataJsonFilePath, ExcelTemplateData.class);
//    System.out.println("excelTemplateData: " + JsonService.toJsonString(excelTemplateData));
//    excelGenerator.generate(excelTemplateFilePath, excelOutputFilePath, excelTemplateData);
//
//
//    String wordTemplateFilePath = "word_template.docx";
//    String wordOutputFilePath = "word_output.docx";
//    String wordDataJsonFilePath = "word_data_json.json";
//    WordTemplateData wordTemplateData = JsonService.readJsonFile(wordDataJsonFilePath, WordTemplateData.class);
//    System.out.println("wordTemplateData: " + JsonService.toJsonString(excelTemplateData));
//    wordGenerator.generate(wordTemplateFilePath, wordOutputFilePath, wordTemplateData);

      String wordTemplateFilePath = "temp/tttd.docx";
      String wordOutputFilePath = "temp/tttd_out.docx";
      String wordDataJsonFilePath = "temp/tttd.json";
      DocTemplateData docTemplateData = JsonService.readJsonFile(wordDataJsonFilePath, DocTemplateData.class);
      System.out.println("docTemplateData: " + JsonService.toJsonString(docTemplateData));
      assert docTemplateData != null;
      poiTlGenerator.generate(wordTemplateFilePath, wordOutputFilePath, docTemplateData.getData());
  }

  @Data
  private static class DocTemplateData {
      private Map<String, Object> data;
  }
}
