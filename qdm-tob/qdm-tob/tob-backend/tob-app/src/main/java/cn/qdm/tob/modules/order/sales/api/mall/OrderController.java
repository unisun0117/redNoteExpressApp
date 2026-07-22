package cn.qdm.tob.modules.order.sales.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.annotation.CurrentCustomer;
import cn.qdm.tob.infrastructure.security.annotation.CurrentUser;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.resolver.CustomerContext;
import cn.qdm.tob.modules.order.pay.api.internal.WxPayApi;
import cn.qdm.tob.modules.order.pay.api.internal.dto.CreateOrderRequest;
import cn.qdm.tob.modules.order.pay.dto.CreateOrderResponse;
import cn.qdm.tob.modules.order.sales.enums.PayMethod;
import cn.qdm.tob.modules.order.sales.service.OrderService;
import cn.qdm.tob.modules.order.sales.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端 — 下单 API
 */
@Slf4j
@RestController
@RequestMapping("/api/mall/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final WxPayApi wxPayApi;

    @PostMapping("/preview")
    @Operation(summary = "结算预览")
    public ResponseResult<OrderPreviewViewVO> preview(
            @CurrentUser UserPrincipal user,
            @CurrentCustomer CustomerContext customer,
            @Valid @RequestBody OrderPreviewVO vo) {
        return ResponseResult.success(orderService.preview(user.getUserId(), customer, vo));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交订单")
    public ResponseResult<OrderSubmitViewVO> submit(
            @CurrentUser UserPrincipal user,
            @CurrentCustomer CustomerContext customer,
            @Valid @RequestBody OrderSubmitVO vo) {
        // 1. 提交订单（事务内）
        OrderSubmitViewVO result = orderService.submit(user.getUserId(), customer, vo);

        // 2. 仅微信支付触发（事务外，失败不回滚订单）
        if (vo.getPayMethod() == PayMethod.WECHAT) {
            try {
                CreateOrderRequest payReq = new CreateOrderRequest();
                payReq.setOutTradeNo(result.getOrderNo().trim());
                payReq.setDescription("订单支付");
                payReq.setAmountYuan(result.getPaidAmount());
                payReq.setCustomerCode(customer.getSapCustomerCode());

                ResponseResult<CreateOrderResponse> payResult = wxPayApi.createOrder(payReq);
                result.setPayParams(payResult.getData());
            } catch (Exception e) {
                log.warn("微信支付下单失败，订单已创建, orderNo={}, error={}",
                        result.getOrderNo(), e.getMessage());
                // 静默降级，不影响订单返回
            }
        }

        return ResponseResult.success(result);
    }
}
