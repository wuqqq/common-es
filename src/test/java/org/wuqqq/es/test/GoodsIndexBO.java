package org.wuqqq.es.test;

import io.searchbox.annotations.JestId;
import io.searchbox.annotations.JestVersion;
import lombok.Data;

import java.util.List;

/**
 * @author wuqi 2018/2/5 0005.
 */
@Data
public class GoodsIndexBO {

    @JestId
    private String cellId;

    private String goodsNo;

    private String shelfCode;

    private Boolean offShelf;

    private String onShelfTime;

    private Integer weight;

    private String merchantId;

    private String goodsName;

    private Integer goodsMode;

    private String iconUrl;

    private String remark;

    private String redirectUrl;

    private String minLoanAmount;

    private String maxLoanAmount;

    private String minLoanRate;

    private String maxLoanRate;

    private Integer minPeriod;

    private Integer maxPeriod;

    private String periodTimeUnit;

    private Boolean valid;

    private String createTime;

    private List<String> labels;

    private String cornerMark;

    private String cornerMarkIconUrl;

    @JestVersion
    private Long version;
}
