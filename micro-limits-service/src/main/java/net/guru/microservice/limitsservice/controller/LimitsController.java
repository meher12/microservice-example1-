package net.guru.microservice.limitsservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import net.guru.microservice.limitsservice.bean.Limits;
import net.guru.microservice.limitsservice.configuration.Configuration;

@RestController
public class LimitsController {

    @Autowired
    Configuration configuration;
    
    @GetMapping("/limits")
    public Limits retrieveLimits() {
        return new Limits(configuration.getMinimum(), configuration.getMaximum());
    }
}
