package com.vn.tuantv.templategenerator.poi_tl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConditionUtils {

    public static boolean checkCondition(String expression, Map<String, Object> data) {
        if (!StringUtils.hasLength(expression)
                || data == null
                || data.isEmpty()) {
            return false;
        }

        Object value = ObjectUtils.findValue(data, expression);

        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String str) {
            return StringUtils.hasText(str);
        }

        if (value instanceof Collection<?> collection) {
            return !CollectionUtils.isEmpty(collection);
        }

        return true;

    }

}
