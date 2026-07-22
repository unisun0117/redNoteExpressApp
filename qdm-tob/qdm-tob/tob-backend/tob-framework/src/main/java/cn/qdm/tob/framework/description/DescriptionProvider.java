package cn.qdm.tob.framework.description;

import jakarta.annotation.Nullable;

import java.util.Map;

/**
 * 描述值提供者 —— 将 {@link Description#value()} 指定的分组下所有编码一次性加载为映射表。
 * <p>
 * 同时服务于：
 * <ul>
 *   <li><b>Jackson 序列化</b>：编码 → 显示文本（单向），由 {@code DescriptionJsonSerializer} 调用</li>
 *   <li><b>Excel 导入/导出</b>：编码 ↔ 显示文本（双向），由 {@code DictionaryConverter} 调用</li>
 * </ul>
 * <p>
 * 框架按注册顺序遍历所有提供者，{@link #load(String)} 返回非 null 的 Map 时采用该映射表进行查找；
 * 返回 null 表示当前提供者不适配该分组，继续尝试下一个。全部不适配时使用原始编码值。
 * <p>
 * 实现类声明为 Spring Bean 即可自动注册；可通过 {@link org.springframework.core.annotation.Order}
 * 或 {@link org.springframework.core.PriorityOrdered} 控制匹配优先级。
 */
public interface DescriptionProvider {

    /**
     * 加载指定分组的完整编码→文本映射。
     *
     * @param group {@link Description#value()}，作为分组标识（如字典编码、静态规则等）
     * @return 编码→文本映射；返回 {@code null} 表示不适配该分组
     */
    @Nullable
    Map<Object, String> load(String group);
}
