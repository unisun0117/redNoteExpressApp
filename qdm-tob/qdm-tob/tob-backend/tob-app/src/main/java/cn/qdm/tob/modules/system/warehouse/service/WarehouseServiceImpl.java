package cn.qdm.tob.modules.system.warehouse.service;

import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.warehouse.api.internal.WarehouseApi;
import cn.qdm.tob.modules.system.warehouse.api.internal.dto.WarehouseDTO;
import cn.qdm.tob.modules.system.warehouse.domain.SysWarehouse;
import cn.qdm.tob.modules.system.warehouse.mapper.SysWarehouseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库档案查询服务实现。
 * <p>
 * 实现 {@link WarehouseApi}，供其他模块通过接口调用。
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends TobBaseService<SysWarehouseMapper, SysWarehouse> implements WarehouseApi {

    @Override
    public List<WarehouseDTO> listByRegion(String region) {
        LambdaQueryWrapper<SysWarehouse> wrapper = new LambdaQueryWrapper<>();
        if (region != null && !region.isBlank()) {
            wrapper.eq(SysWarehouse::getRegion, region);
        }
        return baseMapper.selectList(wrapper).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseDTO getByCode(String code) {
        SysWarehouse entity = baseMapper.selectOne(
                new LambdaQueryWrapper<SysWarehouse>()
                        .eq(SysWarehouse::getCode, code));
        return entity == null ? null : toDTO(entity);
    }

    private WarehouseDTO toDTO(SysWarehouse entity) {
        return WarehouseDTO.builder()
                .code(entity.getCode())
                .name(entity.getName())
                .region(entity.getRegion())
                .type(entity.getType())
                .province(entity.getProvince())
                .city(entity.getCity())
                .district(entity.getDistrict())
                .address(entity.getAddress())
                .lng(entity.getLng())
                .lat(entity.getLat())
                .provinceName(entity.getProvinceName())
                .cityName(entity.getCityName())
                .districtName(entity.getDistrictName())
                .build();
    }
}
