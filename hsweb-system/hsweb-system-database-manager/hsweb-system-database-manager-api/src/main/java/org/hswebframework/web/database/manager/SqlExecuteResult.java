package org.hswebframework.web.database.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhouhao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteResult {

    private SqlInfo sqlInfo;

    private Object result;

    private boolean success;

}
