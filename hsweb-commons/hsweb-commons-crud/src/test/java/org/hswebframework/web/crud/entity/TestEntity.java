package org.hswebframework.web.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.GenericEntity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "s_test")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class TestEntity extends GenericEntity<String> {

    @Column(length = 32)
    private String name;

    @Column
    private Integer age;

    @Override
    @GeneratedValue(generator = "md5")
    public String getId() {
        return super.getId();
    }
}
