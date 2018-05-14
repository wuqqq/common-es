/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wuqi 2018/2/5 0005.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsPageResult<T> {

    private long totalCount = 0L;

    private List<T> dataList;
}
