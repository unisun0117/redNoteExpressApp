package cn.qdm.tob.modules.order.sales.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.order.sales.domain.OrdCartItem;
import cn.qdm.tob.modules.order.sales.mapper.OrdCartItemMapper;
import cn.qdm.tob.modules.order.sales.vo.CartAddVO;
import cn.qdm.tob.modules.order.sales.vo.CartItemViewVO;
import cn.qdm.tob.modules.order.sales.vo.CartUpdateVO;
import cn.qdm.tob.modules.product.catalog.api.internal.ProductCatalogApi;
import cn.qdm.tob.modules.product.catalog.api.internal.dto.ProductCatalogDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService extends TobBaseService<OrdCartItemMapper, OrdCartItem> {

    private final OrdCartItemMapper cartItemMapper;
    private final ProductCatalogApi productCatalogApi;

    @Transactional
    public void add(Long userId, CartAddVO vo) {
        var existing = cartItemMapper.lambdaSelectOne(w -> w
                .eq(OrdCartItem::getUserId, userId)
                .eq(OrdCartItem::getBarcode, vo.getBarcode())
        );
        existing.ifPresentOrElse(e -> {
            e.setQuantity(e.getQuantity().add(vo.getQuantity()));
            cartItemMapper.updateById(e);
        }, () -> {
            OrdCartItem item = new OrdCartItem();
            item.setUserId(userId);
            item.setBarcode(vo.getBarcode());
            item.setQuantity(vo.getQuantity());
            item.setSelected(1);
            cartItemMapper.insert(item);
        });
    }

    @Transactional
    public void update(Long userId, CartUpdateVO vo) {
        OrdCartItem item = cartItemMapper.selectById(vo.getId());
        AssertUtils.notNull(item, "购物车记录不存在");
        AssertUtils.equals(item.getUserId(), userId, "无权操作");
        if (vo.getQuantity() != null) {
            AssertUtils.gt(vo.getQuantity(), BigDecimal.ZERO, "数量必须大于等于0");
            item.setQuantity(vo.getQuantity());
        }
        if (vo.getSelected() != null) {
            item.setSelected(vo.getSelected());
        }
        cartItemMapper.updateById(item);
    }

    public void remove(Long userId, Long cartItemId) {
        cartItemMapper.lambdaDelete(w -> w
                .eq(OrdCartItem::getId, cartItemId)
                .eq(OrdCartItem::getUserId, userId)
        );
    }

    @Transactional
    public void removeBatch(Long userId, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return;
        cartItemMapper.lambdaDelete(w -> w
                .eq(OrdCartItem::getUserId, userId)
                .in(OrdCartItem::getId, ids)
        );
    }

    public List<CartItemViewVO> list(Long userId, String priceGroupCode) {
        List<OrdCartItem> cartItems = cartItemMapper.lambdaSelect(w -> w.eq(OrdCartItem::getUserId, userId));
        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> barcodes = cartItems.stream()
                .map(OrdCartItem::getBarcode).distinct().collect(Collectors.toList());

        Map<String, ProductCatalogDTO> catalogMap = productCatalogApi
                .listByBarcodesWithPrice(priceGroupCode, barcodes).stream()
                .collect(Collectors.toMap(ProductCatalogDTO::getProductBarcode, Function.identity()));

        return cartItems.stream().map(ci -> {
            CartItemViewVO vo = new CartItemViewVO();
            vo.setId(ci.getId());
            vo.setBarcode(ci.getBarcode());
            vo.setQuantity(ci.getQuantity());
            vo.setSelected(ci.getSelected());

            ProductCatalogDTO catalog = catalogMap.get(ci.getBarcode());
            if (catalog != null) {
                vo.setGoodsName(catalog.getProductName());
                vo.setProductImage(catalog.getMainImage());
                vo.setProductStatus(catalog.getStatus());
                if (catalog.getPrice() != null && catalog.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    vo.setUnitPrice(catalog.getPrice());
                }
                // 商品存在且价格有效
                vo.setValid(catalog.getPrice() != null && catalog.getPrice().compareTo(BigDecimal.ZERO) > 0);
            } else {
                // 商品已不存在
                vo.setValid(false);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    public List<OrdCartItem> getSelectedItems(Long userId) {
        return cartItemMapper.lambdaSelect(w -> w
                .eq(OrdCartItem::getUserId, userId)
                .eq(OrdCartItem::getSelected, 1)
        );
    }

    @Transactional
    public void clearSelected(Long userId) {
        cartItemMapper.lambdaDelete(w -> w
                .eq(OrdCartItem::getUserId, userId)
                .eq(OrdCartItem::getSelected, 1));
    }

}
