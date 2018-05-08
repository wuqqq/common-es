/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.common;

import io.searchbox.core.search.sort.Sort;
import lombok.Getter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author wuqi 2018/2/5 0005.
 */
public class EsPageQuery {

    /**
     * 从0开始
     */
    @Getter
    private int page;

    @Getter
    private int size;

    @Getter
    private List<String> includeFieldPatternList;

    @Getter
    private List<Sort> sorts;

    private String json;

    public static EsPageQueryBuilder builder() {
        return new EsPageQueryBuilder();
    }

    public static class EsPageQueryBuilder {

        private EsPageQuery query = new EsPageQuery();

        private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        public EsPageQueryBuilder page(int page) {
            if (page < 0)
                page = 0;
            query.page = page;
            return this;
        }

        public EsPageQueryBuilder size(int size) {
            if (size < 1)
                size = 1;
            query.size = size;
            return this;
        }

        public EsPageQueryBuilder query(QueryBuilder query) {
            searchSourceBuilder.query(query);
            return this;
        }

        public EsPageQueryBuilder aggregation(AggregationBuilder aggregation) {
            searchSourceBuilder.aggregation(aggregation);
            return this;
        }

        public EsPageQueryBuilder collapse(CollapseBuilder collapse) {
            searchSourceBuilder.collapse(collapse);
            return this;
        }

        public EsPageQueryBuilder json(String json) {
            query.json = json;
            return this;
        }

        public EsPageQueryBuilder addIncludeFieldPatterns(String... includeFieldPatterns) {
            if (query.includeFieldPatternList == null)
                query.includeFieldPatternList = new ArrayList<>();
            query.includeFieldPatternList.addAll(Arrays.asList(includeFieldPatterns));
            return this;
        }

        public EsPageQueryBuilder sorts(Sort... sorts) {
            if (query.sorts == null)
                query.sorts = new ArrayList<>();
            query.sorts.addAll(Arrays.asList(sorts));
            return this;
        }

        public EsPageQuery build() {
            query.json = searchSourceBuilder.toString();
            return query;
        }
    }

    public String toJsonString() {
        return json;
    }

    public int getFrom() {
        return page * size;
    }
}
