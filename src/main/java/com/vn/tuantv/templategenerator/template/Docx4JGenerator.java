package com.vn.tuantv.templategenerator.template;

import com.vn.tuantv.templategenerator.dto.WordTemplateData;
import com.vn.tuantv.templategenerator.utils.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBElement;
import lombok.AllArgsConstructor;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@AllArgsConstructor
public class Docx4JGenerator implements WordGenerator {

  @Override
  public void generate(String templateFilePath, String outputFilePath, WordTemplateData wordTemplateData) {
    try {
      long startTime = System.currentTimeMillis();
      gen(templateFilePath, outputFilePath, wordTemplateData);
      long endTime = System.currentTimeMillis();
      System.out.println("Generated word file: " + outputFilePath + " take time: " + (endTime - startTime) + "ms");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void gen(String templateFilePath, String outputFilePath, WordTemplateData wordTemplateData)
      throws Exception {
    if (!StringUtils.hasText(templateFilePath)
        || !StringUtils.hasText(outputFilePath)) {
      return;
    }

    if (wordTemplateData == null || !wordTemplateData.hasData()) {
      return;
    }

    File resource = new File(templateFilePath);
    // Load file Word mẫu
    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(resource);

    MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

    fillTables(wordMLPackage.getMainDocumentPart(), wordTemplateData);
    fillParaGraphs(documentPart, wordTemplateData);

    wordMLPackage.save(new File(outputFilePath));
  }


  static Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
  private void fillParaGraphs(MainDocumentPart mainDocumentPart, WordTemplateData wordTemplateData) {
    Map<String, String> textData = wordTemplateData.getTextData();
    if (textData == null || textData.isEmpty()) {
      return;
    }
    // khai báo StringBuilder để dùng, tránh việc khởi tạo trong vòng for -> tăng bộ nhớ heap
    // cần phải clear sau mỗi vòng for
    StringBuilder stringBuilder = new StringBuilder();
    for (Object obj : mainDocumentPart.getContent()) {
      if (obj instanceof P paragraph) {
        fillParaGraph(paragraph, textData, stringBuilder);
      }
      //clear dữ liệu cho vòng lặp mới
      stringBuilder.setLength(0);
    }
  }

  private void fillParaGraph(P paragraph, Map<String, String> textData, StringBuilder stringBuilder) {
    String cellValue = getValueFromParagraph(paragraph.getContent(), stringBuilder);
    if (!StringUtils.hasText(cellValue)) {
      return;
    }

    stringBuilder.setLength(0);
    Matcher matcher = pattern.matcher(cellValue);
    while (matcher.find()) {
      String key = matcher.group(1); // Lấy tên biến ${...}
      String replacement = textData.getOrDefault(key, StringUtil.EMPTY); // Thay bằng giá trị tương ứng hoặc chuỗi rỗng
      matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(stringBuilder);

    List<Object> paraContents = paragraph.getContent();
    for (Object paraObj : paraContents) {
      // fill dữ liệu, nếu fill thành công thì không thực hiện nữa
      if (this.fillTexData(paraObj, stringBuilder, paraContents)) {
        break;
      }
    }
  }

  private void fillTables(MainDocumentPart mainDocumentPart, WordTemplateData wordTemplateData) throws Exception {
    // lấy tất cả các table trong file
    List<Object> tables = mainDocumentPart.getJAXBNodesViaXPath("//w:tbl", true);

    // xử lý từng table
    for (Object tblObj : tables) {
      Tbl table = (Tbl) ((JAXBElement<?>) tblObj).getValue();
      fillTable(table, wordTemplateData);
    }
  }

  private void fillTable(Tbl table, WordTemplateData wordTemplateData) {
    Map<String, List<Map<String, String>>> tableData = wordTemplateData.getTableData();
    List<Object> rows = table.getContent();

    StringBuilder stringBuilder = new StringBuilder();

    // Duyệt qua từng hàng
    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      Tr row = (Tr) rows.get(rowIndex);
      // lấy cả text của hàng
      String rowText = getTextFromRow(row);

      if (!StringUtils.hasText(rowText)) {
        continue;
      }
      // nếu text trùng với key của bàng ví dụ ${...table}

      String tableName = StringUtil.EMPTY;
      List<Map<String, String>> dataList = new ArrayList<>();
      for (Entry<String, List<Map<String, String>>> entry : tableData.entrySet()) {
        String key = entry.getKey();
        if (rowText.contains("${" + key)) {
          dataList = entry.getValue();
          tableName = key;
          break;
        }
      }

      if (CollectionUtils.isEmpty(dataList)) {
        continue;
      }

      // Lấy hàng tiếp theo làm template để fill dữ liệu
      Tr templateRow = row;
      // xoá hàng đó đi
      table.getContent().remove(rowIndex);

      // Thêm các dòng dữ liệu
      for (Map<String, String> rowData : dataList) {
        Tr newRow = createRowFromData(tableName, rowData, templateRow, stringBuilder);
        table.getContent().add(rowIndex, newRow);
        rowIndex++;
        stringBuilder.setLength(0);
      }
      return; // xong thì thoát
    }
  }

  private boolean fillTexData(Object paraObj, StringBuilder stringBuilder, List<Object> paraContents) {
    if (!(paraObj instanceof R r)) {
      return false;
    }
    List<Object> textContents = r.getContent();
    if (CollectionUtils.isEmpty(textContents)) {
      return false;
    }
    Text text = (Text) ((JAXBElement<?>) textContents.get(0)).getValue();
    text.setValue(stringBuilder.toString());
    if (textContents.size() > 1) {
      for (int i = 1; i < textContents.size(); i++) {
        text = (Text) ((JAXBElement<?>) textContents.get(i)).getValue();
        text.setValue(StringUtil.EMPTY);
      }
    }
    paraContents.clear();
    paraContents.add(paraObj);
    return true;
  }

  private void getTextFromCell(Tc cell, StringBuilder stringBuilder) {
    for (Object pObj : cell.getContent()) {
      if (pObj instanceof P p) {
        stringBuilder.append(getText(p));
      }
    }
  }

  private String getTextFromRow(Tr row) {
    StringBuilder sb = new StringBuilder();
    for (Object cellObj : row.getContent()) {
      Tc cell = (Tc) ((JAXBElement<?>) cellObj).getValue();
      getTextFromCell(cell, sb);
    }
    return sb.toString().trim();
  }

  private Tr createRowFromData(String tableName, Map<String, String> rowData, Tr templateRow, StringBuilder stringBuilder) {
    Map<String, String> rowDataNew = new HashMap<>();
    rowData.forEach((key, value) -> rowDataNew.put("${" + tableName + "." + key + "}", value));

    Tr newRow = XmlUtils.deepCopy(templateRow);
    // lặp qua từng ô trong hàng
    for (Object rowContent : newRow.getContent()) {
      Object tcObj = ((JAXBElement<?>) rowContent).getValue();
      if (tcObj instanceof Tc tc) {
        Object tcObject = tc.getContent().get(0);
        if (tcObject instanceof P paragraph) {
          List<Object> paraContents = paragraph.getContent();
          // lấy giá trị trong cell
          String cellValue = getValueFromParagraph(paraContents, stringBuilder);
          for (Object paraObj : paraContents) {
            if (paraObj instanceof R r) {
              List<Object> textContents = r.getContent();
              if (!CollectionUtils.isEmpty(textContents)) {
                Text text = (Text) ((JAXBElement<?>) textContents.get(0)).getValue();
                text.setValue(rowDataNew.get(cellValue));
                for (int i = 1; i < textContents.size(); i++) {
                  text = (Text) ((JAXBElement<?>) textContents.get(i)).getValue();
                  text.setValue(rowDataNew.get(cellValue));
                }
                paraContents.clear();
                paraContents.add(paraObj);
                break;
              }
            }
          }
          stringBuilder.setLength(0);
        }
      }
    }
    return newRow;
  }

  private String getValueFromParagraph(List<Object> paraContents, StringBuilder stringBuilder) {
    for (Object paraObj : paraContents) {
      if (paraObj instanceof R r) {
        List<Object> textContents = r.getContent();
        getValueFromTextContent(textContents, stringBuilder);
      }
    }
    return stringBuilder.toString();
  }

  private void getValueFromTextContent(List<Object> textContents, StringBuilder stringBuilder) {
    if (CollectionUtils.isEmpty(textContents)) {
      return;
    }
    for (Object obj : textContents) {
      Object txtObj = ((JAXBElement<?>) obj).getValue();
      if (txtObj instanceof Text txt) {
        stringBuilder.append(txt.getValue());
      }
    }
  }

  private String getText(P paragraph) {
    StringBuilder sb = new StringBuilder();
    for (Object o : paragraph.getContent()) {
      if (o instanceof R r) {
        for (Object o2 : r.getContent()) {
          sb.append(((Text) ((JAXBElement<?>) o2).getValue()).getValue());
        }
      }
    }
    return sb.toString();
  }

}
