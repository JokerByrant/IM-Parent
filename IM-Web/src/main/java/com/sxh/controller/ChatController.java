package com.sxh.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.net.UnknownHostException;

/**
 * @author sxh
 * @date 2022/2/18
 */
@RestController
@Slf4j
public class ChatController {
    @Value("${netty.host}")
    private String host;
    @Value("${netty.port}")
    private String port;

    /**
     * 前端以websocket方式连接
     * @return
     */
    @GetMapping("/index")
    public ModelAndView index() throws UnknownHostException {
        log.info("初始化聊天页面...");
        ModelAndView mv = new ModelAndView("/index");
        JSONObject imData = new JSONObject();
        imData.put("server", host + ":" + port);
        mv.addObject("imData", imData);
        return mv;
    }
}
