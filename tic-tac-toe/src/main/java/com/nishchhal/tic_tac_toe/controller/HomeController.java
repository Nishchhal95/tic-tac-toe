package com.nishchhal.tic_tac_toe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping
    public String Home(){
        return "index.html";
    }
    
}
