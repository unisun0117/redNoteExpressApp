package cn.qdm.tob.framework.jackson;

import org.apache.commons.lang3.StringUtils;

/**
 * 脱敏策略 —— 控制 {@link Sensitive} 的掩码行为。
 *
 * <pre>{@code
 * @Sensitive(strategy = MaskStrategy.MOBILE)   // 188****8888
 * private String phone;
 * }</pre>
 */
public enum MaskStrategy {
    /** 根据字符串长度自动选择规则（默认） */
    AUTO {
        @Override
        public String apply(String str, String mask) {
            int len = StringUtils.length(str);
            return switch (len) {
                case 0 -> str;
                case 2, 3, 4 -> NAME.apply(str, mask);
                case 11 -> MOBILE.apply(str, mask);
                case 16 -> ID_CARD.apply(str, mask);
                default -> FULL.apply(str, mask);
            };
        }
    },

    /** 姓名：保留姓 + 最后一个字 */
    NAME {
        @Override
        public String apply(String str, String mask) {
            if (StringUtils.isEmpty(str)) return str;
            int len = str.length();
            return switch (len) {
                case 2 -> apply(str, mask, 1, 0);
                case 3 -> apply(str, mask, 1, 1);
                default -> apply(str, mask, 2, 1);
            };
        }
    },

    /** 手机号：保留前 3 后 4 → 188****8888 */
    MOBILE {
        @Override
        public String apply(String str, String mask) {
            return apply(str, mask, 3, 4);
        }
    },

    /** 身份证号：保留前 6 后 4 → 123456********4321 */
    ID_CARD {
        @Override
        public String apply(String str, String mask) {
            return apply(str, mask, 6, 4);
        }
    },

    /** 金额：全部掩码且固定长度  */
    MONEY {
        @Override
        public String apply(String str, String mask) {
            return mask.repeat(3);
        }
    },

    /** 全部掩码 */
    FULL {
        @Override
        public String apply(String str, String mask) {
            return StringUtils.isEmpty(str) ? str : mask.repeat(str.length());
        }
    };

    public abstract String apply(String str, String mask);

    protected String apply(String str, String mask, int prefix, int suffix) {
        if (StringUtils.isEmpty(str)) return str;

        int len = str.length();
        int count = len - prefix - suffix;
        if (count <= 0) return mask.repeat(len);

        return str.substring(0, prefix) + mask.repeat(count) + str.substring(len - suffix);
    }
}
