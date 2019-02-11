package org.hswebframework.web.bean.diff;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class DiffBean {

    private Object before;

    private Object after;

    private List<Diff> diffs;





    public List<Diff> getDiffs() {
        return diffs;
    }
}
