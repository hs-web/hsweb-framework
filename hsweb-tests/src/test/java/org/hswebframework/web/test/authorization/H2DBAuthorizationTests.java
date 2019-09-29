package org.hswebframework.web.test.authorization;

import org.hswebframework.web.test.TestApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class H2DBAuthorizationTests  extends CommonAuthorizationTests{

}
