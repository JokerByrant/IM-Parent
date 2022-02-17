package com.sxh.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录Controller
 * @author sxh
 * @date 2022/2/16
 */
@RestController
public class LoginController {
    @GetMapping("/login")
    public ModelAndView loginPage(String error){
        //前往登录页面
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("error", error);
        return modelAndView;
    }
}
