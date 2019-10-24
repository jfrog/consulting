package com.jfrog.demogradle.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "Proudly handcrafted by " +
                "<a href='http://wwww.jfrog.com'>Jfrog</a> :)";
    }

}