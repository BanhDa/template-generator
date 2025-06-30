package com.vn.tuantv.templategenerator.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vn.tuantv.templategenerator.utils.StringUtil;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class ExcelTemplateData {

  private Map<String, String> textData = new HashMap<>();

  private Map<String, List<String>> columnData = new HashMap<>();

  public boolean hasData() {
    return (textData != null && !textData.isEmpty());
  }

}
