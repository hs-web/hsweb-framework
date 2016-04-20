package org.hsweb.web.service.impl;

import org.hsweb.web.bean.common.*;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.valid.ValidResults;
import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.util.List;

/**
 * Created by 浩 on 2016-01-22 0022.
 */
@Transactional(rollbackFor = Throwable.class)
public abstract class AbstractServiceImpl<Po, PK> implements GenericService<Po, PK> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

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
        getMapper().insert(new InsertParam<>(data));
        if (data instanceof GenericPo) {
            return (PK) ((GenericPo) data).getU_id();
        }
        return null;
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
        return this.getMapper().select(new QueryParam());
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

    protected void tryValidPo(Po data) {
        ValidResults results;
        if (data instanceof GenericPo) {
            results = ((GenericPo) data).valid();
        } else {
            results = GenericPo.valid(data);
        }
        if (!results.isSuccess())
            throw new ValidationException(results.toString());
    }
}
