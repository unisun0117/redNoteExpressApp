package cn.qdm.tob.modules.system.common;

public interface CacheKeys {
    String PREFIX = "tob:system:";

    String DICT_ITEMS = PREFIX + "dict:items:";

    /** 活跃字典项缓存（仅 ENABLED 状态），与全量缓存不同前缀，避免误删 */
    String DICT_ITEMS_ACTIVE = PREFIX + "dict:items:active:";

    String AUTH_SMS_CODE = PREFIX + "auth:sms_code";

    // ==================== 缓存 Key ====================
    /** 全量菜单列表（按 group 分组），TTL 1 天，变更时同步 @CacheEvict */
    String MENU_ALL = PREFIX + "menu:all:";

    /** 用户最终权限码集合 */
    String USER_PERMS = PREFIX + "user:perms:";

    /** 用户授权 menuId 集合（不存整棵树，和 menu:all 拼装剪枝） */
    String USER_MENU_IDS = PREFIX + "user:menu_ids:";
}
