package org.hsweb.web.controller;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.hsweb.commons.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用Controller，使用RESTful和json进行数据提交及访问。
 * 如果要进行权限控制，可以在方法上注解{@link Authorize}
 * <br/>所有Controller应继承改类，并手动注解@Controller 以及@RequestMapping
 * <br/>json解析使用fastJson
 * Created by 浩 on 2015-07-28 0028.
 */
public abstract class GenericController<PO, PK> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取此Controller 需要的服务类
     *
     * @return
     */
    protected abstract GenericService<PO, PK> getService();

    /**
     * 获取PO的类型
     *
     * @return PO类型
     */
    protected final Class<PO> getPOType() {
        return (Class<PO>) ClassUtils.getGenericType(this.getClass(), 0);
    }

    /**
     * 获取PK(主键)的类型
     *
     * @return PK(主键)类型
     */
    protected final Class<PK> getPKType() {
        return (Class<PK>) ClassUtils.getGenericType(this.getClass(), 1);
    }

    /**
     * 查询列表,并返回查询结果
     *
     * @param param 查询参数
     * @return 返回请求结果
     */
    @RequestMapping(method = RequestMethod.GET)
    @AccessLogger("查询列表")
    @Authorize(action = "R")
    public ResponseMessage list(QueryParam param) {
        // 获取条件查询
        Object data;
        if (!param.isPaging())//不分页
            data = getService().select(param);
        else
            data = getService().selectPager(param);
        return ResponseMessage.ok(data)
                .include(getPOType(), param.getIncludes())
                .exclude(getPOType(), param.getExcludes())
                .onlyData();
    }

    /**
     * 根据id（主键）查询数据
     *
     * @param id 主键
     * @return 请求结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @AccessLogger("查询明细")
    @Authorize(action = "R")
    public ResponseMessage info(@PathVariable("id") PK id) {
        PO po = getService().selectByPk(id);
        if (po == null)
            throw new BusinessException("data is not found!", 404);
        return ResponseMessage.ok(po);
    }


    /**
     * 根据查询条件，查询数据数量
     *
     * @param param 查询条件
     * @return 请求结果
     */
    @RequestMapping(value = "/total", method = RequestMethod.GET)
    @AccessLogger("查询总数")
    @Authorize(action = "R")
    public ResponseMessage total(QueryParam param) {
        // 获取条件查询
        return ResponseMessage.ok(getService().total(param));
    }

    /**
     * 请求添加数据，请求必须以POST方式，必要参数为：json
     *
     * @param object 前端请求的对象
     * @return 请求结果
     */
    @RequestMapping(method = RequestMethod.POST)
    @AccessLogger("新增")
    @Authorize(action = "C")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseMessage add(@RequestBody PO object) {
        PK pk = getService().insert(object);
        return ResponseMessage.created(pk);
    }

    /**
     * 请求删除指定id的数据，请求方式为DELETE，使用rest风格，如请求 /delete/1 ，将删除id为1的数据
     *
     * @param id 要删除的id标识
     * @return 请求结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @AccessLogger("删除")
    @Authorize(action = "D")
    public ResponseMessage delete(@PathVariable("id") PK id) {
        PO old = getService().selectByPk(id);
        if (old == null) throw new NotFoundException("data is not found!");
        int number = getService().delete(id);
        return ResponseMessage.ok(number);
    }

    /**
     * 请求更新数据，请求必须以PUT方式
     *
     * @param object 前端请求的对象
     * @return 请求结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @AccessLogger("修改")
    @Authorize(action = "U")
    public ResponseMessage update(@PathVariable("id") PK id, @RequestBody(required = true) PO object) {
        PO old = getService().selectByPk(id);
        if (old == null) throw new NotFoundException("data is not found!");
        if (object instanceof GenericPo) {
            ((GenericPo) object).setId(id);
        }
        int number = getService().update(object);
        return ResponseMessage.ok(number);
    }

    /**
     * 请求更新数据，请求必须以PUT方式，必要参数为：json
     *
     * @param json 前端请求的对象
     * @return 请求结果
     */
    @RequestMapping(method = RequestMethod.PUT)
    @AccessLogger("批量修改")
    @Authorize(action = "U")
    public ResponseMessage update(@RequestBody(required = true) String json) {
        int number;
        if (json.startsWith("[")) {
            List<PO> datas = JSON.parseArray(json, getPOType());
            number = getService().update(datas);
        } else if (json.startsWith("{")) {
            PO data = JSON.parseObject(json, getPOType());
            List<PO> datas = new ArrayList<>();
            datas.add(data);
            number = getService().update(datas);
        } else {
            throw new BusinessException("请求数据格式错误!");
        }
        return ResponseMessage.ok(number);
    }
}
