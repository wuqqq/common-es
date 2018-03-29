/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.test.repo;

import com.weidai.es.repo.EsRepository;
import com.weidai.es.test.domain.GoodsIndexBO;
import com.weidai.es.util.EsUtils;
import io.searchbox.client.JestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wuqi 2018/2/26 0026.
 */
@Component
public class GoodsIndexRepository extends EsRepository<GoodsIndexBO> {

    private final JestClient jestClient;

    @Autowired
    public GoodsIndexRepository(JestClient jestClient) {
        this.jestClient = jestClient;
    }

    @Override
    protected JestClient getClient() {
        return jestClient;
    }

    @Override
    protected String getIndexJsonString() {
        return EsUtils.loadJsonStringFromPath(INDEX_CONFIG_DIR + "GoodsIndex.json");
    }

    @Override
    protected String getId(GoodsIndexBO goodsIndexBO) {
        return goodsIndexBO.getCellId();
    }

    @Override
    protected Long getVersion(GoodsIndexBO goodsIndexBO) {
        return goodsIndexBO.getVersion();
    }

    @Override
    protected String getAlias() {
        return "mario-goods";
    }

    @Override
    protected String getType() {
        return "goods";
    }
}
