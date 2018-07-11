package org.wuqqq.es.test;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author wuqi 2018/3/6 0006.
 */
@Data
public class GoodsPageQueryVO {

    @NotNull(message = "{page.notNull.msg}")
    @Min(value = 1, message = "{page.min.illegal.msg}")
    private Integer page;

    @NotNull(message = "{size.notNull.msg}")
    @Range(min = 0, max = 200, message = "{size.range.illegal.msg}")
    private Integer size;

    private String shelfCode;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品类型
     */
    private Integer goodsMode;

    /**
     * 商户id
     */
    private String merchantId;

    /**
     * 商品状态
     */
    private Boolean valid;

    /**
     * 标签名称
     */
    private String markName;
}
