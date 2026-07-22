// 后台管理系统菜单数据 — 对应 product/spec/README.md 后台管理菜单目录
// url 字段指向 product/prd/ 下的实际原型 HTML 文件
window.PRD_MENU_DATA = [
    {
        id: "workspace",
        title: "工作台",
        icon: "HomeFilled",
        children: [
            { id: "/", name: "首页" },
            { id: "/approve", name: "待办", url: "../spec/08-审批管理/待办管理-后台-原型.html" },
        ],
    },
    {
        id: "product",
        title: "商品管理",
        icon: "Goods",
        children: [
            { id: "/product/warehouse", name: "商品资料", url: "../spec/01-商品管理/商品资料-原型.html" },
            { id: "/product/category", name: "商品分类", url: "../spec/01-商品管理/商品分类管理-原型.html" },
            { id: "/product/price", name: "商品价格", url: "../spec/01-商品管理/商品价格管理-原型.html" },
        ],
    },
    {
        id: "customer",
        title: "客户管理",
        icon: "User",
        children: [
            { id: "/customer/profile", name: "客户档案", url: "../spec/02-客户管理/客户档案-后台-原型.html" },
            { id: "/customer/account", name: "登录账号", url: "../spec/02-客户管理/小程序登录注册模块-后台-原型.html" },
        ],
    },
    {
        id: "marketing",
        title: "营销管理",
        icon: "Present",
        children: [
            { id: "/marketing/coupon", name: "优惠券管理", url: "../spec/04-营销管理/优惠券管理-后台-原型.html" },
        ],
    },
    {
        id: "trade",
        title: "订单管理",
        icon: "Document",
        children: [
            { id: "/trade/order", name: "销售订单", url: "../spec/03-交易管理/销售订单-后台-原型.html" },
            { id: "/trade/return", name: "退货管理", url: "./trade-return.html" },
        ],
    },
    {
        id: "finance",
        title: "财务管理",
        icon: "Money",
        children: [
            { id: "/finance/balance", name: "账户管理", url: "../spec/05-财务管理/账户管理-后台-原型.html" },
            { id: "/finance/payment", name: "资金流水", url: "../spec/05-财务管理/资金流水-后台-原型.html" },
            { id: "/finance/funds", name: "充值提现", url: "../spec/05-财务管理/充值提现-后台-原型.html" },
        ],
    },
    {
        id: "operation",
        title: "运营管理",
        icon: "Setting",
        children: [
            { id: "/operation/region", name: "销售大区", url: "../spec/07-运营管理/销售大区管理-原型.html" },
            { id: "/operation/salesman", name: "销售员管理", url: "../spec/07-运营管理/业务员管理-原型.html" },
            { id: "/operation/visit", name: "客户拜访", url: "../spec/07-运营管理/客户拜访管理-后台-原型.html" },
            { id: "/operation/warehouse", name: "仓库管理", url: "../spec/07-运营管理/仓库信息管理-原型.html" },
            { id: "/operation/message", name: "消息公告", url: "../spec/07-运营管理/公告管理-原型.html" },
        ],
    },
    {
        id: "report",
        title: "数据管理",
        icon: "DataAnalysis",
        children: [
            { id: "/report/product", name: "商品报表", url: "../spec/09-数据管理/商品统计报表-原型.html" },
            { id: "/report/customer", name: "客户报表", url: "../spec/09-数据管理/客户统计报表-原型.html" },
            { id: "/report/salesman", name: "销售员报表", url: "../spec/09-数据管理/销售员统计报表-原型.html" },
        ],
    },
    {
        id: "system",
        title: "系统管理",
        icon: "Tools",
        children: [
            { id: "/system/account", name: "账户管理", url: "../spec/06-基础管理/用户管理-原型.html" },
            { id: "/system/menu", name: "菜单管理", url: "../spec/06-基础管理/菜单管理-原型.html" },
            { id: "/system/role", name: "角色管理", url: "../spec/06-基础管理/角色管理-原型.html" },
            { id: "/system/permission", name: "数据权限", url: "./system-permission.html" },
            { id: "/system/dict", name: "字典管理", url: "../spec/06-基础管理/字典管理-原型.html" },
            { id: "/system/privacy", name: "小程序隐私政策", url: "../spec/06-基础管理/小程序隐私政策管理-原型.html" },
        ],
    },
];
