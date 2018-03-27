/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.common;

import lombok.Getter;

/**
 * @author wuqi 2018/3/7 0007.
 */
public enum EsErrorEnum {
    IO_EXCEPTION("7000001", "IO异常"),
    CREATE_INDEX_EXCEPTION("7000002", "创建es索引异常"),
    CREATE_INDEX_MAPPING_EXCEPTION("7000003", "创建索引映射异常"),
    INDEX_DOCUMENT_EXCEPTION("7000004", "索引文档异常"),
    UPDATE_DOCUMENT_EXCEPTION("7000005", "更新文档异常"),
    DELETE_DOCUMENT_EXCEPTION("7000006", "删除文档异常"),
    BULK_INDEX_DOCS_EXCEPTION("7000007", "批量索引文档异常"),
    BULK_UPDATE_DOCS_EXCEPTION("7000008", "批量更新文档异常"),
    SEARCH_INDEX_EXCEPTION("7000009", "查询索引异常"),
    MODIFY_ALIASES_EXCEPTION("7000010", "修改索引别名异常"),
    DELETE_INDEX_EXCEPTION("7000011", "删除索引异常");

    @Getter
    private final String code;
    @Getter
    private final String desc;

    EsErrorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EsErrorEnum getEnumByCode(String code) {
        for (EsErrorEnum e : EsErrorEnum.values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}
