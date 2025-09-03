package com.vn.tuantv.templategenerator.poi_tl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocUtils {
    public static void stripTag(XWPFParagraph paragraph, Pattern pattern) {
        if (paragraph == null
                || CollectionUtils.isEmpty(paragraph.getRuns())
                || pattern == null) {
            return;
        }

        for (XWPFRun run : paragraph.getRuns()) {
            stripTag(run, pattern);
        }
    }

    public static void stripTag(XWPFRun run, Pattern pattern) {
        if (run == null) {
            return;
        }
        String str = run.toString();
        if (!StringUtils.hasText(str)) {
            return;
        }
        String rep =  pattern.matcher(str).replaceAll(StringUtil.EMPTY);
        if (!str.equals(rep)) {
            run.setText(rep, 0);
        }
    }

    public static void deleteLines(XWPFDocument document, boolean[] deleteLines) {
        if (deleteLines == null
                || deleteLines.length == 0
                || document == null
                || document.getBodyElements().isEmpty()) {
            return;
        }
        List<IBodyElement> bodyElements = document.getBodyElements();
        int bodyElementCount = bodyElements.size();
        for (int indexDeleteLine = deleteLines.length - 1; indexDeleteLine >= 0; indexDeleteLine--) {
            if (deleteLines[indexDeleteLine] && indexDeleteLine < bodyElementCount) {
                document.removeBodyElement(indexDeleteLine);
            }
        }
    }
}
