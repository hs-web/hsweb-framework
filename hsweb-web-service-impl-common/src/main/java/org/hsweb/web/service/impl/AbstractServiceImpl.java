package org.hsweb.web.service.impl;

import org.hsweb.web.bean.common.*;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.valid.ValidResults;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.exception.ValidationException;
import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by 浩 on 2016-01-22 0022.
 */
@Transactional(rollbackFor = Throwable.class)
public abstract class AbstractServiceImpl<Po, PK> implements GenericService<Po, PK> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected Validator validator;

    protected abstract GenericMapper<Po, PK> getMapper();

    @Override
    @Transactional(readOnly = true)
    public PagerResult<Po> selectPager(QueryParam param) {
        PagerResult<Po> pagerResult = new PagerResult<>();
        param.setPaging(false);
        int total = getMapper().total(param);
        pagerResult.setTotal(total);
        //根据实际记录数量重新指定分页参数
        param.rePaging(total);
        pagerResult.setData(getMapper().select(param));
        return pagerResult;
    }

    @Override
    public PK insert(Po data) {
        PK primaryKey = null;
        if (data instanceof GenericPo) {
            if (((GenericPo) data).getId() == null)
                ((GenericPo) data).setId(GenericPo.createUID());
            primaryKey = (PK) ((GenericPo) data).getId();
        }
        tryValidPo(data);
        getMapper().insert(InsertParam.build(data));
        return primaryKey;
    }

    public List<PK> batchInsert(List<Po> data, boolean skipFail) {
        List<PK> pkList = new ArrayList<>();
        List<Po> insertData = new ArrayList<>();
        //build
        for (Po po : data) {
            if (data instanceof GenericPo) {
                if (((GenericPo) data).getId() == null)
                    ((GenericPo) data).setId(GenericPo.createUID());
                PK primaryKey = (PK) ((GenericPo) data).getId();
                try {
                    tryValidPo(po);
                    insertData.add(po);
                    pkList.add(primaryKey);
                } catch (ValidationException e) {
                    if (!skipFail) throw e;
                    else if (logger.isWarnEnabled()) {
                        logger.warn("data validate fail:{}", e);
                    }
                }

            }
        }
        getMapper().insert((InsertParam) InsertParam.build(insertData));
        return pkList;
    }

    @Override
    public int delete(PK pk) {
        return getMapper().delete(DeleteParam.build().where("id", pk));
    }

    @Override
    public int update(Po data) {
        return getMapper().update(UpdateParam.build(data));
    }

    @Override
    public int update(List<Po> data) {
        int i = 0;
        for (Po po : data) {
            i += getMapper().update(UpdateParam.build(po));
        }
        return i;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Po> select(QueryParam param) {
        return this.getMapper().select(param);
    }

    @Transactional(readOnly = true)
    public List<Po> select() {
        return this.getMapper().select(QueryParam.build().noPaging());
    }

    @Override
    @Transactional(readOnly = true)
    public int total(QueryParam param) {
        return this.getMapper().total(param);
    }

    @Override
    @Transactional(readOnly = true)
    public Po selectByPk(PK pk) {
        return this.getMapper().selectByPk(pk);
    }

    @Override
    public int saveOrUpdate(Po po) {
        if (po instanceof GenericPo) {
            Po old = selectByPk((PK) ((GenericPo) po).getId());
            if (old != null)
                return update(po);
            else
                insert(po);
        } else {
            throw new UnsupportedOperationException("不支持此操作");
        }
        return 1;
    }

    protected void assertNotNull(Object po, String message) {
        if (po == null) throw new NotFoundException(message);
    }

    protected void tryValidPo(Po data) {
        Set<ConstraintViolation<Object>> set = validator.validate(data);
        ValidResults results = new ValidResults();
        if (set.size() != 0) {
            for (ConstraintViolation<Object> violation : set) {
                results.addResult(violation.getPropertyPath().toString(), violation.getMessage());
            }
        }
        if (!results.isSuccess())
            throw new ValidationException(results);
    }
}
