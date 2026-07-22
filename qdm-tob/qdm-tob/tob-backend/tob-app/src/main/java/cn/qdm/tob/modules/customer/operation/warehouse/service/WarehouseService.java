package cn.qdm.tob.modules.customer.operation.warehouse.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.customer.operation.warehouse.domain.OprWarehouse;
import cn.qdm.tob.modules.customer.operation.warehouse.mapper.WarehouseMapper;
import cn.qdm.tob.modules.customer.operation.warehouse.vo.WarehouseCreationVO;
import cn.qdm.tob.modules.customer.operation.warehouse.vo.WarehouseEditVO;
import cn.qdm.tob.modules.customer.operation.warehouse.vo.WarehouseViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService extends TobBaseService<WarehouseMapper, OprWarehouse> {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initColumns() {
        try { jdbcTemplate.execute("ALTER TABLE sys_warehouse ADD COLUMN province_name VARCHAR(30) NULL"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("ALTER TABLE sys_warehouse ADD COLUMN city_name VARCHAR(30) NULL"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("ALTER TABLE sys_warehouse ADD COLUMN district_name VARCHAR(30) NULL"); } catch (Exception ignored) {}
    }

    public Page<WarehouseViewVO> page(Integer pageNum, Integer pageSize, String keyword, String region) {
        LambdaQueryWrapper<OprWarehouse> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank())
            w.and(q -> q.like(OprWarehouse::getCode, keyword).or().like(OprWarehouse::getName, keyword));
        if (region != null && !region.isBlank())
            w.eq(OprWarehouse::getRegion, region);
        w.orderByDesc(OprWarehouse::getUpdatedAt);
        Page<OprWarehouse> ep = baseMapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<WarehouseViewVO> vos = ep.getRecords().stream().map(e -> {
            WarehouseViewVO vo = new WarehouseViewVO();
            vo.setId(String.valueOf(e.getId()));
            vo.setCode(e.getCode()); vo.setName(e.getName()); vo.setRegion(e.getRegion());
            vo.setType(e.getType()); vo.setProvince(e.getProvince()); vo.setCity(e.getCity());
            vo.setDistrict(e.getDistrict());
            vo.setProvinceName(e.getProvinceName()); vo.setCityName(e.getCityName()); vo.setDistrictName(e.getDistrictName());
            vo.setAddress(e.getAddress());
            vo.setLng(e.getLng()); vo.setLat(e.getLat());
            vo.setUpdatedBy(e.getUpdatedBy()); vo.setUpdatedAt(e.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
        Page<WarehouseViewVO> vp = new Page<>(pageNum, pageSize, ep.getTotal());
        vp.setRecords(vos);
        return vp;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(WarehouseCreationVO vo) {
        AssertUtils.isNull(baseMapper.selectOne(
            new LambdaQueryWrapper<OprWarehouse>().eq(OprWarehouse::getCode, vo.getCode())), "仓库编码已存在");
        OprWarehouse e = new OprWarehouse();
        e.setCode(vo.getCode()); e.setName(vo.getName()); e.setRegion(vo.getRegion());
        e.setType(vo.getType()); e.setProvince(vo.getProvince()); e.setCity(vo.getCity());
        e.setDistrict(vo.getDistrict()); e.setAddress(vo.getAddress());
        e.setProvinceName(vo.getProvinceName()); e.setCityName(vo.getCityName()); e.setDistrictName(vo.getDistrictName());
        e.setLng(vo.getLng()); e.setLat(vo.getLat());
        e.setCreatedBy(vo.getCreatedBy()); e.setUpdatedBy(vo.getCreatedBy());
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        e.setCreatedAt(now); e.setUpdatedAt(now);
        baseMapper.insert(e);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(String code, WarehouseEditVO vo) {
        OprWarehouse e = baseMapper.selectOne(
            new LambdaQueryWrapper<OprWarehouse>().eq(OprWarehouse::getCode, code));
        AssertUtils.notNull(e, "仓库不存在");
        e.setName(vo.getName()); e.setRegion(vo.getRegion()); e.setType(vo.getType());
        e.setProvince(vo.getProvince()); e.setCity(vo.getCity()); e.setDistrict(vo.getDistrict());
        e.setProvinceName(vo.getProvinceName()); e.setCityName(vo.getCityName()); e.setDistrictName(vo.getDistrictName());
        e.setAddress(vo.getAddress()); e.setLng(vo.getLng()); e.setLat(vo.getLat());
        e.setUpdatedBy(vo.getUpdatedBy());
        e.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        baseMapper.updateById(e);
    }
}
