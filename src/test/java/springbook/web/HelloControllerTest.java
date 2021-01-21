package springbook.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:WEB-INF/spring-servlet.xml")
@WebAppConfiguration
public class HelloControllerTest {

    @Autowired
    private HelloSpring helloSpring;
    @Autowired
    private HelloController helloController;

    private MockMvc mockMvc;

    @Before
    public void mockSetUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(helloController)
            .setViewResolvers(getViewResolver())
            .build();
    }

    protected ViewResolver getViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/view");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Test
    public void mockMvcTest() throws Exception {
        mockMvc.perform(get("/hello")
            .param("name", "youzheng")
//            .param("type", "admin")
        )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void flashMapObjTest() {
        FlashMap flashMap = new FlashMap();
        flashMap.put("message", "Hi");
        flashMap.setTargetRequestPath("/hello/result");
        flashMap.startExpirationPeriod(10);

    }

}