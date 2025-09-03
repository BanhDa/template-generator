package com.vn.tuantv.templategenerator.poi_tl;

import com.deepoove.poi.XWPFTemplate;
import com.vn.tuantv.templategenerator.poi_tl.dto.IfOpenMark;
import com.vn.tuantv.templategenerator.poi_tl.dto.IfPair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class AppearanceProcessor {

    // pattern {{#if customer.extra}}
    private static final Pattern IF_OPEN  = Pattern.compile("\\{\\{#if\\s+([^}]+)\\}\\}");
    // pattern {{/if}}
    private static final Pattern IF_CLOSE = Pattern.compile("\\{\\{/if\\}\\}");

    /**
     * Xoá các dòng không thoả mãn điều kiện If
     * @param template
     * @param data
     */
    public void process(XWPFTemplate template, Map<String, Object> data) {
        XWPFDocument document = template.getXWPFDocument();
        //TODO: tìm các cặp if
        List<IfPair> ifPairs = collectIfPairs(document);
        //TODO: xoá
        deleteLineWithIfCondition(document, data, ifPairs);
    }

    private List<IfPair> collectIfPairs(XWPFDocument document) {
        List<IfPair> ifPairs = new ArrayList<>();

        if (document == null
            || CollectionUtils.isEmpty(document.getBodyElements())) {
            return ifPairs;
        }

        List<IBodyElement>  bodyElements = document.getBodyElements();
        Deque<IfOpenMark> ifOpenMarksStack = new ArrayDeque<>();

        for (int index = 0; index < bodyElements.size(); index++) {
            IBodyElement bodyElement = bodyElements.get(index);
            if (!(bodyElement instanceof XWPFParagraph paragraph)) {
                continue;
            }

            String text = paragraph.getText();
            if (!StringUtils.hasText(text)) {
                continue;
            }

            Matcher matcher = IF_OPEN.matcher(text);
            if (matcher.find()) {
                ifOpenMarksStack.push(IfOpenMark.builder()
                        .index(index)
                        .paragraph(paragraph)
                        .expression(text)
                        .depth(ifOpenMarksStack.size())
                        .build());
                continue;
            }
            if (IF_CLOSE.matcher(text).find()) {
                if (ifOpenMarksStack.isEmpty()) {
                    throw new RuntimeException("Don't have if open marks. line " + index);
                }
                IfOpenMark ifOpenMark = ifOpenMarksStack.pop();
                ifPairs.add(IfPair.builder()
                                .startIndex(ifOpenMark.getIndex())
                                .startParagraph(ifOpenMark.getParagraph())
                                .expression(ifOpenMark.getExpression())
                                .endIndex(index)
                                .endParagraph(paragraph)
                                .depth(ifOpenMark.getDepth())
                                .build());
            }

        }

        return ifPairs;
    }

    /**
     * Vòng lặp đầu tiên: lặp qua các điều kiện if, đánh dấu những dòng cần xoá
     * Vòng lặp thứ 2: xoá những dòng có đánh dấu xoá
     * @param document
     * @param data
     * @param ifPairs
     */
    private void deleteLineWithIfCondition(XWPFDocument document, Map<String, Object> data, List<IfPair> ifPairs) {
        if (CollectionUtils.isEmpty(ifPairs)
                || document == null
                || CollectionUtils.isEmpty(document.getBodyElements())) {
            return;
        }

        if (data == null) {
            data = new HashMap<>();
        }

        List<IBodyElement> bodyElements = document.getBodyElements();
        ifPairs.sort(Comparator.comparingInt(IfPair::getDepth));
        boolean[] deleteLines = markDeleteLine(bodyElements.size(), ifPairs, data);
        DocUtils.deleteLines(document, deleteLines);

    }

    private boolean[] markDeleteLine(Integer totalNumberOfLines, List<IfPair> ifPairs, Map<String, Object> data) {
        boolean[] deleteLines = new boolean[totalNumberOfLines];

        for (IfPair ifPair : ifPairs) {
            if (ifPair.getStartIndex() + 1 >= deleteLines.length
                    || deleteLines[ifPair.getStartIndex() + 1]) {
                continue;
            }

            DocUtils.stripTag(ifPair.getStartParagraph(), IF_OPEN);
            DocUtils.stripTag(ifPair.getEndParagraph(), IF_CLOSE);

            boolean show = ConditionUtils.checkCondition(ifPair.getExpression(), data);
            if (!show) { // nếu không show
                // đánh dấu xoá tất cả các dòng nằm trong điêu kiện
                for (int index = ifPair.getStartIndex(); index < ifPair.getEndIndex(); index++) {
                    deleteLines[index] = true;
                }
            } else { // Nếu có show
                // đánh dấu xoá dòng điều kiện
                deleteLines[ifPair.getStartIndex()] = true;
                deleteLines[ifPair.getEndIndex()] = true;
            }
        }

        return deleteLines;
    }

}
