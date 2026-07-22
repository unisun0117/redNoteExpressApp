package cn.qdm.tob.modules.order.pay.api.internal;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.order.pay.api.internal.dto.CreateOrderRequest;
import cn.qdm.tob.modules.order.pay.dto.CreateOrderResponse;
import cn.qdm.tob.modules.order.pay.service.WxPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WxPayApi {

    private final WxPayService wxPayService;
    public ResponseResult<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();

        log.info("正在创建支付订单, 商户订单号: {}, 用户ID: {}", request.getOutTradeNo(), userId);
        CreateOrderResponse response = wxPayService.createJsapiOrder(request, userId);
        return ResponseResult.success(response);
    }
}
