package com.vn.tuantv.templategenerator.poi_tl;

import com.deepoove.poi.XWPFTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OnePassPostProcessor {

    // pattern {{#if customer.extra}}
    private static final Pattern IF_OPEN  = Pattern.compile("\\{\\{#if\\s+([^}]+)\\}\\}");
    // pattern {{/if}}
    private static final Pattern IF_CLOSE = Pattern.compile("\\{\\{/if\\}\\}");

    @Data
    @AllArgsConstructor
    public static class IfMark {
        private int startIndex;
        private String expr;
    }

    @Data
    @AllArgsConstructor
    public static class IfPair {
        private int startIndex;
        private String expr;
        private int endIndex;
    }

    public static void process(XWPFTemplate template, Map<String, Object> data) {
        XWPFDocument doc = template.getXWPFDocument();

        // quét 1 lượt để gom các cặp if/endif (hỗ trợ lồng nhau)
        List<IBodyElement> bodyElements = doc.getBodyElements();
        Deque<IfMark> stack = new ArrayDeque<>();
        List<IfPair> pairs = new ArrayList<>();

        for (int index = 0; index < bodyElements.size(); index++) {
            IBodyElement bodyElement = bodyElements.get(index);
            if (!(bodyElement instanceof XWPFParagraph)) {
                continue;
            }
            String text = ((XWPFParagraph) bodyElement).getText();

            if (!StringUtils.hasText(text)) {
                continue;
            }

            Matcher mOpen = IF_OPEN.matcher(text);
            if (mOpen.find()) {
                String expr = mOpen.group(1).trim(); //ví dụ : customer.extra
                stack.push(new IfMark(index, expr));
                continue;
            }
            Matcher mClose = IF_CLOSE.matcher(text);
            if (mClose.find()) {
                if (stack.isEmpty()) {
                    System.out.println("Không có thẻ mở if");
                } else {
                    IfMark ifMark = stack.pop();
                    pairs.add(new IfPair(ifMark.getStartIndex(), ifMark.getExpr(), index));
                }
            }
        }

        // Xử lý theo thứ tự từ trong ra ngoài ( xoá ngược để không lệch index)
        pairs = pairs.stream()
                .sorted(Comparator.comparingInt(IfPair::getStartIndex).reversed())
                .collect(Collectors.toList());

//        for (IfPair p : pairs) {
//            // gỡ tag mở/đóng
//            stripTag((XWPFParagraph) body.get(p.startIdx), IF_OPEN);
//            stripTag((XWPFParagraph) body.get(p.endIdx), IF_CLOSE);
//
//            boolean show = truthy(resolve(p.expr, model));
//            if (!show) {
//                // xóa [startIdx+1 .. endIdx-1]
//                for (int k = p.endIdx - 1; k > p.startIdx; k--) {
//                    removeElement(doc, body.get(k));
//                    body.remove(k);
//                }
//                // dọn 2 paragraph tag nếu trống
//                cleanupParagraph((XWPFParagraph) body.get(p.startIdx));
//                // sau khi xóa đoạn giữa, phần tử endIdx đã dịch -> lấy lại body
//                body = doc.getBodyElements();
//                int newEnd = Math.min(p.startIdx + 1, body.size() - 1);
//                if (newEnd >= 0 && body.get(newEnd) instanceof XWPFParagraph) {
//                    cleanupParagraph((XWPFParagraph) body.get(newEnd));
//                }
//            } else {
//                // chỉ bỏ tag, giữ nội dung
//            }
//            body = doc.getBodyElements();
//        }
//
//        // 3) Dọn paragraph rỗng & row rỗng (1 lượt)
//        for (int i = body.size() - 1; i >= 0; i--) {
//            IBodyElement be = body.get(i);
//
//            if (be instanceof XWPFParagraph) {
//                XWPFParagraph p = (XWPFParagraph) be;
//                if (isParaEmpty(p)) {
//                    removeElement(doc, p);
//                    continue;
//                }
//            }
//
//            if (be instanceof XWPFTable) {
//                XWPFTable tb = (XWPFTable) be;
//                for (int r = tb.getNumberOfRows() - 1; r >= 0; r--) {
//                    XWPFTableRow row = tb.getRow(r);
//                    if (row == null) continue;
//                    boolean empty = row.getTableCells().stream().allMatch(OnePassPostProcessor::isCellEmpty);
//                    if (empty) tb.removeRow(r);
//                }
//                if (tb.getNumberOfRows() <= 1) {
//                    removeElement(doc, tb);
//                }
//            }
//        }
    }


    private static void stripTag(XWPFParagraph paragraph, Pattern pattern) {
        if (paragraph == null) {
            return;
        }

        for (XWPFRun run : paragraph.getRuns()) {
            String t = run.toString();
            if (!StringUtils.hasText(t)) {
                continue;
            }
            String rep = pattern.matcher(t).replaceAll("");
            if (!t.equals(rep)) {
                run.setText(rep, 0);
            }
        }
    }
    private static boolean isParaEmpty(XWPFParagraph paragraph) {
        return Optional.ofNullable(paragraph.getText()).orElse("")
                .replace("\u00A0", " ").trim()
                .isEmpty();
    }

    private static boolean isCellEmpty(XWPFTableCell cell) {
        return cell.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .reduce("", (a, b) -> a+ b)
                .replace("\u00A0", " ").trim()
                .isEmpty();
    }

//    private static void removeElement(XWPFDocument doc, IBodyElement el) {
//        if (el instanceof XWPFParagraph p) {
//            int pos = doc.getPosOfParagraph(p);
//            if (pos >= 0) doc.removeBodyElement(pos);
//        } else {
//            int pos = doc.getBodyElements().indexOf(el);
//            if (pos >= 0) doc.removeBodyElement(pos);
//        }
//    }
//
//    private static Object resolve(String path, Map<String, Object> model) {
//        Object cur = model;
//        for (String p : path.split("\\.")) {
//            if (!(cur instanceof Map)) return null;
//            cur = ((Map<?, ?>) cur).get(p);
//        }
//        return cur;
//    }
//
//    private static boolean truthy(Object v) {
//        if (v == null) return false;
//        if (v instanceof Boolean b) return b;
//        if (v instanceof Number n)  return n.doubleValue() != 0d;
//        String s = v.toString().trim();
//        return !s.isEmpty() && !"false".equalsIgnoreCase(s) && !"0".equals(s);
//    }
}
