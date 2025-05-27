package com.vn.tuantv.templategenerator.template;

import com.vn.tuantv.templategenerator.dto.ExcelTemplateData;

public interface ExcelGenerator {

  void generate(String templateFilePath, String outputFilePath, ExcelTemplateData templateData);

}
