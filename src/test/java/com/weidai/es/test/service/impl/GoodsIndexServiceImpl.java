/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.test.service.impl;

import com.weidai.es.common.EsPageQuery;
import com.weidai.es.common.EsPageResult;
import com.weidai.es.test.domain.GoodsIndexBO;
import com.weidai.es.test.domain.GoodsPageQueryVO;
import com.weidai.es.test.repo.GoodsIndexRepository;
import com.weidai.es.test.service.GoodsIndexService;
import io.searchbox.core.search.sort.Sort;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.searchbox.core.search.sort.Sort.Sorting.DESC;
import static java.util.stream.Collectors.toList;

/**
 * @author wuqi 2018/2/1 0001.
 */
@Service
public class GoodsIndexServiceImpl implements GoodsIndexService {

    private static final Logger logger = LoggerFactory.getLogger(GoodsIndexServiceImpl.class);

    private final GoodsIndexRepository repository;

    @Autowired
    public GoodsIndexServiceImpl(GoodsIndexRepository repository) {
        this.repository = repository;
    }

    @Override
    public void batchUpdate(List<GoodsIndexBO> boList) {
        repository.bulkUpdateDocs(boList);
    }

    @Override
    public void batchIndex(List<GoodsIndexBO> boList) {
        repository.bulkIndexDocs(boList);
    }

    @Override
    public void batchIndex(String index, List<GoodsIndexBO> boList) {
        repository.bulkIndexDocs(index, boList);
    }

    @Override
    public boolean checkIndexExists(String index) {
        return repository.checkIndexExists(index);
    }

    @Override
    public void createIndex(String index) {
        repository.createIndex(index);
    }

    @Override
    public void modifyAliases(String index) {
        repository.modifyAliases(index);
    }

    @Override
    public EsPageResult<GoodsIndexBO> listGoodsPage(GoodsPageQueryVO goodsPageQueryVO) {
        EsPageQuery.EsPageQueryBuilder builder = EsPageQuery.builder();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (StringUtils.hasText(goodsPageQueryVO.getGoodsName())) {
            query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("goodsName.raw", goodsPageQueryVO.getGoodsName())));
        }
        if (goodsPageQueryVO.getGoodsMode() != null) {
            query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("goodsMode", goodsPageQueryVO.getGoodsMode())));
        }
        if (StringUtils.hasText(goodsPageQueryVO.getMerchantId())) {
            query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("merchantId", goodsPageQueryVO.getMerchantId())));
        }
        if (goodsPageQueryVO.getValid() != null) {
            query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("valid", goodsPageQueryVO.getValid())));
        }
        if (StringUtils.hasText(goodsPageQueryVO.getShelfCode())) {
            query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("shelfCode", goodsPageQueryVO.getShelfCode())));
            builder.sorts(new Sort("weight", DESC), new Sort("onShelfTime", DESC));
        } else {
            builder.collapse(new CollapseBuilder("goodsNo")).sorts(new Sort("createTime", DESC));
        }
        if (StringUtils.hasText(goodsPageQueryVO.getMarkName())) {
            BoolQueryBuilder markNameQb = QueryBuilders.boolQuery();
            markNameQb.should(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("labels.raw", goodsPageQueryVO.getMarkName())));
            markNameQb.should(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("cornerMark.raw", goodsPageQueryVO.getMarkName())));
            query.must(markNameQb);
        }
        long totalCount = repository.searchDistinctCount(query, "goodsNo");
        if (totalCount > 0) {
            EsPageResult<GoodsIndexBO> rs =
                    repository.search(builder.query(query).page(goodsPageQueryVO.getPage() - 1).size(goodsPageQueryVO.getSize()).build());
            rs.setTotalCount(totalCount);
            return rs;
        }
        return new EsPageResult<>(totalCount, null);
    }

    @Override
    public List<String> listGoodsName(String goodsNamePattern, String shelfCode) {
        EsPageQuery.EsPageQueryBuilder builder = EsPageQuery.builder();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.multiMatchQuery(goodsNamePattern, "goodsName", "goodsName.pinyin"));
        if (StringUtils.hasText(shelfCode)) {
            query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("shelfCode", shelfCode)));
        } else {
            builder.collapse(new CollapseBuilder("goodsNo"));
        }
        EsPageResult<GoodsIndexBO> rs = repository.search(builder.query(query).page(0).size(10).addIncludeFieldPatterns("goodsName").build());
        if (!CollectionUtils.isEmpty(rs.getDataList())) {
            return rs.getDataList().stream().map(GoodsIndexBO::getGoodsName).collect(toList());
        }
        return null;
    }

    @Override
    public void index(GoodsIndexBO bo) {
        repository.indexDoc(bo);
    }

    @Override
    public void update(GoodsIndexBO bo) {
        repository.updateDoc(bo);
    }

    @Override
    public List<GoodsIndexBO> listUserRecommendedGoods(String shelfCode) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("shelfCode", shelfCode)));
        query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("offShelf", false)));
        query.must(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("valid", true)));
        EsPageResult<GoodsIndexBO> rs =
                repository.search(EsPageQuery.builder().query(query).page(0).size(4).sorts(new Sort("weight", DESC), new Sort("onShelfTime", DESC)).build());
        return rs.getDataList();
    }
}
