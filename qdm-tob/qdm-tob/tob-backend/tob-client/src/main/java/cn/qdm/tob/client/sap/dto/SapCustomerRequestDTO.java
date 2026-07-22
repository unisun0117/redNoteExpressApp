package cn.qdm.tob.client.sap.dto;

import cn.qdm.tob.framework.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author zhaoxiaoyun
 */
@Data
public class SapCustomerRequestDTO {
    /**
     * 操作类型：1-新增，2-修改
     */
    @JsonProperty("ZZSTUS")
    private String action;

    /**
     * OA单号
     */
    @JsonProperty("ZOANUM")
    private String oaNo;

    /**
     * 客户编号
     */
    @JsonProperty("PARTNER")
    private String no;

    /**
     * 业务伙伴分组--固定值=Z007-大客户
     */
    @JsonProperty(value = "BU_GROUP", defaultValue = "Z007")
    private String group;

    /**
     * 业务伙伴类型--固定值=Y002-大客户
     */
    @JsonProperty(value = "BPKIND", defaultValue = "Y002")
    private String kind;

    /**
     * 客户名称
     */
    @JsonProperty("NAME1")
    private String name;

    /**
     * 搜索词
     */
    @JsonProperty("SORT1")
    private String keyword;

    /**
     * 国家/地区代码--新增时，必填 暂时固定中国 CN
     */
    @JsonProperty(value = "LAND1", defaultValue = "CN")
    private String country;

    /**
     * 省/自治区/直辖市--新增时，必填
     */
    @JsonProperty("REGIO")
    private String province;

    /**
     * 城市--新增时，必填
     */
    @JsonProperty("CITY1")
    private String city;

    /**
     * 行政区
     */
    @JsonProperty("ZXZQ")
    private String area;

    /**
     * 街道--新增时，必填
     */
    @JsonProperty("STREET")
    private String street;

    /**
     * 电话号码：区号 + 号码
     */
    @JsonProperty("TEL_NUMBER")
    private String telephone;

    /**
     * 语言代码--新增时，必填 中文：1
     */
    @JsonProperty(value = "SPRAS", defaultValue = "1")
    private String locales;

    /**
     * 到货开始时间，如果没值不能传空，必须是000000
     */
    @JsonProperty(value = "ZDLVBETIME", defaultValue = "000000")
    @JsonFormat(pattern = Constants.PLAIN_DATE_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate receivingStartTime;

    /**
     * 到货结束时间，如果没值不能传空，必须是000000
     */
    @JsonProperty(value = "ZDLVENTIME", defaultValue = "000000")
    @JsonFormat(pattern = Constants.PLAIN_DATE_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate receivingEndTime;

    /**
     * 收货联系人姓名
     */
    @JsonProperty("ZRCVR_NAME")
    private String consigneeName;

    /**
     * 收货联系人电话
     */
    @JsonProperty("ZRCVR_TEL")
    private String consigneeTel;

    /**
     * 门店照片
     */
    @JsonProperty("ZMTZP")
    private String shopPhoto;
}
