package springbook.web;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloController {

    @Autowired
    private HelloSpring helloSpring;

    @RequestMapping(value = "/hello")
    public ModelAndView helloSpring(@RequestParam("name")String name) {

        String message = this.helloSpring.sayHello(name);
        Map<String,String> map = new HashMap<String, String>();
        map.put("message", message);
//        return new ModelAndView("/WEB-INF/view/hello",map);
        return new ModelAndView("hello",map);
    }

    @RequestMapping(value = "/hello",params = "type=user")
    public ModelAndView helloSpring1(@RequestParam("type")String type,@RequestParam("name")String name) {

        String message = this.helloSpring.sayHello(name);
        Map<String,String> map = new HashMap<String, String>();
        map.put("message", message + type);

        return new ModelAndView("hello",map);
    }

    @RequestMapping(value = "/hello",params = "type=admin")
    public ModelAndView helloSpring2(@RequestParam("type")String type,@RequestParam("name")String name) {

        String message = this.helloSpring.sayHello(name);
        Map<String,String> map = new HashMap<String, String>();
        map.put("message", message + type);

        return new ModelAndView("hello",map);
    }
}
