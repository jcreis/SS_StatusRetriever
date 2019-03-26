package mbchallenge.cliapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/hello")
public class HelloController {

    @GetMapping
    private void getThings(){
        System.out.println("hello");
    }

}
