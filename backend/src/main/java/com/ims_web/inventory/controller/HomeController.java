package com.ims_web.inventory.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
                <h1> Inventario APP</h1>
                <p> Backend funcionando</p>
                """;
    }
}