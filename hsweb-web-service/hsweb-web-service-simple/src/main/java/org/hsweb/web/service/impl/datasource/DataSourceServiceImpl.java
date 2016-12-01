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

package org.hsweb.web.service.impl.datasource;

import org.hsweb.web.bean.po.datasource.DataSource;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.dao.datasource.DataSourceMapper;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.datasource.DataSourceService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.hsweb.web.bean.po.GenericPo.Property.id;
import static org.hsweb.web.bean.po.datasource.DataSource.Property.*;

/**
 * 数据源服务类
 * Created by generator
 */
@Service("dataSourceService")
public class DataSourceServiceImpl extends AbstractServiceImpl<DataSource, String> implements DataSourceService {

    private static final String CACHE_NAME = "datasource";

    @Autowired
    protected DataSourceProperties properties;

    @Resource
    protected DataSourceMapper dataSourceMapper;

    @Resource
    protected ConfigService configService;

    @Autowired(required = false)
    protected DynamicDataSource dynamicDataSource;


    @PostConstruct
    public void init() {
        checkDynamicDataSourceSupport();
    }

    protected void checkDynamicDataSourceSupport() {
        if (dynamicDataSource == null)
            logger.warn("\ndynamicDataSource is not support! if you want use it,please import " +
                    "\n<!--------------------------------------------------------->\n" +
                    "       <dependency>\n" +
                    "            <groupId>org.hsweb</groupId>\n" +
                    "            <artifactId>hsweb-web-datasource</artifactId>\n" +
                    "       </dependency>" +
                    "\n<!--------------------------------------------------------->" +
                    "\n to pom.xml");
    }

    @Override
    protected DataSourceMapper getMapper() {
        return this.dataSourceMapper;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'id:'+#id")
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public DataSource selectByPk(String id) {
        return super.selectByPk(id);
    }

    @Override
    public String insert(DataSource data) {
        checkDynamicDataSourceSupport();
        data.setCreateDate(new Date());
        data.setEnabled(1);
        tryValidPo(data);
        DataSource old = createQuery().fromBean(data).where(name).or(id).single();
        if (old != null) throw new BusinessException("名称:" + data.getName() + "或id:" + data.getId() + "已存在");
        return super.insert(data);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int update(List<DataSource> data) {
        return super.update(data);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "'id:'+#data.id")
    public int update(DataSource data) {
        checkDynamicDataSourceSupport();
        DataSource old = createQuery().fromBean(data).where(name).and().not(id).single();
        if (old != null) throw new BusinessException("名称" + data.getName() + "已存在");
        return createUpdate(data).excludes(createDate, enabled).where(id, data.getId()).exec();
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "'id:'+#id")
    public void enable(String id) {
        checkDynamicDataSourceSupport();
        createUpdate().set(enabled, 1).includes(enabled).where(DataSource.Property.id, id).exec();
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "'id:'+#id")
    public void disable(String id) {
        checkDynamicDataSourceSupport();
        createUpdate().set(enabled, 0).includes(enabled).where(DataSource.Property.id, id).exec();
    }


    @Override
    @CacheEvict(value = CACHE_NAME, key = "'id:'+#id")
    public int delete(String id) {
        return super.delete(id);
    }

}
