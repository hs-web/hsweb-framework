package org.hsweb.web.service;

/**
 * @author zhouhao
 */
public interface InsertService<Po,Pk> {

    /**
     * 添加一条数据
     *
     * @param data 要添加的数据
     * @return 添加后生成的主键
     */
    Pk insert(Po data);
}
