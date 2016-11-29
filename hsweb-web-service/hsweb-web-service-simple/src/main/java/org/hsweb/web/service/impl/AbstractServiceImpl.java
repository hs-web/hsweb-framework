package org.hsweb.web.service.impl;

import org.hsweb.ezorm.core.dsl.Delete;
import org.hsweb.ezorm.core.dsl.Query;
import org.hsweb.ezorm.core.dsl.Update;
import org.hsweb.web.bean.common.*;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.validator.ValidateResults;
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
 * 抽象通用服务实现类，通过指定{@link GenericMapper} 实现通用的增删改查方法
 *
 * @param <Po> PO类型
 * @param <PK> 主键类型
 * @author zhouhao
 * @see GenericService
 * @since 1.0
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
        if (total == 0) {
            pagerResult.setData(new ArrayList<>());
        } else {
            //根据实际记录数量重新指定分页参数
            param.rePaging(total);
            pagerResult.setData(getMapper().select(param));
        }
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

    @Deprecated
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
                    if (!skipFail) {
                        throw e;
                    } else if (logger.isWarnEnabled()) {
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
        return createDelete().where(GenericPo.Property.id, pk).exec();
    }

    @Override
    public int update(Po data) {
        return createUpdate().fromBean(data).where(GenericPo.Property.id).exec();
    }

    @Override
    public int update(List<Po> data) {
        int i = 0;
        for (Po po : data) {
            i += update(po);
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
        return createQuery().listNoPaging();
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
        if (po == null) {
            throw new NotFoundException(message);
        }
    }

    protected void assertNotNull(Object po) {
        assertNotNull(po, "数据不存在");
    }

    protected void tryValidPo(Po data) {
        Set<ConstraintViolation<Object>> set = validator.validate(data);
        ValidateResults results = new ValidateResults();
        if (set.isEmpty()) {
            for (ConstraintViolation<Object> violation : set) {
                results.addResult(violation.getPropertyPath().toString(), violation.getMessage());
            }
        }
        if (!results.isSuccess())
            throw new ValidationException(results);
    }

    /**
     * 创建dsl更新操作对象，默认使用map进行数据填充,调用此方法,需要通过{@link Update#set(String, Object)}进行属性赋值
     *
     * @return {@link Update}
     * @see Update
     * @see GenericService#createUpdate(GenericMapper)
     */
    public Update<Po, UpdateParam<Po>> createUpdate() {
        return GenericService.createUpdate(getMapper());
    }

    /**
     * 创建dsl更新操作对象，并指定要操作的数据
     *
     * @return {@link Update}
     * @see Update
     * @see GenericService#createUpdate(GenericMapper)
     */
    public Update<Po, UpdateParam<Po>> createUpdate(Po data) {
        return Update.build(getMapper()::update, new UpdateParam<>(data));
    }

    /**
     * 创建dsl删除操作对象
     *
     * @return {@link Delete}
     * @see Delete
     * @see GenericService#createDelete(Delete.Executor)
     */
    public Delete createDelete() {
        return GenericService.createDelete(getMapper());
    }

}
