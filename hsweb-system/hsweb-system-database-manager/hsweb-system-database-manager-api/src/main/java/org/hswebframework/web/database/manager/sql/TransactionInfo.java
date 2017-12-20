package org.hswebframework.web.database.manager.sql;

import lombok.Data;
import org.hswebframework.web.database.manager.SqlInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TransactionInfo implements Serializable {
    private static final long serialVersionUID = -4174268983558799472L;
    private String id;

    private List<SqlInfo> sqlHistory=new ArrayList<>();

    private Date createTime;

    private Date lastExecuteTime;

}
