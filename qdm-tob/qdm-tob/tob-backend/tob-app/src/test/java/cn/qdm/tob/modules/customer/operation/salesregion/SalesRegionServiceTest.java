package cn.qdm.tob.modules.customer.operation.salesregion;

import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionSimpleDTO;
import cn.qdm.tob.modules.customer.operation.salesregion.domain.OprSalesRegion;
import cn.qdm.tob.modules.customer.operation.salesregion.mapper.SalesRegionMapper;
import cn.qdm.tob.modules.customer.operation.salesregion.service.SalesRegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("销售大区 Service")
class SalesRegionServiceTest {

    @Mock
    private SalesRegionMapper salesRegionMapper;

    private SalesRegionService salesRegionService;

    @BeforeEach
    void setUp() {
        salesRegionService = new SalesRegionService(null);
        ReflectionTestUtils.setField(salesRegionService, "baseMapper", salesRegionMapper);
    }

    @Test
    @DisplayName("listAll → 无数据应返回空列表")
    void shouldReturnEmptyListWhenNoData() {
        when(salesRegionMapper.selectList(null)).thenReturn(List.of());

        List<SalesRegionSimpleDTO> result = salesRegionService.listAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listAll → 有数据应返回 id + name DTO")
    void shouldReturnIdAndNameDto() {
        OprSalesRegion r1 = new OprSalesRegion();
        r1.setId(1L);
        r1.setName("华东大区");

        OprSalesRegion r2 = new OprSalesRegion();
        r2.setId(2L);
        r2.setName("华北大区");

        when(salesRegionMapper.selectList(null)).thenReturn(List.of(r1, r2));

        List<SalesRegionSimpleDTO> result = salesRegionService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("华东大区");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("华北大区");
    }
}
