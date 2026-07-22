package cn.qdm.tob.modules.customer.cst.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.customer.cst.dto.CustomerArchiveQuery;
import cn.qdm.tob.modules.customer.cst.service.CustomerArchiveService;
import cn.qdm.tob.modules.customer.cst.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户档案后台管理 API
 */
@Tag(name = "客户档案管理")
@RestController
@RequestMapping("/api/admin/customer-archive")
@RequiredArgsConstructor
public class CustomerArchiveController {

    private final CustomerArchiveService service;

    /**
     * 分页查询客户档案列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询客户档案列表")
    public ResponseResult<Page<CustomerArchiveSummaryVO>> page(
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
        @Parameter(description = "公司名称") @RequestParam(required = false) String companyName,
        @Parameter(description = "销售大区名称") @RequestParam(required = false) String salesRegionName,
        @Parameter(description = "省份") @RequestParam(required = false) String province,
        @Parameter(description = "城市") @RequestParam(required = false) String city,
        @Parameter(description = "区县") @RequestParam(required = false) String district,
        @Parameter(description = "业务员姓名") @RequestParam(required = false) String salesmanName,
        @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
        @Parameter(description = "审核人姓名") @RequestParam(required = false) String auditorName,
        @Parameter(description = "注册时间起始") @RequestParam(required = false) String startDate,
        @Parameter(description = "注册时间结束") @RequestParam(required = false) String endDate) {

        CustomerArchiveQuery query = new CustomerArchiveQuery();
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setCompanyName(companyName);
        query.setSalesRegionName(salesRegionName);
        query.setProvince(province);
        query.setCity(city);
        query.setDistrict(district);
        query.setSalesmanName(salesmanName);
        query.setAuditStatus(auditStatus);
        query.setAuditorName(auditorName);
        query.setStartDate(startDate);
        query.setEndDate(endDate);

        return ResponseResult.success(service.page(query));
    }

    /**
     * 查询客户档案详情
     */
    @GetMapping("/detail")
    @Operation(summary = "查询客户档案详情")
    public ResponseResult<CustomerArchiveViewVO> detail(
        @Parameter(description = "档案ID", required = true) @RequestParam Long id) {
        return ResponseResult.success(service.getById(id));
    }

    /**
     * 后台新增客户档案
     */
    @PostMapping("/create")
    @Operation(summary = "后台新增客户档案")
    public ResponseResult<Void> create(@Valid @RequestBody CustomerArchiveCreateVO vo) {
        service.create(vo, "管理员");
        return ResponseResult.success();
    }

    /**
     * 编辑业务属性（仅已通过状态可编辑）
     */
    @PostMapping("/edit")
    @Operation(summary = "编辑业务属性")
    public ResponseResult<Void> edit(
        @Parameter(description = "档案ID", required = true) @RequestParam Long id,
        @Valid @RequestBody CustomerArchiveEditVO vo) {
        service.edit(id, vo, "管理员");
        return ResponseResult.success();
    }

    /**
     * 手动分配审核人（仅待审核状态可分配）
     */
    @PostMapping("/assign-auditor")
    @Operation(summary = "手动分配审核人")
    public ResponseResult<Void> assignAuditor(
        @Parameter(description = "档案ID", required = true) @RequestParam Long id,
        @Valid @RequestBody AssignAuditorVO vo) {
        service.assignAuditor(id, vo, null, "管理员");
        return ResponseResult.success();
    }

    /**
     * 查询已绑定用户列表
     */
    @GetMapping("/bound-users")
    @Operation(summary = "查询已绑定用户列表")
    public ResponseResult<List<CustomerArchiveViewVO.BoundUserVO>> boundUsers(
        @Parameter(description = "档案ID", required = true) @RequestParam Long id) {
        CustomerArchiveViewVO view = service.getById(id);
        return ResponseResult.success(view.getBoundUsers());
    }

    /**
     * 搜索小程序用户（用于绑定）
     */
    @GetMapping("/search-users")
    @Operation(summary = "搜索小程序用户")
    public ResponseResult<List<CustomerArchiveViewVO.BoundUserVO>> searchUsers(
        @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.searchUsers(keyword));
    }

    /**
     * 绑定小程序用户
     */
    @PostMapping("/bind-user")
    @Operation(summary = "绑定小程序用户")
    public ResponseResult<Void> bindUser(
        @Parameter(description = "档案ID", required = true) @RequestParam Long id,
        @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
        @Parameter(description = "用户姓名") @RequestParam(required = false) String userName,
        @Parameter(description = "用户手机号") @RequestParam(required = false) String userMobile) {
        service.bindUser(id, userId, userName, userMobile, "管理员");
        return ResponseResult.success();
    }

    /**
     * 解绑小程序用户
     */
    @PostMapping("/unbind-user")
    @Operation(summary = "解绑小程序用户")
    public ResponseResult<Void> unbindUser(
        @Parameter(description = "档案ID", required = true) @RequestParam Long id,
        @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        service.unbindUser(id, userId, "管理员");
        return ResponseResult.success();
    }
}
