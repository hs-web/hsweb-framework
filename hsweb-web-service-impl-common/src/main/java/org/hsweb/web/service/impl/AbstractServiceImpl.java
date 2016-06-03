package org.hsweb.web.service.impl;

import org.hsweb.web.bean.common.*;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.valid.ValidResults;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.exception.ValidationException;
import org.hsweb.web.core.utils.RandomUtil;
import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
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
    public PagerResult<Po> selectPager(QueryParam param) throws Exception {
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
    public PK insert(Po data) throws Exception {
        PK primaryKey = null;
        if (data instanceof GenericPo) {
            if (((GenericPo) data).getId() == null)
                ((GenericPo) data).setId(GenericPo.createUID());
            primaryKey = (PK) ((GenericPo) data).getId();
        }
        tryValidPo(data);
        getMapper().insert(new InsertParam<>(data));
        return primaryKey;
    }

    @Override
    public int delete(PK pk) throws Exception {
        return getMapper().delete(new DeleteParam().where("primaryKey", pk));
    }

    @Override
    public int update(Po data) throws Exception {
        return getMapper().update(new UpdateParam<>(data));
    }

    @Override
    public int update(List<Po> data) throws Exception {
        int i = 0;
        for (Po po : data) {
            i += getMapper().update(new UpdateParam<>(po));
        }
        return i;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Po> select(QueryParam param) throws Exception {
        return this.getMapper().select(param);
    }

    @Transactional(readOnly = true)
    public List<Po> select() throws Exception {
        return this.getMapper().select(new QueryParam().noPaging());
    }

    @Override
    @Transactional(readOnly = true)
    public int total(QueryParam param) throws Exception {
        return this.getMapper().total(param);
    }

    @Override
    @Transactional(readOnly = true)
    public Po selectByPk(PK pk) throws Exception {
        return this.getMapper().selectByPk(pk);
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
