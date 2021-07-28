package org.hswebframework.web.api.crud.entity;

public interface TransactionManagers {

    /**
     * 响应式的事务管理器
     */
    String reactiveTransactionManager = "connectionFactoryTransactionManager";

    /**
     * JDBC事务管理器
     */
    String jdbcTransactionManager = "transactionManager";

}
