package org.hsweb.web.service.commons;

import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.dao.QueryMapper;
import org.hsweb.web.service.QueryService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 */
public interface SimpleQueryService<Po, Pk> extends QueryService<Po, Pk> {
    QueryMapper<Po, Pk> getQueryMapper();

    @Override
    default PagerResult<Po> selectPager(QueryParam param) {
        PagerResult<Po> pagerResult = new PagerResult<>();
        param.setPaging(false);
        int total = getQueryMapper().total(param);
        pagerResult.setTotal(total);
        if (total == 0) {
            pagerResult.setData(new ArrayList<>());
        } else {
            //根据实际记录数量重新指定分页参数
            param.rePaging(total);
            pagerResult.setData(getQueryMapper().select(param));
        }
        return pagerResult;
    }

    @Override
    @Transactional(readOnly = true)
    default List<Po> select(QueryParam param) {
        return getQueryMapper().select(param);
    }

    @Override
    @Transactional(readOnly = true)
    default int total(QueryParam param) {
        return getQueryMapper().total(param);
    }


    @Override
    @Transactional(readOnly = true)
    default Po selectByPk(Pk pk) {
        return getQueryMapper().selectByPk(pk);
    }

    @Override
    @Transactional(readOnly = true)
    default List<Po> select() {
        return this.createQuery().listNoPaging();
    }

}
