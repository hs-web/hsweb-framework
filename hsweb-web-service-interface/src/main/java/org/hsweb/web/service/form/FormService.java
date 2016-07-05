package org.hsweb.web.service.form;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.service.GenericService;

import java.util.List;

/**
 * 自定义表单服务类
 * Created by generator
 */
public interface FormService extends GenericService<Form, String> {

    /**
     * 修改表单,不修改原始数据，而是新加入一条数据，版本号自动+1.
     */
    @Override
    int update(Form data) ;

    /**
     * 创建一个新版本的表单
     *
     * @param oldVersionId 旧版本表单ID
     * @return 新版本表单ID
     */
    String createNewVersion(String oldVersionId) ;

    /**
     * 查询最新版本的表单列表
     *
     * @param param 查询参数
     * @return 表单列表
     */
    List<Form> selectLatestList(QueryParam param) ;

    /**
     * 查询最新版本的表单数量
     *
     * @param param 查询参数
     * @return 表单数量
     */
    int countLatestList(QueryParam param) ;

    /**
     * 发布表单，发布表单后，可通过{@link DynamicFormService}进行调用.
     * 表单发布后，using属性自动改为true,其他已发布的版本将自动取消发布.
     *
     * @param formId 要发布的表单ID
     * @ 发布失败异常
     */
    void deploy(String formId) throws Exception;

    /**
     * 取消发布,取消发布后。表单失效。使用{@link DynamicFormService}后无法再进行调用
     *
     * @param formId 要取消发布的表单ID
     * @ 取消失败异常
     */
    void unDeploy(String formId) ;

    /**
     * 创建当前已部署表单对应的html，用于前端渲染.
     * 要创建的表单必须已经发布
     *
     * @param name 要创建html的表单名称
     * @return html字符串
     * @
     */
    String createDeployHtml(String name) ;

    Form selectDeployed(String name) ;

    /**
     * 根据表单名称，获取最新版本的表单
     *
     * @param name 表单名称
     * @return 表单对象，表单不存在将返回null
     * @
     */
    Form selectLatest(String name) ;

    /**
     * 根据表单名称和版本，获取表单
     *
     * @param name    表单名称
     * @param version 表单版本
     * @return 表单对象，表单不存在将返回null
     * @
     */
    Form selectByVersion(String name, int version) ;

    /**
     * 创建表单的html预览。
     *
     * @param id 表单ID
     * @return html 字符串
     * @
     */
    String createViewHtml(String id) ;

    /**
     * 查询当前正在使用的表单
     *
     * @param name 正在使用的表单名称
     * @return 表单对象。没有则返回null
     * @
     */
    Form selectUsing(String name) ;


}
