package org.hswebframework.web.service.oauth2;

import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity extends SimpleGenericEntity<String> {
    private static final long serialVersionUID = 6405200441627288263L;
    private String name;

    private boolean boy;

    private Date createTime;
}
