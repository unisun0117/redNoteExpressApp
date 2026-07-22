package cn.qdm.tob.client.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * SAP供应商查询响应实体
 */
@Data
public class SapVendorDTO {
    /**
     * 供应商编码
     */
    @JsonProperty("venderid")
    private String id;

    /**
     * 供应商类型
     */
    @JsonProperty("partnertype")
    private String type;

    /**
     * 供应商分组
     */
    @JsonProperty("vendergroup")
    private String group;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 名称
     */
    @JsonProperty("vendorname")
    private String name;

    /**
     * 简称
     */
    @JsonProperty("vendornameshort")
    private String shortName;

    /**
     * 地址
     */
    private String address;

    /**
     * 城市ID
     */
    @JsonProperty("cityid")
    private String cityId;

    /**
     * 国家编码
     */
    @JsonProperty("countryid")
    private String countryId;

    /**
     * 省份ID
     */
    @JsonProperty("provinceid")
    private String provinceId;

    /**
     * 省份
     */
    @JsonProperty("provincedesc")
    private String province;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 税分类
     */
    @JsonProperty("taxtype")
    private String taxType;

    /**
     * 税号
     */
    @JsonProperty("taxnum")
    private String taxNum;

    /**
     * 称谓
     */
    private String title;

    /**
     * 关联编码
     */
    @JsonProperty("relvendorid")
    private String relVendorId;

    /**
     * 银行国家
     */
    @JsonProperty("bankcountry")
    private String bankCountry;

    /**
     * 银行编码
     */
    @JsonProperty("bankid")
    private String bankId;

    /**
     * 银行账户
     */
    @JsonProperty("bankaccount")
    private String bankAccount;

    /**
     * 银行名称
     */
    @JsonProperty("bankname")
    private String bankName;

    /**
     * 公司性质
     */
    @JsonProperty("companytype")
    private String companyType;

    /**
     * 主营业务
     */
    @JsonProperty("mainbusiness")
    private String mainBusiness;

    /**
     * 注册资金
     */
    @JsonProperty("registerfund")
    private String registerFund;

    /**
     * 业务模式
     */
    @JsonProperty("businessmodel")
    private String businessMode;

    /**
     * B2B大客户标记
     */
    @JsonProperty("b2bflag")
    private String b2bFlag;
}
