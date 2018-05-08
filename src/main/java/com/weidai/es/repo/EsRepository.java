/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.repo;

import com.weidai.es.common.EsPageQuery;
import com.weidai.es.common.EsPageResult;
import com.weidai.es.common.EsRuntimeException;
import com.weidai.es.util.EsUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.aliases.AddAliasMapping;
import io.searchbox.indices.aliases.GetAliases;
import io.searchbox.indices.aliases.ModifyAliases;
import io.searchbox.indices.aliases.RemoveAliasMapping;
import io.searchbox.params.Parameters;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.weidai.es.common.EsErrorEnum.*;

/**
 * @author wuqi 2018/2/5 0005.
 */
public abstract class EsRepository<T> {

    private static final Logger logger = LoggerFactory.getLogger(EsRepository.class);

    public static final String INDEX_CONFIG_DIR = "es/index/config/";

    private volatile Class<T> clazz;

    public boolean checkIndexExists(String index) {
        try {
            JestResult rs = getClient().execute(new IndicesExists.Builder(index).build());
            return rs.isSucceeded();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    public void createIndex(String index) {
        try {
            JestResult rs = getClient().execute(new CreateIndex.Builder(index).settings(getIndexJsonString()).build());
            if (rs.isSucceeded()) {
                logger.info("create index {} successfully!", index);
            } else {
                logger.error("create index {} failed, response code: {}, error message: {}", getAlias(), rs.getResponseCode(), rs.getErrorMessage());
                throw new EsRuntimeException(CREATE_INDEX_EXCEPTION);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    public void deleteIndex(String index) {
        try {
            JestResult rs = getClient().execute(new DeleteIndex.Builder(index).build());
            if (rs.isSucceeded()) {
                logger.info("delete index {} successfully!", index);
            } else {
                logger.error("delete index {} failed, response code: {}, error message: {}", index, rs.getResponseCode(), rs.getErrorMessage());
                throw new EsRuntimeException(DELETE_INDEX_EXCEPTION);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void modifyAliases(String index) {
        try {
            JestResult getAliasesRs = getClient().execute(new GetAliases.Builder().addIndex(getAlias()).build());
            ModifyAliases modifyAliases;
            String oldIndex = null;
            if (getAliasesRs.isSucceeded()) {
                oldIndex = getAliasesRs.getJsonObject().keySet().stream().findFirst().get();
                // 先删除，再添加，原子操作
                modifyAliases = new ModifyAliases.Builder(new RemoveAliasMapping.Builder(oldIndex, getAlias()).build())
                        .addAlias(new AddAliasMapping.Builder(index, getAlias()).build()).build();
            } else {
                modifyAliases = new ModifyAliases.Builder(new AddAliasMapping.Builder(index, getAlias()).build()).build();
            }
            JestResult modAliasesRs = getClient().execute(modifyAliases);
            if (modAliasesRs.isSucceeded()) {
                if (StringUtils.hasText(oldIndex))
                    deleteIndex(oldIndex);
            } else {
                logger.error("modify aliases failed, response code: {}, error message: {}", modAliasesRs.getResponseCode(), modAliasesRs.getErrorMessage());
                throw new EsRuntimeException(MODIFY_ALIASES_EXCEPTION);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    protected String getIndexJsonString() {
        return EsUtils.loadJsonStringFromPath(INDEX_CONFIG_DIR + "default.json");
    }

    public void indexDoc(T t) {
        try {
            DocumentResult rs = getClient().execute(new Index.Builder(t).index(getAlias()).type(getType()).refresh(true).build());
            if (!rs.isSucceeded()) {
                logger.error("index doc failed: [index: {}, type: {}, id: {}, error: {}]", getAlias(), getType(), getId(t), rs.getErrorMessage());
                throw new EsRuntimeException(INDEX_DOCUMENT_EXCEPTION);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    public void updateDoc(T t) {
        String doc = EsUtils.buildDoc(t);
        if (StringUtils.hasText(doc)) {
            try {
                DocumentResult rs =
                        getClient().execute(new Update.VersionBuilder(doc, getVersion(t)).index(getAlias()).type(getType()).id(getId(t)).refresh(true).build());
                if (!rs.isSucceeded()) {
                    logger.error("update doc failed: [index: {}, type: {}, id: {}, version: {}, error: {}", getAlias(), getType(), getId(t), getVersion(t),
                            rs.getErrorMessage());
                    throw new EsRuntimeException(UPDATE_DOCUMENT_EXCEPTION);
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new EsRuntimeException(IO_EXCEPTION, e);
            }
        }
    }

    public void bulkIndexDocs(List<T> tList) {
        bulkIndexDocs(getAlias(), tList);
    }

    public void bulkIndexDocs(String index, List<T> tList) {
        if (!CollectionUtils.isEmpty(tList)) {
            List<Index> actions = buildBulkIndexActions(tList);
            if (!CollectionUtils.isEmpty(actions)) {
                try {
                    BulkResult rs = getClient().execute(new Bulk.Builder().defaultIndex(index).defaultType(getType()).addAction(actions).refresh(true).build());
                    if (!rs.isSucceeded()) {
                        if (rs.getFailedItems().isEmpty()) {
                            logger.error("bulk index docs failed, error message: {}", rs.getErrorMessage());
                            throw new EsRuntimeException(BULK_INDEX_DOCS_EXCEPTION);
                        } else {
                            StringBuilder buffer = new StringBuilder();
                            buffer.append("bulk index docs failed: ");
                            rs.getFailedItems().forEach(item -> buffer.append("[index: ").append(item.index).append(",type: ").append(item.type).append(",id: ")
                                    .append(item.id).append(",error: ").append(item.error).append("],"));
                            String errorInfo = buffer.toString();
                            logger.error(errorInfo.substring(0, errorInfo.length() - 1));
                            throw new EsRuntimeException(BULK_INDEX_DOCS_EXCEPTION);
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    throw new EsRuntimeException(IO_EXCEPTION, e);
                }
            }
        }
    }

    private List<Index> buildBulkIndexActions(List<T> tList) {
        List<Index> rs = new ArrayList<>(tList.size());
        for (T t : tList) {
            rs.add(new Index.Builder(t).build());
        }
        return rs;
    }

    public void bulkUpdateDocs(List<T> tList) {
        if (!CollectionUtils.isEmpty(tList)) {
            List<Update> actions = buildBulkUpdateActions(tList);
            if (!CollectionUtils.isEmpty(actions)) {
                try {
                    BulkResult rs =
                            getClient().execute(new Bulk.Builder().defaultIndex(getAlias()).defaultType(getType()).addAction(actions).refresh(true).build());
                    if (!rs.isSucceeded()) {
                        if (rs.getFailedItems().isEmpty()) {
                            logger.error("bulk update docs failed, error message: {}", rs.getErrorMessage());
                            throw new EsRuntimeException(BULK_UPDATE_DOCS_EXCEPTION);
                        } else {
                            StringBuilder buffer = new StringBuilder();
                            buffer.append("bulk update docs failed: ");
                            rs.getFailedItems().forEach(item -> buffer.append("[index: ").append(item.index).append(",type: ").append(item.type).append(",id: ")
                                    .append(item.id).append(",error: ").append(item.error).append("],"));
                            String errorInfo = buffer.toString();
                            logger.error(errorInfo.substring(0, errorInfo.length() - 1));
                            throw new EsRuntimeException(BULK_UPDATE_DOCS_EXCEPTION);
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    throw new EsRuntimeException(IO_EXCEPTION, e);
                }
            }
        }
    }

    private List<Update> buildBulkUpdateActions(List<T> tList) {
        List<Update> rs = new ArrayList<>(tList.size());
        for (T t : tList) {
            String doc = EsUtils.buildDoc(t);
            if (StringUtils.hasText(doc)) {
                rs.add(new Update.VersionBuilder(doc, getVersion(t)).id(getId(t)).build());
            }
        }
        return rs;
    }

    public void deleteDoc(String id) {
        try {
            DocumentResult rs = getClient().execute(new Delete.Builder(id).index(getAlias()).type(getType()).refresh(true).build());
            if (!rs.isSucceeded()) {
                logger.error("delete doc failed: [index: {}, type: {}, id: {}, error: {}]", getAlias(), getType(), id, rs.getErrorMessage());
                throw new EsRuntimeException(DELETE_DOCUMENT_EXCEPTION);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    public void bulkDeleteDocs(List<String> idList){
        if (!CollectionUtils.isEmpty(idList)) {
            List<Delete> actions = idList.stream().map(id-> new Delete.Builder(id).build()).collect(Collectors.toList());
            try {
                BulkResult rs = getClient().execute(new Bulk.Builder().defaultIndex(getAlias()).defaultType(getType()).addAction(actions).refresh(true).build());
                if (!rs.isSucceeded()) {
                    if (rs.getFailedItems().isEmpty()) {
                        logger.error("bulk delete docs failed, error message: {}", rs.getErrorMessage());
                        throw new EsRuntimeException(BULK_DELETE_DOCS_EXCEPTION);
                    } else {
                        StringBuilder buffer = new StringBuilder();
                        buffer.append("bulk delete docs failed: ");
                        rs.getFailedItems().forEach(item -> buffer.append("[index: ").append(item.index).append(",type: ").append(item.type).append(",id: ")
                                .append(item.id).append(",error: ").append(item.error).append("],"));
                        String errorInfo = buffer.toString();
                        logger.error(errorInfo.substring(0, errorInfo.length() - 1));
                        throw new EsRuntimeException(BULK_DELETE_DOCS_EXCEPTION);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new EsRuntimeException(IO_EXCEPTION, e);
            }
        }
    }

    public EsPageResult<T> search(EsPageQuery esPageQuery) {
        Search.Builder searchBuilder =
                new Search.Builder(esPageQuery.toJsonString()).addIndex(getAlias()).addType(getType()).setParameter(Parameters.FROM, esPageQuery.getFrom())
                        .setParameter(Parameters.SIZE, esPageQuery.getSize()).setParameter(Parameters.VERSION, true);
        if (!CollectionUtils.isEmpty(esPageQuery.getIncludeFieldPatternList())) {
            esPageQuery.getIncludeFieldPatternList().forEach(searchBuilder::addSourceIncludePattern);
        }
        if (!CollectionUtils.isEmpty(esPageQuery.getSorts())) {
            searchBuilder.addSort(esPageQuery.getSorts());
        }
        try {
            SearchResult rs = getClient().execute(searchBuilder.build());
            if (!rs.isSucceeded()) {
                logger.error("search failed: [index: {}, type: {}, error: {}]", getAlias(), getType(), rs.getErrorMessage());
                throw new EsRuntimeException(SEARCH_INDEX_EXCEPTION);
            }
            long hitCount = rs.getJsonObject().get("hits").getAsJsonObject().get("total").getAsLong();
            EsPageResult<T> result = new EsPageResult<>();
            result.setDataList(rs.getSourceAsObjectList(getParameterizedClass(), true));
            result.setTotalCount(hitCount);
            return result;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    public T getDocById(String id){
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException("document id mustn't be null or empty string");
        try {
            DocumentResult rs = getClient().execute(new Get.Builder(getAlias(), id).type(getType()).build());
            if (!rs.isSucceeded()) {
                logger.error("get doc failed: [index: {}, type: {}, error: {}]", getAlias(), getType(), rs.getErrorMessage());
                throw new EsRuntimeException(SEARCH_DOCUMENT_EXCEPTION);
            }
            return rs.getSourceAsObject(getParameterizedClass());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getParameterizedClass() {
        if (clazz == null)
            clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return clazz;
    }

    public long searchDistinctCount(QueryBuilder query, String distinctField) {
        String json = EsPageQuery.builder().query(query).aggregation(AggregationBuilders.cardinality(distinctField + "_count").field(distinctField)).build()
                .toJsonString();
        Search.Builder searchBuilder = new Search.Builder(json).addIndex(getAlias()).addType(getType()).setParameter(Parameters.SIZE, 0);
        try {
            SearchResult rs = getClient().execute(searchBuilder.build());
            if (!rs.isSucceeded()) {
                logger.error("search failed: [index: {}, type: {}, error: {}]", getAlias(), getType(), rs.getErrorMessage());
                throw new EsRuntimeException(SEARCH_INDEX_EXCEPTION);
            }
            return rs.getAggregations().getCardinalityAggregation(distinctField + "_count").getCardinality();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new EsRuntimeException(IO_EXCEPTION, e);
        }
    }

    protected abstract JestClient getClient();

    protected abstract String getId(T t);

    protected abstract Long getVersion(T t);

    protected abstract String getAlias();

    protected abstract String getType();
}
