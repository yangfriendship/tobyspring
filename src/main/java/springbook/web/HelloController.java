package springbook.web;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloController {

    @Autowired
    private HelloSpring helloSpring;

    @RequestMapping("/hello")
    public ModelAndView helloSpring(@RequestParam("name")String name) {

        String message = this.helloSpring.sayHello(name);
        Map<String,String> map = new HashMap<String, String>();
        map.put("message", message);

//        return new ModelAndView("/WEB-INF/view/hello.jsp",map);
        return new ModelAndView("hello",map);
    }
}
