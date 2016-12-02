package org.hsweb.web.service;

/**
 * 通用Service,实现增删改查
 *
 * @author zhouhao
 * @since 1.0
 */
public interface GenericService<Po, Pk> extends QueryService<Po, Pk>, UpdateService<Po>, InsertService<Po, Pk>, DeleteService<Pk> {

}
