package org.hswebframework.web.dao.mybatis.builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hswebframework.web.commons.entity.QueryEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestQueryEntity implements QueryEntity {

    private String name$like;

    private int age$gt;

    private TestQueryEntity or;
}