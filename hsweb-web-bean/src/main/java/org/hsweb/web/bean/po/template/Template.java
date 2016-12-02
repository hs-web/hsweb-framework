package org.hsweb.web.bean.po.template;

import org.hsweb.web.bean.po.GenericPo;

import java.util.List;

public class Template extends GenericPo<String> {

    private String name;

    private String remark;

    private String template;

    private String classifiedId;

    private String type;

    private String script;

    private String css;

    private List<String> cssLinks;

    private List<String> scriptLinks;

    private int version;

    private int revision;

    private int release;

    private boolean using;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getClassifiedId() {
        return classifiedId;
    }

    public void setClassifiedId(String classifiedId) {
        this.classifiedId = classifiedId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public List<String> getCssLinks() {
        return cssLinks;
    }

    public void setCssLinks(List<String> cssLinks) {
        this.cssLinks = cssLinks;
    }

    public List<String> getScriptLinks() {
        return scriptLinks;
    }

    public void setScriptLinks(List<String> scriptLinks) {
        this.scriptLinks = scriptLinks;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public int getRelease() {
        return release;
    }

    public void setRelease(int release) {
        this.release = release;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }



public interface Property extends GenericPo.Property{
	/**
	 *
	 * @see Template#name
	 */
	String name="name";
	/**
	 *
	 * @see Template#remark
	 */
	String remark="remark";
	/**
	 *
	 * @see Template#template
	 */
	String template="template";
	/**
	 *
	 * @see Template#classifiedId
	 */
	String classifiedId="classifiedId";
	/**
	 *
	 * @see Template#type
	 */
	String type="type";
	/**
	 *
	 * @see Template#script
	 */
	String script="script";
	/**
	 *
	 * @see Template#css
	 */
	String css="css";
	/**
	 *
	 * @see Template#cssLinks
	 */
	String cssLinks="cssLinks";
	/**
	 *
	 * @see Template#scriptLinks
	 */
	String scriptLinks="scriptLinks";
	/**
	 *
	 * @see Template#version
	 */
	String version="version";
	/**
	 *
	 * @see Template#revision
	 */
	String revision="revision";
	/**
	 *
	 * @see Template#release
	 */
	String release="release";
	/**
	 *
	 * @see Template#using
	 */
	String using="using";
	}
}