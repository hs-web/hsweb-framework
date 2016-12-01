package org.hsweb.web.dao;

/**
 * @author zhouhao
 */
public interface CRUMapper<Po, Pk> extends InsertMapper<Po>, QueryMapper<Po, Pk>, UpdateMapper<Po> {
}
