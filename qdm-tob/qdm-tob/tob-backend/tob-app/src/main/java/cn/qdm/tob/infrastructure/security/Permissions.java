package cn.qdm.tob.infrastructure.security;


public enum Permissions implements Authority {
    /**
      *  system 系统模块 权限
     */
    USER_VIEW("sys:user:view", "用户查看"),
    USER_EDIT("sys:user:edit", "用户编辑"),
    USER_DELETE("sys:user:delete", "用户删除"),

    MENU_VIEW("sys:menu:view", "菜单查看"),
    MENU_EDIT("sys:menu:edit", "菜单编辑"),
    MENU_DELETE("sys:menu:delete", "菜单删除"),

    ROLE_VIEW("sys:role:view", "角色查看"),
    ROLE_EDIT("sys:role:edit", "角色编辑"),
    ROLE_DELETE("sys:role:delete", "角色删除"),

    DICT_VIEW("sys:dict:view", "字典查看"),
    DICT_EDIT("sys:dict:edit", "字典编辑"),
    DICT_DELETE("sys:dict:delete", "字典删除"),

    PRIVACY_DOC_VIEW("sys:privacy:view", "隐私政策查看"),
    PRIVACY_DOC_EDIT("sys:privacy:edit", "隐私政策编辑"),

    /**
     *  customer 客户模块 权限
     */
    CUSTOMER_VIEW("cst:customer:view", "客户查看"),

    /** 运营管理-公告 */
    ANNOUNCEMENT_VIEW("ops:announcement:view", "公告查看"),
    ANNOUNCEMENT_EDIT("ops:announcement:edit", "公告编辑"),


    /**
     *  order 订单模块 权限
     */
    ORDER_VIEW("ord:order:view", "订单查看"),

    /** 账户管理 */
    ACCOUNT_VIEW("ord:account:view", "账户查看"),
    ACCOUNT_TRANSACTION_CREATE("ord:account:transaction:create", "新增流水"),

    /** 资金流水 */
    TRANSACTION_VIEW("ord:transaction:view", "资金流水查看"),
    TRANSACTION_EXPORT("ord:transaction:export", "资金流水导出"),

    /**
     *  product 商品模块 权限
     */
    PRODUCT_VIEW("prd:product:view", "商品查看"),
    CATEGORY_VIEW("prd:category:view", "商品分类查看"),
    CATEGORY_EDIT("prd:category:edit", "商品分类编辑"),

    /** 价格组 */
    PRICE_GROUP_LIST("prd:price-group:list", "价格组查看"),
    PRICE_GROUP_CREATE("prd:price-group:create", "价格组新增"),
    PRICE_GROUP_UPDATE("prd:price-group:update", "价格组编辑"),
    PRICE_GROUP_OPTIONS("prd:price-group:options", "价格组下拉选项"),

    /** 价格组明细 */
    PRICE_DETAIL_LIST("prd:price-detail:list", "价格组明细查看"),
    PRICE_DETAIL_DETAIL("prd:price-detail:detail", "价格组明细详情"),
    PRICE_DETAIL_CREATE("prd:price-detail:create", "价格组明细新增"),
    PRICE_DETAIL_UPDATE("prd:price-detail:update", "价格组明细编辑"),
    PRICE_DETAIL_LOOKUP("prd:price-detail:lookup", "价格组明细条码反查"),
    PRICE_DETAIL_EXPORT("prd:price-detail:export", "价格组明细导出"),
    PRICE_DETAIL_IMPORT("prd:price-detail:import", "价格组明细导入"),
    ;

    private final String value;
    private final String description;

    Permissions(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
