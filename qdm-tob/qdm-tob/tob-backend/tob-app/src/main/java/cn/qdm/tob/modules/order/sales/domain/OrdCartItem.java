package cn.qdm.tob.modules.order.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车表实体
 */
@Data
@TableName("ord_cart_item")
public class OrdCartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 小程序用户ID */
    private Long userId;

    /** 商品条码 */
    private String barcode;

    /** 商品名称（冗余，列表展示用） */
    private String goodsName;

    /** 数量 */
    private BigDecimal quantity;

    /** 是否勾选（0=否 1=是） */
    private Integer selected;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
