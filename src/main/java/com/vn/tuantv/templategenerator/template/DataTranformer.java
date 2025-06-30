package com.vn.tuantv.templategenerator.template;

import com.vn.tuantv.templategenerator.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataTranformer {

    private static final int TABLE_NAME_INDEX = 0;
    private static final int COLUMN_NAME_INDEX = 1;

    public Map<String, Map<String, List<String>>> transformTableData(
            Map<String, List<String>> columnData) {
        Map<String, Map<String, List<String>>> result = new HashMap<>();
        if (columnData == null || columnData.isEmpty()) {
            log.debug("[transformTableData] No column data provided, returning empty result.");
            return result;
        }

        String tableName;
        String columnName;
        for (Map.Entry<String, List<String>> entry : columnData.entrySet()) {
            String key = entry.getKey();
            List<String> columnValues = entry.getValue();
            if (!StringUtils.hasText(key)) {
                continue;
            }

            String[] keyElements = key.split(StringUtil.DOT_PATTERN);
            if (keyElements.length < 2) {
                log.debug("[transformTableData] Invalid key format: '{}'. Expected format: 'tableName.columnName'.", key);
                continue;
            }
            tableName = keyElements[TABLE_NAME_INDEX];
            columnName = keyElements[COLUMN_NAME_INDEX];

            Map<String, List<String>> tableData = result.get(tableName);
            if (tableData == null) {
                tableData = new HashMap<>();
            }
            tableData.put(columnName, columnValues);
            result.put(tableName, tableData);
        }

        return result;
    }

}
