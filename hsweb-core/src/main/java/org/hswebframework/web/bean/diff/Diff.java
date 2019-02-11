package org.hswebframework.web.bean.diff;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Diff {

    private String fieldName;

    private String fieldComment;

    private Object before;

    private Object after;


}
