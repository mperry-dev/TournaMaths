package com.tournamaths.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {
  // This page can be viewed by anyone - see SecurityConfig.java
  @GetMapping("/")
  public String home() {
    return "sign_up";
  }
}
