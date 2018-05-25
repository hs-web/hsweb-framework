package org.hswebframework.web.dao.crud;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "h_test")
@Getter
@Setter
@ToString
public class TestEntity implements org.hswebframework.web.commons.entity.Entity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "name",
            columnDefinition = "varchar COMMENT '创建时间'"
    )
    private String name;

    @Column(
            name = "create_time",
            columnDefinition = "timestamp COMMENT '创建时间'"
    )
    private Date createTime;

    @Column(
            name = "data_type",
            columnDefinition = "bigint COMMENT '类型'"
    )
    private DataType dataType;

    @Column(
            name = "data_types",
            columnDefinition = "bigint COMMENT '多个类型'"
    )
    private DataType[] dataTypes;

    private NestEntity nest;
}
