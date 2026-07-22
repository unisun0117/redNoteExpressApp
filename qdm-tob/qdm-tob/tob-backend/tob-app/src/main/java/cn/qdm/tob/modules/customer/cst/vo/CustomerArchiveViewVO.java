package cn.qdm.tob.modules.customer.cst.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户档案详情视图对象
 */
@Data
@Schema(description = "客户档案详情")
public class CustomerArchiveViewVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "SAP客户编码")
    private String sapCustomerCode;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "营业执照编号")
    private String licenseNo;

    @Schema(description = "门头照URL")
    private String doorPhoto;

    @Schema(description = "营业执照照URL")
    private String licensePhoto;

    @Schema(description = "收货存放位置照片（JSON数组）")
    private String storagePhotos;

    @Schema(description = "收货人姓名")
    private String contactName;

    @Schema(description = "收货人联系电话")
    private String contactPhone;

    @Schema(description = "所在省份")
    private String province;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "所在区县")
    private String district;

    @Schema(description = "详细收货地址")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "可收货时段开始（HH:mm）")
    private String receiveTimeStart;

    @Schema(description = "可收货时段结束（HH:mm）")
    private String receiveTimeEnd;

    @Schema(description = "收货要求")
    private String receiveRequirement;

    @Schema(description = "归属销售大区ID")
    private Long salesRegionId;

    @Schema(description = "归属销售大区名称")
    private String salesRegionName;

    @Schema(description = "归属业务员ID")
    private Long salesmanId;

    @Schema(description = "归属业务员姓名")
    private String salesmanName;

    @Schema(description = "审核状态：PENDING/APPROVED/REJECTED")
    private String auditStatus;

    @Schema(description = "价格组")
    private String priceGroup;

    @Schema(description = "结算公司")
    private String settleCompany;

    @Schema(description = "经营类型")
    private String businessType;

    @Schema(description = "结算类型：CASH/PERIOD")
    private String settleType;

    @Schema(description = "内部收货备注")
    private String internalRemark;

    @Schema(description = "审核处理人ID")
    private Long auditorId;

    @Schema(description = "审核处理人姓名")
    private String auditorName;

    @Schema(description = "审核人类型：SALESMAN/MANAGER")
    private String auditorType;

    @Schema(description = "审核驳回原因")
    private String auditRejectReason;

    @Schema(description = "审核处理时间")
    private LocalDateTime auditTime;

    @Schema(description = "提交用户ID")
    private Long submitUserId;

    @Schema(description = "提交用户姓名")
    private String submitUserName;

    @Schema(description = "最近下单时间")
    private LocalDateTime lastOrderTime;

    @Schema(description = "创建/提交时间")
    private LocalDateTime createdAt;

    @Schema(description = "最近更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "审核历史记录")
    private List<AuditLogVO> auditLogs;

    @Schema(description = "已绑定用户列表")
    private List<BoundUserVO> boundUsers;

    /**
     * 审核历史记录VO
     */
    @Data
    @Schema(description = "审核历史记录")
    public static class AuditLogVO {
        @Schema(description = "记录ID")
        private Long id;

        @Schema(description = "操作时间")
        private LocalDateTime createdAt;

        @Schema(description = "操作动作")
        private String action;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "审批意见")
        private String remark;
    }

    /**
     * 已绑定用户VO
     */
    @Data
    @Schema(description = "已绑定用户")
    public static class BoundUserVO {
        @Schema(description = "绑定ID")
        private Long id;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户姓名")
        private String userName;

        @Schema(description = "用户手机号")
        private String userMobile;

        @Schema(description = "成员角色")
        private String memberRole;

        @Schema(description = "绑定时间")
        private LocalDateTime createdAt;
    }
}
