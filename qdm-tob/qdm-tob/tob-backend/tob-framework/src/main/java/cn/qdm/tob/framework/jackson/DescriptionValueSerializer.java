package cn.qdm.tob.framework.jackson;

import cn.qdm.tob.framework.Describable;
import cn.qdm.tob.framework.description.DescriptionProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@code @Description} 字段的序列化器。
 * <p>
 * 转换优先级：
 * <ol>
 *   <li>值为 null → 输出 null</li>
 *   <li>值实现了 {@link Describable} → 直接调用 {@link Describable#getDescription()}</li>
 *   <li>遍历 {@link DescriptionProvider}，首个 {@link DescriptionProvider#load(String)} 返回非 null 映射表 → 查表</li>
 *   <li>配置了分隔符 → 按分隔符拆分后逐项查表，再以分隔符拼接</li>
 *   <li>全部不适配 → 保留原始值</li>
 * </ol>
 */
class DescriptionValueSerializer extends ValueSerializer<Object> {
    private final List<DescriptionProvider> providers;
    private final String group;
    private final String separator;

    private final Cache<String, Map<Object, String>> cache;

    DescriptionValueSerializer(List<DescriptionProvider> providers, String group, String separator) {
        this.providers = providers;
        this.group = group;
        this.separator = separator;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(100)
                .build();
    }

    @Override
    public void serialize(Object val, JsonGenerator gen, SerializationContext prov) throws JacksonException {
        if (Objects.isNull(val)) {
            prov.defaultSerializeNullValue(gen);
            return;
        }
        gen.writeString(resolve(val));
    }

    String resolve(Object val) {
        if (val instanceof Describable d) return d.getDescription();

        var map = cache.get(group, g -> providers
                .stream()
                .map(i -> i.load(g))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null)
        );

        if (Objects.isNull(map)) return String.valueOf(val);

        if (StringUtils.isEmpty(separator)) return map.getOrDefault(val, String.valueOf(val));

        var parts = String.valueOf(val).split(Pattern.quote(separator));
        return Stream.of(parts)
                .map(k -> map.getOrDefault(k, k))
                .collect(Collectors.joining(separator));
    }
}
