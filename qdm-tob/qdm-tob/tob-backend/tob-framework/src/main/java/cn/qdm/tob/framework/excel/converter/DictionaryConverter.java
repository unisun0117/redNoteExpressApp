package cn.qdm.tob.framework.excel.converter;

import cn.qdm.tob.framework.Describable;
import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.framework.description.DescriptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用字典转换器
 * <p>
 * 复用 {@link DescriptionProvider} 机制（与 Jackson {@code @Description} 序列化共用同一套字典加载），
 * 无需单独定义 {@code @Dict} 注解或 {@code DictLoader} 接口。
 * </p>
 * <p>
 * 导出（convert）：编码 → 显示文本（通过 {@link DescriptionProvider#load} 查表）<br>
 * 导入（revert）：显示文本 → 编码（反向查表）
 * </p>
 */
public class DictionaryConverter implements ExcelValueConverter {

    private static final Logger log = LoggerFactory.getLogger(DictionaryConverter.class);

    /** 按注册顺序遍历，首个返回非 null 的生效 */
    private final List<DescriptionProvider> providers;

    /** 实例级缓存：用于流式导入场景 */
    private final ConcurrentHashMap<String, Map<Object, String>> cached = new ConcurrentHashMap<>();

    public DictionaryConverter(List<DescriptionProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public Object convert(Object value, Field field) {
        if (field == null) return value;

        Description desc = field.getAnnotation(Description.class);
        if (desc == null) return value;

        return getValue(desc.value(), value);
    }

    @Override
    public Object revert(Object value, Field field) {
        if (value == null || field == null) return value;

        Description desc = field.getAnnotation(Description.class);
        if (desc == null) return value;

        return revertValue(desc.value(), value, field);
    }

    /**
     * 编码 → 显示文本
     */
    private String getValue(String group, Object value) {
        if (value == null) return null;

        // 值自身可描述（如枚举实现了 Describable），直接返回描述文本，不查字典
        if (value instanceof Describable d) {
            return d.getDescription();
        }

        Map<Object, String> dictionary = cached.computeIfAbsent(group, this::loadGroup);

        String text = dictionary.get(value);
        if (text != null) return text;

        // 兼容类型不一致（如 Excel 读取 byte，字典 key 为 int）
        var targetType = dictionary.keySet().stream()
                .findFirst().map(Object::getClass).orElse(null);
        if (targetType != null) {
            try {
                Object converted = convertToType(value, targetType);
                text = dictionary.get(converted);
                if (text != null) return text;
            } catch (Exception ignored) {}
        }

        return value.toString();
    }

    /**
     * 显示文本 → 编码
     */
    private Object revertValue(String group, Object displayValue, Field field) {
        if (displayValue == null) return null;

        // 字段类型是实现了 Describable 的枚举，通过描述文本反向匹配枚举常量
        Class<?> fieldType = field.getType();
        if (fieldType.isEnum() && Describable.class.isAssignableFrom(fieldType)) {
            String text = displayValue.toString();
            for (Object constant : fieldType.getEnumConstants()) {
                if (text.equals(((Describable) constant).getDescription())) {
                    return constant;
                }
            }
            return null;
        }

        Map<Object, String> dictionary = cached.computeIfAbsent(group, this::loadGroup);
        String displayStr = displayValue.toString();

        Object itemKey = dictionary.entrySet().stream()
                .filter(e -> displayStr.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (itemKey == null) return null;

        Class<?> targetType = field.getType();
        if (targetType.isAssignableFrom(itemKey.getClass())) return itemKey;

        return convertToType(itemKey, targetType);
    }

    /**
     * 按注册顺序遍历所有 DescriptionProvider，首个返回非 null 的生效
     */
    private Map<Object, String> loadGroup(String group) {
        return providers.stream()
                .map(p -> p.load(group))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(Map::of);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertToType(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.valueOf(value.toString());
        if (targetType == long.class || targetType == Long.class) return Long.valueOf(value.toString());
        if (targetType == double.class || targetType == Double.class) return Double.valueOf(value.toString());
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.valueOf(value.toString());
        if (targetType == String.class) return value.toString();
        if (targetType.isEnum()) return Enum.valueOf((Class<? extends Enum>) targetType, value.toString());
        return value;
    }
}
