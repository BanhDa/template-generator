package com.vn.tuantv.templategenerator.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class WordTemplateData {

  private Map<String, String> textData = new HashMap<>();

  private Map<String, List<String>> columnData = new HashMap<>();

  public boolean hasData() {
    return (textData != null && !textData.isEmpty());
  }
}
