package org.hswebframework.web.entity.authorization;


/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleActionEntity implements ActionEntity {

    private String action;

    private String describe;

    private boolean defaultCheck;

    public SimpleActionEntity() {
    }

    public SimpleActionEntity(String action) {
        this.action = action;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String getDescribe() {
        return describe;
    }

    @Override
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public boolean isDefaultCheck() {
        return defaultCheck;
    }

    @Override
    public void setDefaultCheck(boolean defaultCheck) {
        this.defaultCheck = defaultCheck;
    }

    @Override
    public SimpleActionEntity clone() {
        SimpleActionEntity target = new SimpleActionEntity();
        target.setAction(getAction());
        target.setDescribe(getDescribe());
        target.setDefaultCheck(isDefaultCheck());
        return target;
    }
}
