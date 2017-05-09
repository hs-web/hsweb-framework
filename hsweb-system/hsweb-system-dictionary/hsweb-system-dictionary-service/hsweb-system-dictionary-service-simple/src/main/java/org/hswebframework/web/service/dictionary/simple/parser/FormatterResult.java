package org.hswebframework.web.service.dictionary.simple.parser;

/**
 *
 * @author zhouhao
 */
public class FormatterResult<V> {
    private V result;

    private String pattern;

    public FormatterResult() {
    }

    public FormatterResult(V result) {
        this(result, String.valueOf(result));
    }

    public FormatterResult(V result, String pattern) {
        this.result = result;
        this.pattern = pattern;
    }

    public V getResult() {
        return result;
    }

    public void setResult(V result) {
        this.result = result;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
