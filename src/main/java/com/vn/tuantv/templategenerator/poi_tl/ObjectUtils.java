package com.vn.tuantv.templategenerator.poi_tl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectUtils {

    /**
     *
     * @param map
     * @param keyPath separate with dot: a.b.c
     * @return
     */
    public static Object findValue(Map<String, Object> map, String keyPath) {
        if (map == null
                || map.isEmpty()
                || !StringUtils.hasText(keyPath)) {
            return null;
        }

        Object result = map;
        for (String key : keyPath.split(StringUtil.DOT_PATTERN)) {
            if (!(result instanceof Map<?,?> m)) {
                return null;
            }
            result = m.get(key);
        }

        return result;
    }

}
