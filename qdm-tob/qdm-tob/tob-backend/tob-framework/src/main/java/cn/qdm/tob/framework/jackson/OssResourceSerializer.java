package cn.qdm.tob.framework.jackson;

import cn.qdm.tob.framework.oss.OssUrlGenerator;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@code @OssResource} 字段的序列化器 —— 将 OSS key 转换为完整 URL。
 * <p>
 * 支持类型：
 * <ul>
 *   <li>{@link String} - 单个 key</li>
 *   <li>{@link Collection}&lt;String&gt; - key 集合</li>
 *   <li>String[] - key 数组</li>
 * </ul>
 * <p>
 * 处理逻辑：
 * <ul>
 *   <li>值为 null → 输出 null</li>
 *   <li>URL 生成失败 → 输出原始 key</li>
 * </ul>
 */
@Slf4j
class OssResourceSerializer extends ValueSerializer<Object> {

    private final OssResource annotation;
    private final OssUrlGenerator ossUrlGenerator;

    OssResourceSerializer(OssResource annotation, OssUrlGenerator ossUrlGenerator) {
        this.annotation = annotation;
        this.ossUrlGenerator = ossUrlGenerator;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext prov) {
        if (Objects.isNull(value)) {
            gen.writeNull();
            return;
        }

        if (value instanceof String str) {
            gen.writeString(convertSingle(str));
        } else if (value instanceof Collection<?> coll) {
            String result = coll.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(this::convertSingle)
                    .collect(Collectors.joining(annotation.separator()));
            gen.writeString(result);
        } else if (value.getClass().isArray() && value.getClass().getComponentType() == String.class) {
            String result = Stream.of((String[]) value)
                    .filter(Objects::nonNull)
                    .map(this::convertSingle)
                    .collect(Collectors.joining(annotation.separator()));
            gen.writeString(result);
        } else {
            gen.writeString(String.valueOf(value));
        }
    }

    private String convertSingle(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }

        try {
            if (annotation.presigned()) {
                return ossUrlGenerator.generatePresignedUrl(key, annotation.expiry(), annotation.timeUnit());
            } else {
                return ossUrlGenerator.generatePublicUrl(key);
            }
        } catch (Exception e) {
            log.warn("生成 OSS URL 失败，返回原始 key: {}", key, e);
            return key;
        }
    }
}
