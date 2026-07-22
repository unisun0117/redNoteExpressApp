package cn.qdm.tob.client.sap;

import cn.qdm.tob.client.sap.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "sap-client", url = "${tob.client.sap.base-url}", configuration = SapClientConfiguration.class)
public interface SapClient {
    @PostMapping("TOB/CreateCustomer")
    ResponseEntity<SapCustomerResponseDTO> pushCustomer(@RequestBody SapCustomerRequestDTO req);

    @PostMapping("BMP/B2BOrderPlan")
    ResponseEntity<SapResponseDTO<?>> pushOrder(@RequestBody SapOrderRequestDTO req);

    @PostMapping("BMP/GetVendor")
    ResponseEntity<SapResponseDTO<List<SapVendorDTO>>> queryVendor(@RequestBody SapVendorQueryRequestDTO req);
}
