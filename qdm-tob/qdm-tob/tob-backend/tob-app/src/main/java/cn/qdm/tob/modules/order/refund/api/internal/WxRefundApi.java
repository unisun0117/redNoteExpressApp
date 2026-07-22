package cn.qdm.tob.modules.order.refund.api.internal;

import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.order.refund.api.internal.dto.RefundRequest;
import cn.qdm.tob.modules.order.refund.service.WxRefundService;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.service.refund.model.Refund;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Component
@RequiredArgsConstructor
public class WxRefundApi {


    private final WxRefundService wxRefundService;

    public ResponseResult<Refund> applyRefund(
            @Valid @RequestBody RefundRequest request) {

        log.info("收到退款申请, outTradeNo: {}, outRefundNo: {}", request.getOutTradeNo(), request.getOutRefundNo());

        try {
            Refund response = wxRefundService.applyRefund(
                    request.getOutTradeNo(),
                    request.getOutRefundNo(),
                    request.getRefundAmountYuan(),
                    request.getReason()
            );
            return ResponseResult.success(response);
        } catch (ServiceException e) {
            log.error("退款申请失败, outRefundNo: {}, httpStatus: {}, body: {}",
                    request.getOutRefundNo(), e.getHttpStatusCode(), e.getResponseBody());
            throw new TobServiceException(400,"退款申请失败: " + e.getResponseBody());
        } catch (ValidationException e) {
            log.error("退款申请签名验证失败, outRefundNo: {}", request.getOutRefundNo(), e);
            throw new TobServiceException(400,"签名验证失败");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("退款申请前置校验失败, outRefundNo: {}, 原因: {}",
                    request.getOutRefundNo(), e.getMessage());
            throw new TobServiceException(400,e.getMessage());
        }
    }
}
