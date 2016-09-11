package org.hsweb.web.service.impl.template;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.template.Template;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.hsweb.web.service.template.TemplateService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by zhouhao on 16-5-23.
 */
@Rollback
@Transactional
public class TemplateServiceImplTest extends AbstractTestCase {

    @Resource
    private TemplateService templateService;

    private String templateId;

    @Before
    public void setUp() throws Exception {
        Template template = new Template();
        template.setName("test");
        template.setCssLinks(Arrays.asList("http://***/template.css", "/test.css"));
        template.setScriptLinks(Arrays.asList("http://***/template.js", "/test.js"));
        template.setTemplate("你好:{{user.name}}");
        template.setType("default");
        templateService.insert(template);
        templateId = template.getId();
        testDeploy();
    }

    @Test
    public void testCreateNewVersion() throws Exception {
        String id = templateService.createNewVersion(templateId);
        Template newVersion = templateService.selectByPk(id);
        Assert.assertEquals(newVersion.getVersion(), 1);
        Assert.assertEquals(newVersion.getCssLinks().size(), 2);
        Assert.assertEquals(newVersion.getScriptLinks().size(), 2);
        Assert.assertEquals(newVersion.getTemplate(), "你好:{{user.name}}");
    }

    @Test
    public void testSelectLatestList() throws Exception {
        List<Template> list = templateService.selectLatestList(new QueryParam());
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getVersion(), 1);
    }

    public void testDeploy() throws Exception {
        templateService.deploy(templateId);
        Template template = templateService.selectDeploy("test");
        Assert.assertEquals(template.getId(), templateId);
    }

    @After
    public void testUnDeploy() throws Exception {
        templateService.unDeploy(templateId);
        Template template = templateService.selectDeploy("test");
        Assert.assertNotNull(template);
    }

    @Test
    public void testSelectByVersion() throws Exception {
        Template template = templateService.selectByVersion("name", 2);
        Assert.assertNull(template);
    }
}