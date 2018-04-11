package org.wuqqq.es.common;

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

    private long totalCount;

    private List<T> dataList;
}
