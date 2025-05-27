package com.vn.tuantv.templategenerator.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ExcelTemplateData {

  private Map<String, String> textData = new HashMap<>();

  private Map<String, List<Map<String, String>>> tableData = new HashMap<>();

  public boolean hasData() {
    return (textData != null && !textData.isEmpty())
        || (tableData != null && !tableData.isEmpty());
  }
}
