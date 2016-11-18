/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.controller;

import com.alibaba.fastjson.JSON;
import org.hsweb.commons.ClassUtils;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

import static org.hsweb.web.core.message.ResponseMessage.*;

/**
 * 通用控制器,此控制器实现了通用的增删改查功能
 * 需要提供一个实现了{@link GenericService}接口的实现类
 */
public abstract class GenericController<PO, PK> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取此Controller需要的服务类,由子类实现
     *
     * @return 通用服务类
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
     * @param param 查询参数 {@link QueryParam}
     * @return 查询结果, 如果参数指定了分页(默认指定)将返回格式如:{total:数据总数,data:[{}]}的数据.
     * 否则返回格式[{}]
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
        return ok(data)
                .include(getPOType(), param.getIncludes())
                .exclude(getPOType(), param.getExcludes())
                .onlyData();
    }

    /**
     * 根据id（主键）查询数据
     *
     * @param id 主键
     * @return 请求结果
     * @throws NotFoundException 要查询的数据不存在
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @AccessLogger("查询明细")
    @Authorize(action = "R")
    public ResponseMessage info(@PathVariable("id") PK id) {
        PO po = getService().selectByPk(id);
        if (po == null)
            throw new NotFoundException("data is not found!");
        return ok(po);
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
        return ok(getService().total(param));
    }

    /**
     * 请求添加数据，请求必须以POST方式
     *
     * @param object 请求添加的对象
     * @return 被添加数据的主键值
     * @throws javax.validation.ValidationException 验证数据格式错误
     */
    @RequestMapping(method = RequestMethod.POST)
    @AccessLogger("新增")
    @Authorize(action = "C")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseMessage add(@RequestBody PO object) {
        PK pk = getService().insert(object);
        return created(pk);
    }

    /**
     * 请求删除指定id的数据，请求方式为DELETE，使用rest风格，如请求 /delete/1 ，将删除id为1的数据
     *
     * @param id 要删除的id标识
     * @return 删除结果
     * @throws NotFoundException 要删除的数据不存在
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @AccessLogger("删除")
    @Authorize(action = "D")
    public ResponseMessage delete(@PathVariable("id") PK id) {
        PO old = getService().selectByPk(id);
        assertFound(old, "data is not found!");
        getService().delete(id);
        return ok();
    }

    /**
     * 根据主键修改数据
     *
     * @param id     要修改数据的主键值
     * @param object 要修改的数据
     * @return 请求结果
     * @throws NotFoundException 要修改的数据不存在
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @AccessLogger("修改")
    @Authorize(action = "U")
    public ResponseMessage update(@PathVariable("id") PK id, @RequestBody PO object) {
        PO old = getService().selectByPk(id);
        assertFound(old, "data is not found!");
        if (object instanceof GenericPo) {
            ((GenericPo) object).setId(id);
        }
        int number = getService().update(object);
        return ok(number);
    }

    /**
     * 批量修改数据.
     *
     * @param json 请求修改的数据 json格式
     * @return 被修改数据的条数
     * @throws BusinessException 请求的数据格式错误
     */
    @RequestMapping(method = RequestMethod.PUT)
    @AccessLogger("批量修改")
    @Authorize(action = "U")
    public ResponseMessage update(@RequestBody String json) {
        int number;
        if (json.startsWith("[")) {
            number = getService().update(JSON.parseArray(json, getPOType()));
        } else if (json.startsWith("{")) {
            number = getService().update(Arrays.asList(JSON.parseObject(json, getPOType())));
        } else {
            throw new BusinessException("请求数据格式错误!");
        }
        return ok(number);
    }

    /**
     * 判断对象是否为空,如果为空将抛出 {@link NotFoundException}
     *
     * @param obj 要判断的对象
     * @param msg 为null时异常消息
     */
    public void assertFound(Object obj, String msg) {
        if (obj == null) throw new NotFoundException(msg);
    }
}
