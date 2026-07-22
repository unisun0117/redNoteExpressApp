package cn.qdm.tob.framework.oss;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * OSS 上传策略枚举。
 * 枚举值即前端请求的策略名（如 ?policy=AVATARS）。
 * dir 支持 #{date:format} 日期占位符，运行时替换为当天日期。
 */
@Getter
@RequiredArgsConstructor
public enum StsPolicy implements Describable {
    AVATARS("tob/avatars/#{date:yyyy-MM-dd}",
            DataSize.ofMegabytes(5),
            Duration.ofMinutes(15),
            new String[]{"image/*"},
            "用户头像上传"),

    PRODUCTS("tob/products/#{date:yyyy/MM/dd}",
            DataSize.ofMegabytes(10),
            Duration.ofMinutes(30),
            new String[]{"image/*"},
            "商品图片上传"),

    /** 客户档案上传（门头照、营业执照、收货位置等通用图片） */
    CUSTOMER_ARCHIVE("tob/customer-archive/#{date:yyyy/MM/dd}",
            DataSize.ofMegabytes(5),
            Duration.ofMinutes(30),
            new String[]{"image/*"},
            "客户档案图片上传"),
    ;

    /** 上传目录，支持 #{date:format} 占位符 */
    private final String dir;

    /** 文件大小上限 */
    private final DataSize maxSize;

    /** 凭证有效期 */
    private final Duration duration;

    /** 允许的 Content-Type（精确匹配列表） */
    private final String[] contentTypeIn;

    private final String description;
}
