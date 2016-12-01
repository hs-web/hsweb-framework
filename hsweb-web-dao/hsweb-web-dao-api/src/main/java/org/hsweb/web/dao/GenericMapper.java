package org.hsweb.web.dao;

/**
 * 通用dao，定义常用的增删改查操作。其他daoMapper接口继承此接口，则无需再定义这些方法
 */
public interface GenericMapper<Po, Pk> extends QueryMapper<Po, Pk>, UpdateMapper<Po>, InsertMapper<Po>, DeleteMapper {

}
