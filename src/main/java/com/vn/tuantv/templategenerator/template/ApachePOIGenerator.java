package com.vn.tuantv.templategenerator.template;

import com.vn.tuantv.templategenerator.dto.ExcelTemplateData;
import com.vn.tuantv.templategenerator.utils.StringUtil;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ApachePOIGenerator implements ExcelGenerator {

  public static Pattern pattern = Pattern.compile("\\$\\{(.*?)}");

  private final DataTranformer dataTranformer;

  @Override
  public void generate(String templateFilePath, String outputFilePath,
      ExcelTemplateData templateData) {
    Long startTime = System.currentTimeMillis();
    try {
      FileInputStream fis = new FileInputStream(templateFilePath);
      Workbook workbook = new XSSFWorkbook(fis);

      Sheet sheet = workbook.getSheetAt(0);

      StringBuilder stringBuilder = new StringBuilder();

      Map<String, Map<String, List<String>>> tableData = dataTranformer.transformTableData(
              templateData.getColumnData()
      );

      // fill table data
      fillTables(sheet, tableData, stringBuilder);
      // fill text data
      fillPlaceholders(sheet, templateData.getTextData(), stringBuilder);

      try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
        workbook.write(fos);
      }
      Long endTime = System.currentTimeMillis();
      System.out.println("Generated excel file: " + outputFilePath + " take time: " + (endTime - startTime) + "ms");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void fillTables(Sheet sheet, Map<String, Map<String, List<String>>> tableData, StringBuilder stringBuilder) {
    if (tableData == null || tableData.isEmpty()) {
      return;
    }
    tableData.forEach((tableName, listData) -> {
      try {
        fillTable(sheet, tableName, listData, stringBuilder);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private int getTotalNumberOfRow(Map<String, List<String>> dataList) {
    if (dataList == null || dataList.isEmpty()) {
      return 0;
    }
    int rowNumber = 0;
    for (Map.Entry<String, List<String>> entry : dataList.entrySet()) {
      if (entry.getValue() != null && !entry.getValue().isEmpty()) {
        if (rowNumber < entry.getValue().size()) {
          rowNumber = entry.getValue().size();
        }
      }
    }

    return rowNumber;
  }

  private void fillTable(Sheet sheet, String tableName, Map<String, List<String>> dataList, StringBuilder stringBuilder) {
    int startRow = findRowWithPlaceholder(sheet, "${" + tableName);

    Row templateRow = sheet.getRow(startRow);
    int totalNumberOfRow = getTotalNumberOfRow(dataList);
    insertRows(sheet, startRow, totalNumberOfRow - 1);

    Map<String, String> rowData = new HashMap<>();
    for (int rowIndex = 0; rowIndex < totalNumberOfRow; rowIndex++) {

      String value;
      for (Map.Entry<String, List<String>> entry : dataList.entrySet()) {
        String fieldName = entry.getKey();
        List<String> values = entry.getValue();
        if (!CollectionUtils.isEmpty(values) && rowIndex < values.size()) {
          value = values.get(rowIndex);
        } else {
          value = StringUtil.EMPTY;
        }
        rowData.put(tableName + StringUtil.DOT + fieldName, value);
      }
      Row newRow = sheet.getRow(startRow + rowIndex);
      for (Cell cell : templateRow) {
        Cell newCell = newRow.createCell(cell.getColumnIndex(), cell.getCellType());
        String cellValue = getCellString(cell);
        newCell.setCellValue(fillPlaceholders(cellValue, rowData, stringBuilder));
        copyCellStyle(cell, newCell);
      }
    }
  }

  private void fillPlaceholders(Sheet sheet, Map<String, String> textData, StringBuilder stringBuilder) {
    for (Row row : sheet) {
      for (Cell cell : row) {
        String cellValue = getCellString(cell);
        if (cellValue != null) {
          cell.setCellValue(fillPlaceholders(cellValue, textData, stringBuilder));
        }
      }
    }
  }

  private String fillPlaceholders(String text, Map<String, String> data, StringBuilder stringBuilder) {
    stringBuilder.setLength(0);
    // Tìm tất cả các chuỗi có dạng ${...}
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      String key = matcher.group(1); // Lấy ra nội dung bên trong ${...}
      String value = data.getOrDefault(key, ""); // Nếu không có key thì thay bằng chuỗi rỗng
      matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(stringBuilder);

    String result = stringBuilder.toString();
    stringBuilder.setLength(0);
    return result;
  }

  private int findRowWithPlaceholder(Sheet sheet, String placeholder) {
    for (Row row : sheet) {
      for (Cell cell : row) {
        String val = getCellString(cell);
        if (val != null && val.contains(placeholder)) {
          return row.getRowNum();
        }
      }
    }
    return -1;
  }

  private void insertRows(Sheet sheet, int startRow, int numberOfRows) {
    int lastRowNum = sheet.getLastRowNum();
    sheet.shiftRows(startRow, lastRowNum, numberOfRows, true, false);
    for (int i = 0; i < numberOfRows; i++) {
      sheet.createRow(startRow + i);
    }
  }

  private String getCellString(Cell cell) {
    if (cell == null) return null;
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> String.valueOf(cell.getNumericCellValue());
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> cell.getCellFormula();
      default -> StringUtil.EMPTY;
    };
  }

  private void copyCellStyle(Cell source, Cell target) {
    if (source == null || target == null) return;
    CellStyle newStyle = source.getSheet().getWorkbook().createCellStyle();
    newStyle.cloneStyleFrom(source.getCellStyle());
    target.setCellStyle(newStyle);
  }
}
