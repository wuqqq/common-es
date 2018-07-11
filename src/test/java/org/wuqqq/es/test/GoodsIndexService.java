/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.test;

import java.util.List;

import com.weidai.es.common.EsPageResult;
import com.weidai.mario.goods.facade.vo.GoodsPageQueryVO;

/**
 * @author wuqi 2018/2/1 0001.
 */
public interface GoodsIndexService {

    void batchUpdate(List<GoodsIndexBO> boList);

    void batchIndex(List<GoodsIndexBO> boList);

    void batchIndex(String index, List<GoodsIndexBO> boList);

    boolean checkIndexExists(String index);

    void createIndex(String index);

    void modifyAliases(String index);

    EsPageResult<GoodsIndexBO> listGoodsPage(GoodsPageQueryVO goodsPageQueryVO);

    List<String> listGoodsName(String goodsNamePattern, String shelfCode);

    void index(GoodsIndexBO bo);

    void update(GoodsIndexBO bo);

    List<GoodsIndexBO> listUserRecommendedGoods(String shelfCode);
}
