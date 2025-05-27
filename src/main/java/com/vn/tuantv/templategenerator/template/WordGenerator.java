package com.vn.tuantv.templategenerator.template;

import com.vn.tuantv.templategenerator.dto.WordTemplateData;

public interface WordGenerator {

  void generate(String templateFilePath, String outputFilePath, WordTemplateData wordTemplateData);
}
