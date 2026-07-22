package cn.qdm.tob.modules.product.pricing.api.internal;

import cn.qdm.tob.modules.product.pricing.api.internal.dto.PriceDetailDTO;
import cn.qdm.tob.modules.product.pricing.service.PriceDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 价格明细内部 API — 委托给 PriceDetailService
 */
@Component
@RequiredArgsConstructor
public class PriceDetailApi {

    private final PriceDetailService priceDetailService;

    public List<PriceDetailDTO> listByBarcodes(String priceGroupCode, List<String> barcodes) {
        return priceDetailService.listByBarcodes(priceGroupCode, barcodes);
    }
}
