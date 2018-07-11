package org.wuqqq.es.test;

import io.searchbox.client.JestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wuqqq.es.repo.EsRepository;

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
    protected String settingsJsonFileName() {
        return "GoodsIndex.json";
    }

    @Override
    protected JestClient getClient() {
        return jestClient;
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
