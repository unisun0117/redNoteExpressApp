package cn.qdm.tob.modules.order.account.dto;

import lombok.Data;

/**
 * 客户账户查询参数
 */
@Data
public class CustomerAccountQuery {

    /** 客户编号/名称联合模糊搜索（先查客户表获取匹配 code 列表） */
    private String keyword;

    /** 营业执照编号精确搜索（先查客户表获取匹配 code） */
    private String licenseNo;

    /** 账户类型：PREPAID / CREDIT */
    private String accountType;
}
