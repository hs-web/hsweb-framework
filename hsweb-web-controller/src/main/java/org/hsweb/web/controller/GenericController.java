package org.hsweb.web.controller;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.authorize.annotation.AccessLogger;
import org.hsweb.web.authorize.annotation.Authorize;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.message.ResponseMessage;
import org.hsweb.web.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.webbuilder.utils.common.ClassUtils;

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
    @Authorize(level = "R")
    public ResponseMessage list(QueryParam param) {
        // 获取条件查询
        try {
            Object data;
            if (!param.isPaging())//不分页
                data = getService().select(param);
            else
                data = getService().selectPager(param);
            return new ResponseMessage(true, data).onlyData();
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    /**
     * 根据id（主键）查询数据
     *
     * @param id 主键
     * @return 请求结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @AccessLogger("查询明细")
    @Authorize(level = "R")
    public ResponseMessage info(@PathVariable("id") PK id) {
        try {
            PO po = getService().selectByPk(id);
            if (po == null)
                return new ResponseMessage(false, "data is not found!", "404");
            return new ResponseMessage(true, po);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }


    /**
     * 根据查询条件，查询数据数量
     *
     * @param param 查询条件
     * @return 请求结果
     */
    @RequestMapping(value = "/total", method = RequestMethod.GET)
    @AccessLogger("查询总数")
    @Authorize(level = "R")
    public ResponseMessage total(QueryParam param) {
        try {
            // 获取条件查询
            return new ResponseMessage(true, getService().total(param));
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    /**
     * 请求添加数据，请求必须以POST方式，必要参数为：json
     *
     * @param object 前端请求的对象
     * @return 请求结果
     */
    @RequestMapping(method = RequestMethod.POST)
    @AccessLogger("新增")
    @Authorize(level = "C")
    public ResponseMessage add(@RequestBody PO object) {
        try {
            PK pk = getService().insert(object);
            return new ResponseMessage(true, pk);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    /**
     * 请求删除指定id的数据，请求方式为DELETE，使用rest风格，如请求 /delete/1 ，将删除id为1的数据
     *
     * @param id 要删除的id标识
     * @return 请求结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @AccessLogger("删除")
    @Authorize(level = "D")
    public ResponseMessage delete(@PathVariable("id") PK id) {
        try {
            int number = getService().delete(id);
            return new ResponseMessage(true, number);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    /**
     * 请求更新数据，请求必须以PUT方式
     *
     * @param object 前端请求的对象
     * @return 请求结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @AccessLogger("修改")
    @Authorize(level = "U")
    public ResponseMessage update(@PathVariable("id") PK id, @RequestBody(required = true) PO object) {
        try {
            if (object instanceof GenericPo) {
                ((GenericPo) object).setU_id(id);
            }
            int number = getService().update(object);
            return new ResponseMessage(true, number);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    /**
     * 请求更新数据，请求必须以PUT方式，必要参数为：json
     *
     * @param json 前端请求的对象
     * @return 请求结果
     */
    @RequestMapping(method = RequestMethod.PUT)
    @AccessLogger("批量修改")
    @Authorize(level = "U")
    public ResponseMessage update(@RequestBody(required = true) String json) {
        try {
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
                return new ResponseMessage(false, "数据错误");
            }
            return new ResponseMessage(true, number);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }
}
