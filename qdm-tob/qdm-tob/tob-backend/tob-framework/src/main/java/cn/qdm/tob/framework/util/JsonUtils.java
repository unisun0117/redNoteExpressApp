package cn.qdm.tob.framework.util;

import cn.qdm.tob.framework.Constants;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

public final class JsonUtils {
    private static final JsonMapper JSON_MAPPER;

    static {
        SimpleModule module = new SimpleModule("JavaTimeModule");
        module.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATETIME_PATTERN)));
        module.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_PATTERN)));
        module.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_TIME_PATTERN)));

        JSON_MAPPER = JsonMapper.builder()
                .addModule(module)
                // 序列化时忽略值为 null 的字段
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                // 反序列化时忽略 JSON 中不认识的字段
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    private JsonUtils() { }

    public static JsonMapper mapper() {
        return JSON_MAPPER;
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        if (StringUtils.isBlank(json)) return null;
        return exec(() -> JSON_MAPPER.readValue(json, valueType));
    }

    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        if (StringUtils.isBlank(json)) return null;
        return exec(() -> JSON_MAPPER.readValue(json, valueTypeRef));
    }

    public static <T> T fromJson(String json, JavaType valueType) {
        if (StringUtils.isBlank(json)) return null;
        return exec(() -> JSON_MAPPER.readValue(json, valueType));
    }

    public static <T> T fromJson(byte[] bytes, Class<T> valueType) {
        if (ArrayUtils.isEmpty(bytes)) return null;
        return exec(() -> JSON_MAPPER.readValue(bytes, valueType));
    }

    public static <T> T fromJson(byte[] bytes, TypeReference<T> valueTypeRef) {
        if (ArrayUtils.isEmpty(bytes)) return null;
        return exec(() -> JSON_MAPPER.readValue(bytes, valueTypeRef));
    }

    public static <T> T fromJson(byte[] bytes, JavaType valueType) {
        if (ArrayUtils.isEmpty(bytes)) return null;
        return exec(() -> JSON_MAPPER.readValue(bytes, valueType));
    }

    public static <T> T fromJson(InputStream stream, Class<T> valueType) {
        if (Objects.isNull(stream)) return null;
        return exec(() -> JSON_MAPPER.readValue(stream, valueType));
    }

    public static <T> T fromJson(InputStream stream, TypeReference<T> valueTypeRef) {
        if (Objects.isNull(stream)) return null;
        return exec(() -> JSON_MAPPER.readValue(stream, valueTypeRef));
    }

    public static <T> T fromJson(InputStream stream, JavaType valueType) {
        if (Objects.isNull(stream)) return null;
        return exec(() -> JSON_MAPPER.readValue(stream, valueType));
    }

    public static String toJson(Object value) {
        if (Objects.isNull(value)) return null;
        return exec(() -> JSON_MAPPER.writeValueAsString(value));
    }

    private static <T> T exec(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
