package com.vn.tuantv.templategenerator.poi_tl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IfPair {

    private int startIndex;

    private XWPFParagraph startParagraph;

    private int endIndex;

    private XWPFParagraph endParagraph;

    private String expression;

    private int depth;

}
