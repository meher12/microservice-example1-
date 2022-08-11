# Microservices and RESTful web services with Spring cloud and Spring Boot: #

## 1. Add micro-limits-service:  Centratized configuration Client 
  * server.port=8181
  1.  add dependency config client (spring cloud config)
  2. create rest controller
  3. add limits-service.minimum=1 and limits-service.maximum=998
  4. add @ConfigurationProperties("limits-service") in Configuration, "limits-service" is the name of service 
  5. Connect Limits Service to Spring Cloud Config Server :
     * spring.config.import=optional:configserver:http://localhost:8888 and spring.application.name==limits-service
     * put url http://localhost:8888/limits-service/default
  6. Configuring Profiles for Limits Service:
     * spring.profiles.active=dev AND spring.cloud.config.profile=dev,  run url : http://localhost:8181/limits to get minumun and maximum values of limits-service-dev.properties in git-localconfig-repo switch with Config server
  
## 2. Add micro-config-server: Centratized configuration Server ##
  * server.port=8888
  1.  add dependency config Server (spring cloud config)
  2. add name server in application.properties:
  spring.application.name=spring-cloud-config-servers
  3. Connect Spring Cloud Config Server to Local Git Repository:
     * create folder git-localconfig-repo
     * add spring.cloud.config.server.git.uri=file:///home/meher/j2eews/microservices-project/git-localconfig-repo in application.properties
     * create  file limits-service.properties in "git-localconfig-repo" folder
     * To show limits-service.properties content: http://localhost:8888/limits-service/default
 
## 3. Add micro-currency-exchange-service:  Centratized configuration Client ##  
  * server.port=8000
  1. add dependency config client (spring cloud config)  
  2. create a rest controller CurrencyExchangeController
  3. Setting up Dynamic Port in the the Response by add private Environment environment to add property "environment.getProperty("local.server.port")" from package "org.springframework.core.env.Environment"
  4. create a new instance of Currency Exchange Service by:
     * Duplicate the default service and rename it
     * Run as > run configurations > duplicate > Arguments > VM arguments: -Dserver.port=8001
     * Add url in browser :http://localhost:8001/currency-exchange-service/from/USD/to/INR
  5. Configure JPA and Initialized Data:
     * If you are Spring Boot >=2.5.0, You would need to configure this in application.properties spring.jpa.defer-datasource-initialization=true to insert data in data.sql
     * Create a JPA Repository
     
## 4. Add micro-conversion-exchange-service:  Centratized configuration Client ##  
  * server.port=8100
  1. Setting up Currency Conversion Microservice (add dependency config client (spring cloud config) )
  2. Creating a service for currency conversion (bean, controller), URL: http://localhost:8100/currency-conversion-service/from/USD/to/INR/quantity/10
  3. Invoking Currency Exchange from Currency Conversion Microservice:
     - NB: RestTemplate: is a Spring framework that allows communication between a client and a REST server with HTTP requests.
     * add RestTemplate in "calculateCurrencyConversion(...)" method:
       ```  
          Map<String, String> uriVariables = new HashMap<>();
          uriVariables.put("from", from);
          uriVariables.put("to", to);
          ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity(
                "http://localhost:8000/currency-exchange-service/from/{from}/to/{to}", CurrencyConversion.class,
                uriVariables);
          CurrencyConversion currencyConversion = responseEntity.getBody();
       ```   
      - url for rest template: http://localhost:8100/currency-conversion-service/from/USD/to/INR/quantity/10
  4. Using Feign REST Client for Service Invocation:   
     * /currency-conversion-service/pom.xml add new dependency:
		``` 
		 <dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-openfeign</artifactId>
		 </dependency>  
		 ```

     * in MicroConversionExchangeServiceApplication class add the annotation @EnableFeignClients
     * create CurrencyExchangeProxy classand add annotation @FeignClient(name="currency-exchange-service", url="localhost:8000")

         ```
          @GetMapping("/currency-exchange/from/{from}/to/{to}")
	       public CurrencyConversion retrieveExchangeValue(@PathVariable("from") String from, @PathVariable("to") String to);
	      ```
       
     * CurrencyConversionController.java Modified to:
     
      ```
        @Autowired
	     private CurrencyExchangeProxy proxy;
	
        @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
        public CurrencyConversion calculateCurrencyConversionFeign(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
            ) {
               
         CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);
         
         return new CurrencyConversion(currencyConversion.getId(), 
               from, to, quantity, 
               currencyConversion.getConversionMultiple(), 
               quantity.multiply(currencyConversion.getConversionMultiple()), 
               currencyConversion.getEnvironment() + " " + "feign");
         
         }
      ``` 
  
      * Url for feign: http://localhost:8100/currency-conversion-feign/from/USD/to/INR/quantity/10
   - WITH feign IS VERY ESAY TO USE A REST CLEINT THAN THE restTemplate

## 5. Eureka Understand Naming Server and Setting up Eureka Naming Server 
      - In the past example we had hardcoded the port in the currency-exchange-service-2 while setting the Feign:

      ```
         @FeignClient(name="currency-exchange-service", url="localhost:8000")

      ```
      - If there are multiple instances of currency-exchange-service then we will not be able to call other instances. To solve this issue, we will go with Naming Server or Service Registry, where all the instances of the microservices will be registered. So this Naming Server will also be responsible for Load Balancing at server side.
      - So create a new spring boot application from Spring Initializr. We will call it – naming-server. Add below dependency to it: Eureka Server
      - In pom.xml we can see these:

      ```
         <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
         </dependency>
      ``` 