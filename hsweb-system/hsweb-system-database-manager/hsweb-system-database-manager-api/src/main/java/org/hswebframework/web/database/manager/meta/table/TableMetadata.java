package org.hswebframework.web.database.manager.meta.table;

import lombok.*;
import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableMetadata extends ObjectMetadata {
    private static final long serialVersionUID = 1762059989615865556L;

    private String comment;

    private List<Constraint> constraints;

    private List<ColumnMetadata> columns;
}
