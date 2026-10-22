package org.hswebframework.web.crud.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.web.api.crud.entity.GenericTreeSortSupportEntity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.hswebframework.web.validator.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@Table(name = "test_tree_sort")
@EnableEntityEvent
public class TestTreeSortEntity extends GenericTreeSortSupportEntity<String> {


    @Column
    private String name;

    @Column
    private String groupId;

    @Column(nullable = false)
    @NotBlank(groups = CreateGroup.class)
    @DefaultValue("test")
    private String defaultTest;

    private List<TestTreeSortEntity> children;


    @Override
    public String toString() {
        return "TestTreeSortEntity{}";
    }
}
