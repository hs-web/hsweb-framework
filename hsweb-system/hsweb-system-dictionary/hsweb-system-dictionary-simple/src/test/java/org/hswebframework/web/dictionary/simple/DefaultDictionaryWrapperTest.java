package org.hswebframework.web.dictionary.simple;

import org.hswebframework.web.dictionary.api.DictionaryInfo;
import org.hswebframework.web.dictionary.api.DictionaryInfoService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultDictionaryWrapperTest {

    @Mock
    private DictionaryInfoService dictionaryInfoService;

    @InjectMocks
    private DefaultDictionaryWrapper wrapper = new DefaultDictionaryWrapper();

    @Before
    public void init() {
        List<DictionaryInfo> infos = new ArrayList<>();
        infos.add(DictionaryInfo.builder()
                .value(TestDict.CODE0.getValue())
                .build());
        infos.add(DictionaryInfo.builder()
                .value(TestDict.CODE1.getValue())
                .build());

        when(dictionaryInfoService.select("org.hswebframework.web.dictionary.simple.TestBean.dict", "test", "TestDict"))
                .thenReturn(infos);

        when(dictionaryInfoService.select("org.hswebframework.web.dictionary.simple.TestBean.dict2", "test", "TestDict"))
                .thenReturn(Arrays.asList(infos.get(0)));

        when(dictionaryInfoService.insert(anyList())).then(invocationOnMock -> {
            System.out.println(invocationOnMock.getArgumentAt(0, List.class));
            return null;
        });

        when(dictionaryInfoService.delete(anyString(),anyString(),anyString())).then(invocationOnMock -> {
            System.out.println(Arrays.toString(invocationOnMock.getArguments()));
            return null;
        });
    }

    @Test
    public void test() {

        TestBean bean = new TestBean();
        wrapper.wrap("test", bean);
        System.out.println(bean);
        Assert.assertNotNull(bean.getDict());
        Assert.assertEquals(bean.getDict().length, 2);
        wrapper.persistent("test", bean);

        wrapper.wrap("test2", new EmptyDictBean());
        wrapper.persistent("test2", new EmptyDictBean());

    }
}