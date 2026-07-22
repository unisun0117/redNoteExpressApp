package cn.qdm.tob.framework.jackson;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.framework.description.DescriptionStrategy;
import cn.qdm.tob.framework.description.DescriptionProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.util.NameTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 拦截带有 {@link Description} 注解的属性，按策略修改序列化行为：
 * <ul>
 *   <li>{@link DescriptionStrategy#REPLACEMENT} — 替换原字段值为显示文本</li>
 *   <li>{@link DescriptionStrategy#EXTENSION} — 保留原始编码值，追加 {@code {fieldName}Name} 影子字段</li>
 * </ul>
 */
class DescriptionValueSerializerModifier extends ValueSerializerModifier {

    private final List<DescriptionProvider> providers;

    DescriptionValueSerializerModifier(List<DescriptionProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription.Supplier beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        List<BeanPropertyWriter> result = new ArrayList<>(beanProperties.size() + 4);

        for (BeanPropertyWriter prop : beanProperties) {
            // -------- @Description --------
            var descAnn = prop.getAnnotation(Description.class);
            if (Objects.isNull(descAnn)) {
                result.add(prop);
                continue;
            }

            DescriptionValueSerializer descSerializer = new DescriptionValueSerializer(
                    providers,
                    descAnn.value(),
                    descAnn.separator()
            );

            if (descAnn.strategy() == DescriptionStrategy.EXTENSION) {
                result.add(prop);
                // 改用 rename + assignSerializer 实现影子字段。
                // NameTransformer 无条件追加 "Name"，避免简单匹配的隐含问题。
                BeanPropertyWriter shadow = prop.rename(NameTransformer.simpleTransformer("", "Name"));
                // 调试：如果影子字段名异常，打开下面注释
                // System.out.println("Shadow field: " + shadow.getName());
                shadow.assignSerializer(new ValueSerializer<>() {
                    @Override
                    public void serialize(Object value, JsonGenerator gen, SerializationContext serializers)
                            throws JacksonException {
                        gen.writeString(descSerializer.resolve(value));
                    }
                });
                result.add(shadow);
            } else {
                prop.assignSerializer(descSerializer);
                result.add(prop);
            }
        }

        return result;
    }
}
