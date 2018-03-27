/**
 * Copyright (C), 2011-2018, 微贷网.
 */
package com.weidai.es.common;

import lombok.Getter;

/**
 * @author wuqi 2018/3/7 0007.
 */
public class EsRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 7462581444720168149L;

    @Getter
    private String errCode;

    @Getter
    private String errMsg;

    /**
     * @param esErrorEnum {@link EsErrorEnum}
     */
    public EsRuntimeException(EsErrorEnum esErrorEnum) {
        this(esErrorEnum == null ? null : esErrorEnum.getCode(), esErrorEnum == null ? null : esErrorEnum.getDesc());
    }

    public EsRuntimeException(String errCode, String errMsg) {
        super("[" + errCode + "]: " + errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    /**
     * @param esErrorEnum {@link EsErrorEnum}
     * @param e
     */
    public EsRuntimeException(EsErrorEnum esErrorEnum, Throwable e) {
        this(esErrorEnum == null ? null : esErrorEnum.getCode(), esErrorEnum == null ? null : esErrorEnum.getDesc(), e);
    }

    public EsRuntimeException(String errCode, String errMsg, Throwable e) {
        super("[" + errCode + "]: " + errMsg, e);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
}
