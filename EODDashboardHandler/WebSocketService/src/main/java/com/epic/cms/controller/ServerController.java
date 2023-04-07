/**
 * Author : lahiru_p
 * Date : 3/13/2023
 * Time : 1:48 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/")
public class ServerController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/message")
    public String testConsumeMessage(@Payload String msg){
        System.out.println(msg);
        return msg;
    }

    @GetMapping("/home")
    public String home(Model model) {
        return "home";
    }
}
