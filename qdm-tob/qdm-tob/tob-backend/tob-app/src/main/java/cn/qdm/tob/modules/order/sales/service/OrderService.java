package cn.qdm.tob.modules.order.sales.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.security.resolver.CustomerContext;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.SalesRegionApi;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionDetailDTO;
import cn.qdm.tob.modules.order.sales.domain.OrdCartItem;
import cn.qdm.tob.modules.order.sales.domain.OrdOrderItem;
import cn.qdm.tob.modules.order.sales.domain.OrdSalesOrder;
import cn.qdm.tob.modules.order.sales.enums.*;
import cn.qdm.tob.modules.order.sales.mapper.OrdOrderItemMapper;
import cn.qdm.tob.modules.order.sales.mapper.OrdSalesOrderMapper;
import cn.qdm.tob.modules.order.sales.vo.*;
import cn.qdm.tob.modules.product.catalog.api.internal.ProductCatalogApi;
import cn.qdm.tob.modules.product.catalog.api.internal.dto.ProductCatalogDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单服务 — 预览 + 提交
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrdSalesOrderMapper salesOrderMapper;
    private final OrdOrderItemMapper orderItemMapper;
    private final SalesRegionApi salesRegionApi;
    private final ProductCatalogApi productCatalogApi;

    /**
     * 订单预览 — 地址已由 @CurrentCustomer 校验，直接使用 customer 上下文
     */
    public OrderPreviewViewVO preview(Long userId, CustomerContext customer, OrderPreviewVO vo) {
        // 1. 获取运费规则
        SalesRegionDetailDTO region = salesRegionApi.getDetailById(customer.getSalesRegionId());
        AssertUtils.notNull(region, "销售大区不存在");

        // 2. 选中的购物车商品
        List<OrdCartItem> selectedItems = cartService.getSelectedItems(userId);
        AssertUtils.isTrue(!selectedItems.isEmpty(), "请选择商品");

        // 3. 批量查商品+价格（INNER JOIN 价格表，一次查询）
        List<String> barcodes = selectedItems.stream()
                .map(OrdCartItem::getBarcode).distinct().collect(Collectors.toList());
        Map<String, ProductCatalogDTO> catalogMap = productCatalogApi
                .listByBarcodesWithPrice(customer.getPriceGroup(), barcodes).stream()
                .collect(Collectors.toMap(ProductCatalogDTO::getProductBarcode, Function.identity()));

        // 4. 校验 + 算金额
        BigDecimal goodsAmount = BigDecimal.ZERO;
        List<OrderPreviewItemVO> items = new ArrayList<>();
        for (OrdCartItem ci : selectedItems) {
            ProductCatalogDTO catalog = catalogMap.get(ci.getBarcode());
            AssertUtils.notNull(catalog, "商品 [" + ci.getBarcode() + "] 不存在或未定价");
            AssertUtils.isTrue("LISTED".equals(catalog.getStatus()),
                    "商品 [" + catalog.getProductName() + "] 已下架");
            AssertUtils.isTrue(catalog.getDailyAvailable().compareTo(ci.getQuantity()) >= 0,
                    "商品 [" + catalog.getProductName() + "] 库存不足，当前库存 " + catalog.getDailyAvailable());

            BigDecimal unitPrice = catalog.getPrice() != null ? catalog.getPrice() : BigDecimal.ZERO;

            OrderPreviewItemVO itemVO = new OrderPreviewItemVO();
            itemVO.setBarcode(ci.getBarcode());
            itemVO.setGoodsName(catalog.getProductName());
            itemVO.setQuantity(ci.getQuantity());
            itemVO.setUnitPrice(unitPrice);
            itemVO.setLineTotal(unitPrice.multiply(ci.getQuantity()));
            items.add(itemVO);
            goodsAmount = goodsAmount.add(itemVO.getLineTotal());
        }

        // 5. 运费
        BigDecimal freightAmount = BigDecimal.ZERO;
        if (region.getStdFreeAmount() == null
                || goodsAmount.compareTo(region.getStdFreeAmount()) < 0) {
            freightAmount = region.getStdFreight() != null ? region.getStdFreight() : BigDecimal.ZERO;
        }

        OrderPreviewViewVO result = new OrderPreviewViewVO();
        result.setItems(items);
        result.setGoodsAmount(goodsAmount);
        result.setPromotionAmount(BigDecimal.ZERO);
        result.setCouponAmount(BigDecimal.ZERO);
        result.setFreightAmount(freightAmount);
        result.setPaidAmount(goodsAmount.add(freightAmount));
        return result;
    }

    @Transactional
    public OrderSubmitViewVO submit(Long userId, CustomerContext customer, OrderSubmitVO vo) {
        // 1. 幂等
        if (vo.getIdempotentKey() != null && !vo.getIdempotentKey().isBlank()) {
            var existing = salesOrderMapper
                    .lambdaSelectOne(w -> w
                            .eq(OrdSalesOrder::getSubmitUserId, userId)
                            .eq(OrdSalesOrder::getCreatedBy, vo.getIdempotentKey()))
                    .orElse(null);

            if (Objects.nonNull(existing)) {
                OrderSubmitViewVO result = new OrderSubmitViewVO();
                result.setOrderNo(existing.getOrderNo());
                result.setPaidAmount(existing.getPaidAmount());
                return result;
            }
        }

        // 2. 复用预览校验
        OrderPreviewVO pvo = new OrderPreviewVO();
        pvo.setAddressId(customer.getId());
        pvo.setPayMethod(vo.getPayMethod());
        OrderPreviewViewVO preview = preview(userId, customer, pvo);

        // 3. 订购约束校验（一次查询获取商品+价格）
        List<OrdCartItem> selectedItems = cartService.getSelectedItems(userId);
        List<String> barcodes = selectedItems.stream()
                .map(OrdCartItem::getBarcode).distinct().collect(Collectors.toList());
        Map<String, ProductCatalogDTO> catalogMap = productCatalogApi
                .listByBarcodesWithPrice(customer.getPriceGroup(), barcodes).stream()
                .collect(Collectors.toMap(ProductCatalogDTO::getProductBarcode, Function.identity()));

        for (OrdCartItem ci : selectedItems) {
            ProductCatalogDTO catalog = catalogMap.get(ci.getBarcode());
            AssertUtils.notNull(catalog, "商品 [" + ci.getBarcode() + "] 不存在或未定价");
            BigDecimal qty = ci.getQuantity();
            AssertUtils.ge(qty, catalog.getOrderMinQty(), "商品 [" + catalog.getProductName() + "] 起订量 " + catalog.getOrderMinQty());
            AssertUtils.le(qty, catalog.getOrderMaxQty(), "商品 [" + catalog.getProductName() + "] 限购量 " + catalog.getOrderMaxQty());

            BigDecimal remainder = qty.remainder(catalog.getOrderBaseQty());
            AssertUtils.isTrue(remainder.compareTo(BigDecimal.ZERO) == 0,
                    "商品 [" + catalog.getProductName() + "] 订购量必须是 " + catalog.getOrderBaseQty() + " 的整数倍");
        }

        // 4. 生成订单
        String orderNo = orderNumberGenerator.nextOrderNo();
        LocalDateTime now = LocalDateTime.now();

        OrdSalesOrder order = new OrdSalesOrder();
        order.setOrderNo(orderNo);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        order.setCustomerId(customer.getId());
        order.setCustomerCode(customer.getSapCustomerCode()); // SAP 编码待审核后回写
        order.setCustomerName(customer.getCustomerName());
        order.setSalesRegionId(customer.getSalesRegionId());
        order.setSalesmanId(customer.getSalesmanId());
        order.setSubmitUserId(userId);
        order.setSubmitUserName(customer.getContactName());
        order.setSubmitUserPhone(customer.getContactPhone());
        order.setReceiverName(customer.getContactName());
        order.setReceiverPhone(customer.getContactPhone());
        order.setReceiverAddress(customer.getAddress());
        order.setDeliveryType(DeliveryType.LOGISTICS);
        order.setOrderTime(now);
        order.setArrivalDate(vo.getArrivalDate());
        order.setDeliveryRemark(vo.getDeliveryRemark());
        order.setTotalAmount(preview.getPaidAmount());
        order.setGoodsAmount(preview.getGoodsAmount());
        order.setPromotionAmount(preview.getPromotionAmount());
        order.setCouponAmount(preview.getCouponAmount());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setGoodsPaidAmount(preview.getGoodsAmount());
        order.setFreightAmount(preview.getFreightAmount());
        order.setPaidAmount(preview.getPaidAmount());
        order.setSettleCustomerCode(null);
        order.setPayMethod(vo.getPayMethod());
        order.setSapPushStatus(SapPushStatus.PENDING);
        order.setReturnStatus(ReturnStatus.NONE);
        order.setCreatedBy(vo.getIdempotentKey());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        salesOrderMapper.insert(order);

        // 5. 插入明细
        for (OrdCartItem ci : selectedItems) {
            ProductCatalogDTO dto = catalogMap.get(ci.getBarcode());
            BigDecimal unitPrice = dto != null && dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO;

            OrdOrderItem item = new OrdOrderItem();
            item.setOrderId(order.getId());
            item.setBarcode(ci.getBarcode());
            item.setGoodsName(dto.getProductName());
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setGoodsTotal(unitPrice.multiply(ci.getQuantity()));
            item.setPromotionShare(BigDecimal.ZERO);
            item.setCouponShare(BigDecimal.ZERO);
            item.setDiscountShare(BigDecimal.ZERO);
            item.setGoodsPaid(unitPrice.multiply(ci.getQuantity()));
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            orderItemMapper.insert(item);
        }

        // 6. 清空购物车
        cartService.clearSelected(userId);

        OrderSubmitViewVO result = new OrderSubmitViewVO();
        result.setOrderNo(orderNo);
        result.setPaidAmount(preview.getPaidAmount());
        return result;
    }
}
