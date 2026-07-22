package cn.qdm.tob.modules.product.pricing.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 价格组实体
 * <p>
 * 联合主键：销售大区 + 价格组编码
 */
@Data
@TableName("prd_price_group")
public class PrdPriceGroup {

    @TableId
    private Long id;

    /** 销售大区编号 */
    private String salesRegionCode;

    /** 销售大区名称（冗余） */
    private String salesRegionName;

    /** 价格组编码 */
    private String priceGroupCode;

    /** 价格组名称 */
    private String priceGroupName;

    /** 描述说明 */
    private String description;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
