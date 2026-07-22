package cn.qdm.tob.framework.jackson;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OssResource {
    /**
     * 是否生成预签名url
     */
    boolean presigned() default true;

    /**
     * url有效期；仅当 presigned 为 true 时有效
     */
    long expiry() default 10;

    /**
     * url有效期时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 分割符(当多个Url拼接时的分割符)
     */
    String separator() default "";
}
