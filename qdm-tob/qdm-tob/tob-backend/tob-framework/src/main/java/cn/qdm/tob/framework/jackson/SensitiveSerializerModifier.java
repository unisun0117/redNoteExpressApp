package cn.qdm.tob.framework.jackson;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 拦截带有 {@link Sensitive} 注解的属性，应用脱敏序列化。
 */
class SensitiveSerializerModifier extends ValueSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription.Supplier beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        List<BeanPropertyWriter> result = new ArrayList<>(beanProperties.size());

        for (BeanPropertyWriter prop : beanProperties) {
            var sensitiveAnn = prop.getAnnotation(Sensitive.class);
            if (Objects.nonNull(sensitiveAnn)) {
                var ser = new SensitiveValueSerializer(sensitiveAnn.strategy(), sensitiveAnn.mask());
                prop.assignSerializer(ser);
            }
            result.add(prop);
        }

        return result;
    }
}
