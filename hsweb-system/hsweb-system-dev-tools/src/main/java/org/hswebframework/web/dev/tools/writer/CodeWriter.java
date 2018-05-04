package org.hswebframework.web.dev.tools.writer;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface CodeWriter {
    String write(List<GeneratedCode> codes);
}
