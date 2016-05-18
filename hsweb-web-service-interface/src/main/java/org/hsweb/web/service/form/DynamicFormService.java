package org.hsweb.web.service.form;

import org.hsweb.web.bean.common.*;
import org.hsweb.web.bean.po.form.Form;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-14.
 */
public interface DynamicFormService {

    Object parseMeta(Form form) throws Exception;

    void deploy(Form form) throws Exception;

    void unDeploy(Form form) throws Exception;

    <T> PagerResult<T> selectPager(String name, QueryParam param) throws Exception;

    <T> List<T> select(String name, QueryParam param) throws Exception;

    int total(String name, QueryParam param) throws Exception;

    String insert(String name, InsertParam<Map<String, Object>> data) throws Exception;

    String saveOrUpdate(String name, Map<String, Object> map) throws Exception;

    int delete(String name, DeleteParam param) throws Exception;

    boolean deleteByPk(String name, String pk) throws Exception;

    int update(String name, UpdateParam<Map<String, Object>> param) throws Exception;

    int updateByPk(String name, String pk, UpdateParam<Map<String, Object>> param) throws Exception;

    <T> T selectByPk(String name, Object pk) throws Exception;

    void exportExcel(String name, QueryParam param, OutputStream outputStream) throws Exception;

    Map<String, Object> importExcel(String name, InputStream inputStream) throws Exception;
}
