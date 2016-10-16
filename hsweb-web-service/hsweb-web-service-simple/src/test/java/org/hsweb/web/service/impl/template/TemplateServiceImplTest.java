package org.hsweb.web.service.impl.template;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.template.Template;
import org.hsweb.web.dao.template.TemplateMapper;
import org.hsweb.web.service.template.TemplateService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by zhouhao on 16-5-23.
 */
public class TemplateServiceImplTest {

    @Mock
    private TemplateService templateService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TemplateMapper templateMapper = mock(TemplateMapper.class);
        System.out.println(templateMapper.select(QueryParam.build()));
        System.out.println(templateMapper.insert(null));
    }

    @Test
    public void test() {
        System.out.println(templateService.insert(null));
    }


}