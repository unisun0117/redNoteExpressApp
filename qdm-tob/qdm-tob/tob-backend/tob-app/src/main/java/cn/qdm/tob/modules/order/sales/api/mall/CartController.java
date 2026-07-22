package cn.qdm.tob.modules.order.sales.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.annotation.CurrentCustomer;
import cn.qdm.tob.infrastructure.security.annotation.CurrentUser;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.resolver.CustomerContext;
import cn.qdm.tob.modules.order.sales.service.CartService;
import cn.qdm.tob.modules.order.sales.vo.CartAddVO;
import cn.qdm.tob.modules.order.sales.vo.CartItemViewVO;
import cn.qdm.tob.modules.order.sales.vo.CartUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端 — 购物车 API
 */
@RestController
@RequestMapping("/api/mall/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "加入购物车")
    public ResponseResult<Void> add(@CurrentUser UserPrincipal user, @Valid @RequestBody CartAddVO vo) {
        cartService.add(user.getUserId(), vo);
        return ResponseResult.success();
    }

    @PutMapping("/update")
    @Operation(summary = "修改购物车（数量/勾选）")
    public ResponseResult<Void> update(@CurrentUser UserPrincipal user, @Valid @RequestBody CartUpdateVO vo) {
        cartService.update(user.getUserId(), vo);
        return ResponseResult.success();
    }

    @DeleteMapping("/remove")
    @Operation(summary = "删除购物车商品")
    public ResponseResult<Void> remove(@CurrentUser UserPrincipal user, @RequestParam Long id) {
        cartService.remove(user.getUserId(), id);
        return ResponseResult.success();
    }

    @GetMapping("/list")
    @Operation(summary = "购物车列表")
    public ResponseResult<List<CartItemViewVO>> list(@CurrentUser UserPrincipal user,
                                                      @CurrentCustomer CustomerContext customer) {
        return ResponseResult.success(
                cartService.list(user.getUserId(), customer.getPriceGroup()));
    }

    @DeleteMapping("/remove/batch")
    @Operation(summary = "批量删除购物车商品")
    public ResponseResult<Void> removeBatch(@CurrentUser UserPrincipal user, @RequestParam List<Long> ids) {
        cartService.removeBatch(user.getUserId(), ids);
        return ResponseResult.success();
    }
}
