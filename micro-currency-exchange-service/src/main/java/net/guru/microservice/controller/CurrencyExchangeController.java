package net.guru.microservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import net.guru.microservice.bean.CurrencyExchange;
import net.guru.microservice.repository.CurrencyExchangeRepository;

@RestController
public class CurrencyExchangeController {

    @Autowired
    private CurrencyExchangeRepository currencyExchangeRepository;
    
    @Autowired
    private Environment environment;
    
    private Logger logger = LoggerFactory.getLogger(CurrencyExchangeController.class);
    
    @GetMapping("/currency-exchange-service/from/{from}/to/{to}")
    public CurrencyExchange retrieveExchangeValue(@PathVariable String from, @PathVariable String to){

//        CurrencyExchange currencyExchange = new CurrencyExchange(1000L, from, to, BigDecimal.valueOf(50));
        logger.info("retrieveExchangeValue called with {} to {}", from, to);       
        CurrencyExchange currencyExchange = currencyExchangeRepository.findByFromAndTo(from, to)
                .orElseThrow(() -> new RuntimeException("Unable to find data for: "+ from +" to " + to));
        
        
        String port = environment.getProperty("local.server.port");
        currencyExchange.setEnvironment(port);
        return currencyExchange;
    }

}
