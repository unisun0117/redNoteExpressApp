package cn.qdm.tob.framework.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

import java.util.Objects;

/**
 * {@code @Sensitive} 字段的序列化器 —— 按 {@link MaskStrategy} 对值进行掩码。
 */
public class SensitiveValueSerializer extends ValueSerializer<Object> {
    private final MaskStrategy strategy;
    private final String mask;

    SensitiveValueSerializer(MaskStrategy strategy, String mask) {
        this.strategy = strategy;
        this.mask = mask;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext prov) throws JacksonException {
        if (Objects.isNull(value)) {
            prov.defaultSerializeNullValue(gen);
            return;
        }
        gen.writeString(strategy.apply(String.valueOf(value), mask));
    }
}
