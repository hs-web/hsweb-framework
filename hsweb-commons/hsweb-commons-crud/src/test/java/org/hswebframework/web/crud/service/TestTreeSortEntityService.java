package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.entity.TestTreeSortEntity;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestTreeSortEntityService extends GenericReactiveCrudService<TestTreeSortEntity,String>
        implements ReactiveTreeSortEntityService<TestTreeSortEntity,String> {

    @Override
    public IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public void setChildren(TestTreeSortEntity entity, List<TestTreeSortEntity> children) {
        entity.setChildren(children);
    }

    @Override
    public List<TestTreeSortEntity> getChildren(TestTreeSortEntity entity) {
        return entity.getChildren();
    }


}
