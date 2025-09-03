package com.vn.tuantv.templategenerator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.TypeReference;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonService {

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }


  public static <T> List<T> parseList(List<Object> obj, Class<T> classType) {
    if (CollectionUtils.isEmpty(obj)
        || classType == null) {
      return null;
    }
    return obj.stream().map(o -> parse(o, classType)).collect(Collectors.toList());
  }

  public static <T> T parse(Object obj, Class<T> classType) {
    if (obj == null
        || classType == null) {
      return null;
    }
    return mapper.convertValue(obj, classType);
  }

  public static String toJsonString(Object object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      return StringUtil.EMPTY;
    }
  }

  public static <T> T readValue(String json, Class<T> classType) {

    if (!StringUtils.hasText(json)
        || classType == null) {
      return null;
    }
    try {
      return mapper.readValue(json, classType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T readJsonFile(String jsonFilePath, Class<T> classType) {
    if (!StringUtils.hasText(jsonFilePath)
        || classType == null) {
      return null;
    }
    try {
      File file = new File(jsonFilePath);
      return mapper.readValue(file, classType);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
