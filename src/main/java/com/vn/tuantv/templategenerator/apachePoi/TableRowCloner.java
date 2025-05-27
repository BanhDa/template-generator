package com.vn.tuantv.templategenerator.apachePoi;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class TableRowCloner {
  public static XWPFTableRow cloneTableRow(XWPFTable table, XWPFTableRow sourceRow) {
    XWPFTableRow newRow = table.createRow();
    while (newRow.getTableCells().size() > 0) {
      newRow.removeCell(0);
    }

    for (XWPFTableCell sourceCell : sourceRow.getTableCells()) {
      XWPFTableCell newCell = newRow.addNewTableCell();

      // Copy cell-level properties
      newCell.getCTTc().setTcPr(sourceCell.getCTTc().getTcPr());

      // Xóa đoạn mặc định
      newCell.removeParagraph(0);

      // Clone paragraphs
      for (XWPFParagraph sourceP : sourceCell.getParagraphs()) {
        XWPFParagraph newP = newCell.addParagraph();
        newP.getCTP().setPPr(sourceP.getCTP().getPPr());

        for (XWPFRun sourceRun : sourceP.getRuns()) {
          XWPFRun newRun = newP.createRun();
          newRun.setText(sourceRun.text());

          // Copy style
          newRun.setBold(sourceRun.isBold());
          newRun.setItalic(sourceRun.isItalic());
          newRun.setUnderline(sourceRun.getUnderline());
          newRun.setColor(sourceRun.getColor());
          newRun.setFontFamily(sourceRun.getFontFamily());
          newRun.setFontSize(sourceRun.getFontSizeAsDouble());
          newRun.setStrikeThrough(sourceRun.isStrikeThrough());
          newRun.setTextPosition(sourceRun.getTextPosition());
        }
      }
    }

    return newRow;
  }
}
