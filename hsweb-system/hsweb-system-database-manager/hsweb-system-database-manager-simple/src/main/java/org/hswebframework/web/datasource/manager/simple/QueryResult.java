package org.hswebframework.web.datasource.manager.simple;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class QueryResult {
    private List<String> columns;

    private List<List<Object>> data=new ArrayList<>();

}
