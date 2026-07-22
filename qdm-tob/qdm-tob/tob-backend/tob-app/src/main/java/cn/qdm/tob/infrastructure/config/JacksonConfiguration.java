package cn.qdm.tob.infrastructure.config;

import cn.qdm.tob.framework.Constants;
import cn.qdm.tob.framework.description.DescriptionProvider;
import cn.qdm.tob.framework.jackson.DescriptionJacksonModule;
import cn.qdm.tob.framework.jackson.OssJacksonModule;
import cn.qdm.tob.framework.jackson.SensitiveJacksonModule;
import cn.qdm.tob.framework.oss.OssUrlGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Jackson 序列化全局配置。
 * <p>
 * 日期格式：
 * <ul>
 *   <li>{@link LocalDateTime} / {@link Date} → {@code yyyy-MM-dd HH:mm:ss}</li>
 *   <li>{@link LocalDate} → {@code yyyy-MM-dd}</li>
 * </ul>
 * </p>
 * <p>
 * Long 类型序列化为 String，避免 JavaScript 精度丢失。
 * </p>
 */
@Configuration
public class JacksonConfiguration {
    @Bean
    public DescriptionJacksonModule descriptionJacksonModule(List<DescriptionProvider> providers) {
        DescriptionJacksonModule module = new DescriptionJacksonModule();
        providers.forEach(module::addProvider);
        return module;
    }

    @Bean
    public SensitiveJacksonModule sensitiveJacksonModule() {
        return new SensitiveJacksonModule();
    }

    @Bean
    public OssJacksonModule ossJacksonModule(OssUrlGenerator ossUrlGenerator) {
        return new OssJacksonModule(ossUrlGenerator);
    }

    /**
     * 注册 Java 时间类型序列化器（替代 Jackson 2.x 的 {@code @JsonFormat}）。
     */
    @Bean
    public SimpleModule javaTimeModule() {
        SimpleModule module = new SimpleModule("JavaTimeModule");
        module.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATETIME_PATTERN)));
        module.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_PATTERN)));
        module.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_TIME_PATTERN)));
        return module;
    }

    /**
     * Long 类型序列化为 String，避免前端 JavaScript 精度丢失（JS number 最大安全整数为 2^53-1）。
     */
    @Bean
    public SimpleModule longToStringModule() {
        SimpleModule module = new SimpleModule("LongToStringModule");
        ValueSerializer<Long> serializer = new ValueSerializer<>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializationContext ctx) throws JacksonException {
                if (value == null) {
                    gen.writeNull();
                    return;
                }
                gen.writeString(value.toString());
            }
        };
        module.addSerializer(Long.class, serializer);
        module.addSerializer(Long.TYPE, serializer);
        return module;
    }
}

