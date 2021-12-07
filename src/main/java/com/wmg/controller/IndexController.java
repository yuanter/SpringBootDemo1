package com.wmg.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
    /**
     * 需求：返回首页
     */
    @RequestMapping({"", "/", "index"})
    public String index(){
        return "index";
    }

}
