package org.hswebframework.web.database.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhouhao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteRequest implements Serializable{
    private static final long serialVersionUID = 8640714319329779262L;
    private List<SqlInfo> sql;

}
