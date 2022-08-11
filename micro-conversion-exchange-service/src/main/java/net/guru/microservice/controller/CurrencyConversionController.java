package net.guru.microservice.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import net.guru.microservice.bean.CurrencyConversion;
import net.guru.microservice.proxy.CurrencyExchangeProxy;

@RestController
public class CurrencyConversionController {

//    @GetMapping("/currency-conversion-service/from/{from}/to/{to}/quantity/{quantity}")
//    public CurrencyConversion calculateCurrencyConversion(@PathVariable String from, @PathVariable String to,
//            @PathVariable BigDecimal quantity) {
//
//        Map<String, String> uriVariables = new HashMap<>();
//        uriVariables.put("from", from);
//        uriVariables.put("to", to);
//
//        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity("http://localhost:8000/currency-exchange-service/from/{from}/to/{to}", CurrencyConversion.class,
//                uriVariables);
//
//        CurrencyConversion currencyConversion = responseEntity.getBody();
//
//        return new CurrencyConversion(currencyConversion.getId(), from, to, quantity,
//                currencyConversion.getConversionMultiple(),
//                quantity.multiply(currencyConversion.getConversionMultiple()), currencyConversion.getEnvironment());
//
//    }

    @Autowired
    private CurrencyExchangeProxy proxy;

    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(@PathVariable String from, @PathVariable String to,
            @PathVariable BigDecimal quantity) {

        CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);

        return new CurrencyConversion(currencyConversion.getId(), from, to, quantity,
                currencyConversion.getConversionMultiple(),
                quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment() + " " + "feign");

    }

}
