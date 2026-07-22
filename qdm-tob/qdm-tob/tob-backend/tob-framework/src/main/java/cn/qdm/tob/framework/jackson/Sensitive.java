package cn.qdm.tob.framework.jackson;


import java.lang.annotation.*;

/**
 * 脱敏注解 —— 序列化时对字段值进行掩码处理。
 *
 * <pre>{@code
 * @Sensitive                                   // 自动 → ***
 * @Sensitive(strategy = MaskStrategy.NAME)      // → 张*
 * @Sensitive(strategy = MaskStrategy.MOBILE)    // → 188****8888
 * @Sensitive(strategy = MaskStrategy.ID_CARD)   // → 430612********2571
 * @Sensitive(mask = "#")                       // → ###
 * }</pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

    /** 掩码字符，默认 * */
    String mask() default "*";

    /** 脱敏策略，默认 {@link MaskStrategy#AUTO} 根据长度自动适配 */
    MaskStrategy strategy() default MaskStrategy.AUTO;
}
