package cn.qdm.tob.modules.customer.cst.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户公司档案实体
 */
@Data
@TableName("cst_company_archive")
public class CstCompanyArchive {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** SAP客户编码 */
    private String sapCustomerCode;

    /** 公司名称 */
    private String companyName;

    /** 营业执照编号 */
    private String licenseNo;

    /** 门头照URL */
    private String doorPhoto;

    /** 营业执照照URL */
    private String licensePhoto;

    /** 收货存放位置照片（JSON数组） */
    private String storagePhotos;

    /** 收货人姓名 */
    private String contactName;

    /** 收货人联系电话 */
    private String contactPhone;

    /** 所在省份 */
    private String province;

    /** 所在城市 */
    private String city;

    /** 所在区县 */
    private String district;

    /** 详细收货地址 */
    private String address;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 可收货时段开始（HH:mm） */
    private String receiveTimeStart;

    /** 可收货时段结束（HH:mm） */
    private String receiveTimeEnd;

    /** 收货要求（备注） */
    private String receiveRequirement;

    /** 归属销售大区ID */
    private Long salesRegionId;

    /** 归属销售大区名称 */
    private String salesRegionName;

    /** 归属业务员ID */
    private Long salesmanId;

    /** 归属业务员姓名 */
    private String salesmanName;

    /** 提交时填写的业务员推荐码 */
    private String referralCode;

    /** 审核状态：PENDING/APPROVED/REJECTED */
    private String auditStatus;

    /** 价格组 */
    private String priceGroup;

    /** 结算公司 */
    private String settleCompany;

    /** 经营类型 */
    private String businessType;

    /** 结算类型：CASH/PERIOD */
    private String settleType;

    /** 内部收货备注 */
    private String internalRemark;

    /** 审核处理人ID */
    private Long auditorId;

    /** 审核处理人姓名 */
    private String auditorName;

    /** 审核人类型：SALESMAN/MANAGER */
    private String auditorType;

    /** 审核驳回原因 */
    private String auditRejectReason;

    /** 审核处理时间 */
    private LocalDateTime auditTime;

    /** 提交用户ID */
    private Long submitUserId;

    /** 提交用户姓名 */
    private String submitUserName;

    /** 最近下单时间 */
    private LocalDateTime lastOrderTime;

    /** 创建/提交时间 */
    private LocalDateTime createdAt;

    /** 最近更新时间 */
    private LocalDateTime updatedAt;
}
