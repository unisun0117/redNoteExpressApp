package cn.qdm.tob.modules.customer.cst.api.mall;

import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.customer.cst.domain.CstArchiveUserBinding;
import cn.qdm.tob.modules.customer.cst.domain.CstCompanyArchive;
import cn.qdm.tob.modules.customer.cst.service.CustomerArchiveService;
import cn.qdm.tob.modules.customer.cst.vo.mall.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序商城端 — 公司收货地址 API
 */
@Slf4j
@Tag(name = "小程序 - 公司收货地址")
@RestController
@RequestMapping("/api/mall/customer/address")
@RequiredArgsConstructor
public class CustomerAddressMallController {

    private final CustomerArchiveService service;

    // ------------------------------------------------------------------
    // 辅助方法
    // ------------------------------------------------------------------

    private Long getCurrentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return 1L; // 开发调试用模拟用户
    }

    private String getCurrentUserName(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getName();
        }
        return "测试用户";
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String buildFullAddress(String province, String city, String district, String address) {
        StringBuilder sb = new StringBuilder();
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (district != null) sb.append(district);
        if (address != null) sb.append(address);
        return sb.toString();
    }

    // ==================================================================
    //  API 端点
    // ==================================================================

    /**
     * 获取当前用户的地址列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取当前用户的地址列表")
    public ResponseResult<Page<CustomerAddressListVO>> list(
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
        Authentication auth) {

        Long userId = getCurrentUserId(auth);
        Page<CstCompanyArchive> entityPage = service.getMallList(userId, pageNum, pageSize);

        List<CustomerAddressListVO> voList = new ArrayList<>();
        for (CstCompanyArchive entity : entityPage.getRecords()) {
            boolean isAdmin = entity.getSubmitUserId() != null && entity.getSubmitUserId().equals(userId);

            voList.add(CustomerAddressListVO.builder()
                .id(entity.getId())
                .companyName(entity.getCompanyName())
                .contactName(entity.getContactName())
                .contactPhone(maskPhone(entity.getContactPhone()))
                .province(entity.getProvince())
                .city(entity.getCity())
                .district(entity.getDistrict())
                .address(entity.getAddress())
                .fullAddress(buildFullAddress(entity.getProvince(), entity.getCity(), entity.getDistrict(), entity.getAddress()))
                .auditStatus(entity.getAuditStatus())
                .auditRejectReason(entity.getAuditRejectReason())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .isAdmin(isAdmin)
                .build());
        }

        Page<CustomerAddressListVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(voList);
        return ResponseResult.success(voPage);
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/detail")
    @Operation(summary = "获取地址详情")
    public ResponseResult<CustomerAddressDetailVO> detail(
        @Parameter(description = "地址ID", required = true) @RequestParam Long id,
        Authentication auth) {

        Long userId = getCurrentUserId(auth);
        CstCompanyArchive archive = service.getEntityById(id);

        if (archive == null) {
            throw new TobServiceException(404, "地址不存在");
        }

        boolean isAdmin = archive.getSubmitUserId() != null && archive.getSubmitUserId().equals(userId);

        List<String> storagePhotoList = new ArrayList<>();
        if (archive.getStoragePhotos() != null && !archive.getStoragePhotos().isBlank()) {
            String[] parts = archive.getStoragePhotos().split(",");
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    storagePhotoList.add(trimmed);
                }
            }
        }

        CustomerAddressDetailVO vo = CustomerAddressDetailVO.builder()
            .id(archive.getId())
            .companyName(archive.getCompanyName())
            .doorPhoto(archive.getDoorPhoto())
            .licenseNo(archive.getLicenseNo())
            .licensePhoto(archive.getLicensePhoto())
            .contactName(archive.getContactName())
            .contactPhone(archive.getContactPhone())
            .province(archive.getProvince())
            .city(archive.getCity())
            .district(archive.getDistrict())
            .address(archive.getAddress())
            .fullAddress(buildFullAddress(archive.getProvince(), archive.getCity(), archive.getDistrict(), archive.getAddress()))
            .receiveTimeStart(archive.getReceiveTimeStart())
            .receiveTimeEnd(archive.getReceiveTimeEnd())
            .receiveRequirement(archive.getReceiveRequirement())
            .storagePhotos(storagePhotoList)
            .auditStatus(archive.getAuditStatus())
            .auditRejectReason(archive.getAuditRejectReason())
            .isAdmin(isAdmin)
            .build();

        return ResponseResult.success(vo);
    }

    /**
     * 提交新地址
     */
    @PostMapping("/create")
    @Operation(summary = "提交新地址")
    public ResponseResult<Map<String, Long>> create(@Valid @RequestBody CustomerAddressSubmitVO vo, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        String userName = getCurrentUserName(auth);

        CstCompanyArchive entity = new CstCompanyArchive();
        entity.setCompanyName(vo.getCompanyName());
        entity.setLicenseNo(vo.getLicenseNo());
        entity.setDoorPhoto(vo.getDoorPhoto());
        entity.setLicensePhoto(vo.getLicensePhoto());
        entity.setContactName(vo.getContactName());
        entity.setContactPhone(vo.getContactPhone());
        entity.setProvince(vo.getProvince());
        entity.setCity(vo.getCity());
        entity.setDistrict(vo.getDistrict());
        entity.setAddress(vo.getAddress());

        if (vo.getLongitude() != null && !vo.getLongitude().isBlank()) {
            entity.setLongitude(new BigDecimal(vo.getLongitude()));
        }
        if (vo.getLatitude() != null && !vo.getLatitude().isBlank()) {
            entity.setLatitude(new BigDecimal(vo.getLatitude()));
        }

        entity.setReceiveTimeStart(vo.getReceiveTimeStart() != null ? vo.getReceiveTimeStart() : "00:00");
        entity.setReceiveTimeEnd(vo.getReceiveTimeEnd() != null ? vo.getReceiveTimeEnd() : "08:00");
        entity.setReceiveRequirement(vo.getReceiveRequirement());
        entity.setReferralCode(vo.getReferralCode());

        if (vo.getStoragePhotos() != null && !vo.getStoragePhotos().isEmpty()) {
            entity.setStoragePhotos(String.join(",", vo.getStoragePhotos()));
        }

        Long id = service.submitAddress(entity, userId, userName);

        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return ResponseResult.success(result);
    }

    /**
     * 编辑驳回后重新提交地址
     */
    @PostMapping("/update")
    @Operation(summary = "编辑驳回后重新提交")
    public ResponseResult<Map<String, Long>> update(
        @Parameter(description = "地址ID", required = true) @RequestParam Long id,
        @Valid @RequestBody CustomerAddressSubmitVO vo,
        Authentication auth) {

        Long userId = getCurrentUserId(auth);
        String userName = getCurrentUserName(auth);

        CstCompanyArchive entity = new CstCompanyArchive();
        entity.setCompanyName(vo.getCompanyName());
        entity.setLicenseNo(vo.getLicenseNo());
        entity.setDoorPhoto(vo.getDoorPhoto());
        entity.setLicensePhoto(vo.getLicensePhoto());
        entity.setContactName(vo.getContactName());
        entity.setContactPhone(vo.getContactPhone());
        entity.setProvince(vo.getProvince());
        entity.setCity(vo.getCity());
        entity.setDistrict(vo.getDistrict());
        entity.setAddress(vo.getAddress());

        if (vo.getLongitude() != null && !vo.getLongitude().isBlank()) {
            entity.setLongitude(new BigDecimal(vo.getLongitude()));
        }
        if (vo.getLatitude() != null && !vo.getLatitude().isBlank()) {
            entity.setLatitude(new BigDecimal(vo.getLatitude()));
        }

        entity.setReceiveTimeStart(vo.getReceiveTimeStart() != null ? vo.getReceiveTimeStart() : "00:00");
        entity.setReceiveTimeEnd(vo.getReceiveTimeEnd() != null ? vo.getReceiveTimeEnd() : "08:00");
        entity.setReceiveRequirement(vo.getReceiveRequirement());

        if (vo.getStoragePhotos() != null && !vo.getStoragePhotos().isEmpty()) {
            entity.setStoragePhotos(String.join(",", vo.getStoragePhotos()));
        }

        service.resubmitAddress(id, entity, userId, userName);

        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return ResponseResult.success(result);
    }

    /**
     * 验证推荐码
     */
    @GetMapping("/verify-referral")
    @Operation(summary = "验证推荐码")
    public ResponseResult<ReferralCodeVerifyVO> verifyReferral(
        @Parameter(description = "推荐码", required = true) @RequestParam String referralCode) {

        try {
            service.verifyReferralCode(referralCode);
            return ResponseResult.success(ReferralCodeVerifyVO.builder()
                .valid(true)
                .salespersonName("业务员")
                .build());
        } catch (Exception e) {
            return ResponseResult.success(ReferralCodeVerifyVO.builder()
                .valid(false)
                .message(e.getMessage())
                .build());
        }
    }

    /**
     * 获取地址成员列表
     */
    @GetMapping("/members")
    @Operation(summary = "获取地址成员列表")
    public ResponseResult<List<AddressMemberVO>> members(
        @Parameter(description = "地址ID", required = true) @RequestParam Long addressId) {

        List<CstArchiveUserBinding> bindings = service.getMembers(addressId);
        List<AddressMemberVO> voList = new ArrayList<>();

        for (CstArchiveUserBinding binding : bindings) {
            voList.add(AddressMemberVO.builder()
                .id(binding.getId())
                .userId(binding.getUserId())
                .userName(binding.getUserName())
                .userMobile(maskPhone(binding.getUserMobile()))
                .createdAt(binding.getCreatedAt() != null ? binding.getCreatedAt().toString() : null)
                .isAdmin("ADMIN".equals(binding.getMemberRole()))
                .build());
        }

        return ResponseResult.success(voList);
    }

    /**
     * 生成邀请码
     */
    @PostMapping("/generate-invite")
    @Operation(summary = "生成邀请码")
    public ResponseResult<InviteCodeVO> generateInvite(
        @Parameter(description = "地址ID", required = true) @RequestParam Long addressId,
        Authentication auth) {

        Long userId = getCurrentUserId(auth);
        String inviteCode = service.generateInviteCode(addressId, userId);

        return ResponseResult.success(InviteCodeVO.builder()
            .inviteCode(inviteCode)
            .expireTime("24小时")
            .build());
    }

    /**
     * 确认加入地址
     */
    @PostMapping("/join")
    @Operation(summary = "确认加入地址")
    public ResponseResult<Map<String, Long>> join(
        @Parameter(description = "邀请码", required = true) @RequestParam String inviteCode,
        Authentication auth) {

        Long userId = getCurrentUserId(auth);
        String userName = getCurrentUserName(auth);

        service.joinByInviteCode(inviteCode, userId, userName, "138****0000");

        Map<String, Long> result = new HashMap<>();
        result.put("addressId", 1L);
        return ResponseResult.success(result);
    }

    /**
     * 解绑成员
     */
    @PostMapping("/unbind")
    @Operation(summary = "解绑成员")
    public ResponseResult<Void> unbind(
        @Parameter(description = "地址ID", required = true) @RequestParam Long addressId,
        @Parameter(description = "目标用户ID", required = true) @RequestParam Long userId,
        Authentication auth) {

        Long operatorUserId = getCurrentUserId(auth);
        service.unbindMallMember(addressId, userId, operatorUserId);
        return ResponseResult.success();
    }

    /**
     * 验证邀请码并获取地址信息
     */
    @GetMapping("/verify-invite")
    @Operation(summary = "验证邀请码并获取地址信息")
    public ResponseResult<Map<String, Object>> verifyInvite(
        @Parameter(description = "邀请码", required = true) @RequestParam String inviteCode) {

        Map<String, Object> result = new HashMap<>();

        if (inviteCode == null || inviteCode.isBlank()) {
            result.put("valid", false);
            result.put("expired", true);
            return ResponseResult.success(result);
        }

        result.put("valid", true);

        Map<String, String> address = new HashMap<>();
        address.put("companyName", "模拟公司");
        address.put("fullAddress", "模拟地址详情");
        address.put("receiverName", "收货人");
        address.put("receiverPhone", "138****8888");
        result.put("address", address);

        return ResponseResult.success(result);
    }
}
