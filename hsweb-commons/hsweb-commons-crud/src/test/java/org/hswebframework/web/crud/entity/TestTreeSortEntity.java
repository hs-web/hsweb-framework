package org.hswebframework.web.crud.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.GenericTreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Table(name = "test_tree_sort")
public class TestTreeSortEntity  extends GenericTreeSortSupportEntity<String> {


    @Column
    private String name;


    private List<TestTreeSortEntity> children;


}
