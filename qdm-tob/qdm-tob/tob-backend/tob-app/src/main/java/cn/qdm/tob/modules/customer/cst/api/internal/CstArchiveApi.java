package cn.qdm.tob.modules.customer.cst.api.internal;

import cn.qdm.tob.modules.customer.cst.api.internal.dto.CstArchiveDTO;
import cn.qdm.tob.modules.customer.cst.domain.CstCompanyArchive;
import cn.qdm.tob.modules.customer.cst.mapper.CustomerArchiveMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户档案内部 API — 纯查询，业务逻辑委托 Service
 */
@Component
@RequiredArgsConstructor
public class CstArchiveApi {

    private final CustomerArchiveMapper archiveMapper; 

    /** 按ID查询 */
    public CstArchiveDTO getArchiveById(Long archiveId) {
        CstCompanyArchive entity = archiveMapper.selectById(archiveId);
        if (entity == null) return null;
        CstArchiveDTO dto = new CstArchiveDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setSapCustomerCode(entity.getSapCustomerCode());
        dto.setSalesRegionId(entity.getSalesRegionId());
        dto.setSalesRegionName(entity.getSalesRegionName());
        dto.setPriceGroup(entity.getPriceGroup());
        dto.setSettleType(entity.getSettleType());
        dto.setSettleCompany(entity.getSettleCompany());
        dto.setAuditStatus(entity.getAuditStatus());
        dto.setContactName(entity.getContactName());
        dto.setContactPhone(entity.getContactPhone());
        dto.setAddress(entity.getAddress());
        dto.setProvince(entity.getProvince());
        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setSalesmanId(entity.getSalesmanId());
        dto.setSalesmanName(entity.getSalesmanName());
        return dto;
    }


    
    public CstArchiveDTO getArchiveByCode(String sapCustomerCode) {
        CstCompanyArchive entity = archiveMapper.lambdaSelectOne(
                        w -> w.eq(CstCompanyArchive::getSapCustomerCode, sapCustomerCode))
                .orElse(null);
        return entity != null ? toDTO(entity) : null;
    }

    
    public List<CstArchiveDTO> getArchivesByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        return archiveMapper.selectList(
                        new LambdaQueryWrapper<CstCompanyArchive>()
                                .in(CstCompanyArchive::getSapCustomerCode, codes))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    
    public List<CstArchiveDTO> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        return archiveMapper.selectList(
                        new LambdaQueryWrapper<CstCompanyArchive>()
                                .and(w -> w
                                        .like(CstCompanyArchive::getSapCustomerCode, keyword)
                                        .or()
                                        .like(CstCompanyArchive::getCompanyName, keyword)))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    
    public List<CstArchiveDTO> searchByLicenseNo(String licenseNo) {
        if (licenseNo == null || licenseNo.isBlank()) {
            return Collections.emptyList();
        }
        return archiveMapper.selectList(
                        new LambdaQueryWrapper<CstCompanyArchive>()
                                .eq(CstCompanyArchive::getLicenseNo, licenseNo))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ================================================================
    // Entity → DTO
    // ================================================================

    private CstArchiveDTO toDTO(CstCompanyArchive entity) {
        CstArchiveDTO dto = new CstArchiveDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setSapCustomerCode(entity.getSapCustomerCode());
        dto.setSalesRegionId(entity.getSalesRegionId());
        dto.setSalesRegionName(entity.getSalesRegionName());
        dto.setPriceGroup(entity.getPriceGroup());
        dto.setSettleType(entity.getSettleType());
        dto.setSettleCompany(entity.getSettleCompany());
        dto.setAuditStatus(entity.getAuditStatus());
        dto.setContactName(entity.getContactName());
        dto.setContactPhone(entity.getContactPhone());
        dto.setAddress(entity.getAddress());
        dto.setProvince(entity.getProvince());
        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setSalesmanId(entity.getSalesmanId());
        dto.setSalesmanName(entity.getSalesmanName());
        dto.setLicenseNo(entity.getLicenseNo());
        dto.setLicensePhoto(entity.getLicensePhoto());
        return dto;
    }
}
