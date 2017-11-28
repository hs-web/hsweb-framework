package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.CustomDataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

/**
 * @author zhouhao
 */
public class SimpleCustomDataAccessConfigConfig extends AbstractDataAccessConfig implements CustomDataAccessConfig {

    private static final long serialVersionUID = -8754634247843748887L;

    private String classOrBeanName;

    private static Logger logger = LoggerFactory.getLogger(CustomDataAccessConfig.class);

    private transient DataAccessController instance;

    public SimpleCustomDataAccessConfigConfig() {
    }

    public SimpleCustomDataAccessConfigConfig(String classOrBeanName) {
        this.classOrBeanName = classOrBeanName;
        try {
            instance = (DataAccessController) ClassUtils.forName(getClassOrBeanName(), this.getClass().getClassLoader()).newInstance();
        } catch (Exception e) {
            logger.error("init CustomDataAccessConfig error", e);
        }
    }

    @Override
    public DataAccessController getController() {
        if (instance == null) {
            throw new UnsupportedOperationException(new ClassNotFoundException(classOrBeanName));
        }
        return instance;
    }

    public String getClassOrBeanName() {
        return classOrBeanName;
    }

    public void setClassOrBeanName(String classOrBeanName) {
        this.classOrBeanName = classOrBeanName;
    }
}
