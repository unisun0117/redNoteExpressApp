package cn.qdm.tob.framework.jackson;

import cn.qdm.tob.framework.oss.OssUrlGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 拦截带有 {@link OssResource} 注解的属性，应用 OSS URL 序列化。
 */
class OssResourceSerializerModifier extends ValueSerializerModifier {

    private final OssUrlGenerator ossUrlGenerator;

    OssResourceSerializerModifier(OssUrlGenerator ossUrlGenerator) {
        this.ossUrlGenerator = ossUrlGenerator;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription.Supplier beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        List<BeanPropertyWriter> result = new ArrayList<>(beanProperties.size());

        for (BeanPropertyWriter prop : beanProperties) {
            var ossAnn = prop.getAnnotation(OssResource.class);
            if (Objects.nonNull(ossAnn)) {
                var ser = new OssResourceSerializer(ossAnn, ossUrlGenerator);
                prop.assignSerializer(ser);
            }
            result.add(prop);
        }

        return result;
    }
}
