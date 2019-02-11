package org.hswebframework.web.bean.diff;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Diff {

    private String field;

    private String comment;

    private String type;

    private Object before;

    private Object after;


}
