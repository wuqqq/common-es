/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.test;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import lombok.Data;

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
     * 商品类型，{@link com.weidai.mario.goods.facade.constant.GoodsModeEnum}
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
