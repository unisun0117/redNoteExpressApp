package cn.qdm.tob.client.wms;

import cn.qdm.tob.client.wms.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@FeignClient(value = "wms-client", url = "${tob.client.wms.base-url}", path = "fam/Comm2API", configuration = WmsClientConfiguration.class)
public interface WmsClient {
    @PostMapping("TowB_CreateTailGoodsOrder")
    WmsResponseBaseDTO<List<WmsOrderResponseDTO>> pushOrder(@RequestHeader("WmsCode") String dcId,
                                                            @RequestBody WmsOrderRequestDTO req);

    @PostMapping("GetTestingPics")
    WmsResponseBaseDTO<WmsQualityResponseDTO> getQualityReport(@RequestHeader("WmsCode") String dcId,
                                                               @RequestBody WmsQualityRequestDTO req);
}
