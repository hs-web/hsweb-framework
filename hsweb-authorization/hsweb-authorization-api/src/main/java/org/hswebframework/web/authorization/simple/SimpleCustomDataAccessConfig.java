package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.CustomDataAccess;
import org.hswebframework.web.authorization.access.DataAccessController;

/**
 * @author zhouhao
 */
public class SimpleCustomDataAccessConfig extends AbstractDataAccessConfig implements CustomDataAccess {

    private String classOrBeanName;

    private transient DataAccessController instance;

    public SimpleCustomDataAccessConfig() {
    }

    public SimpleCustomDataAccessConfig(String classOrBeanName) {
        this.classOrBeanName = classOrBeanName;
    }

    @Override
    public DataAccessController getController() {
        if (instance == null) {
            synchronized (this) {
                // TODO: 17-2-8  spring bean not support now!
                if (instance == null)
                    try {
                        instance = (DataAccessController) Class.forName(classOrBeanName).newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            }
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
