package com.shoplite.product;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloPageController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";            // treated as a view name, not as the response text
    }
}