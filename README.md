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
     * spring.profiles.active=dev AND spring.cloud.config.profile=dev,  run url : http://localhost:8181/limits to get minumun and maximum values of limits-service-dev.properties in git-localconfig-repo switch with Config server:
       - Url: \
            http://localhost:8888/limits-service/default \
            http://localhost:8888/limits-service/qa \
            http://localhost:8888/limits-service/dev
       - /limits-service/src/main/resources/application.properties Modified:
            ```
            spring.profiles.active=dev
            spring.cloud.config.profile=dev

            spring.application.name=limits-service
            spring.config.import=optional:configserver:http://localhost:8888

            limits-service.minimum=3
            limits-service.maximum=997
            ```

       - /git-localconfig-repo/limits-service-dev.properties New
            ```
            limits-service.minimum=4
            limits-service.maximum=996
            ```
       - /git-localconfig-repo/limits-service-qa.properties New
            ```
            limits-service.minimum=6
            limits-service.maximum=993
            ```
  
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
     
      ```http://localhost:8100/currency-conversion-feign/from/USD/to/INR/quantity/10ionFeign(
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

## 5. Eureka  ##
   1. Understand Naming Server(Service Registry) and Setting up Eureka Naming Server
   * server.port=8761
      - In the past example we had hardcoded the port in the currency-exchange-service-2 while setting the Feign:

         ```
            @FeignClient(name="currency-exchange-service", url="localhost:8000")
         ```
      - If there are multiple instances of currency-exchange-service then we will not be able to call other instances. To solve this issue, we will go with Naming Server or Service Registry, where all the instances of the microservices will be registered. So this Naming Server will also be responsible for Load Balancing at server side.
      - So create a new spring boot application from Spring Initializr. We will call it – naming-server. Add below dependency to it: Eureka Server (Spring Cloud Discovery)
      - In pom.xml we can see these:
         ```
            <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
            </dependency>
         ``` 
      - Add @EnableEurekaServer to the main class.
      - In application.properties:

         ```
            spring.application.name=naming-server
            server.port=8761
            #No spring.config.import property has been defined
            spring.config.import=optional:configserver:

            #Dont'n need this registry server
            eureka.client.register-with-eureka=false
            eureka.client.fetch-registry=false
         ```
      - start the application and hit below url, you will see the UI console of Naming Server: http://localhost:8761/
   2. Connect Currency Conversion & Currency Exchange Microservices to Eureka: 
      - In /currency-conversion-service/src/main/resources/application.properties add new Line
         ```
          eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka
         ```
      - /currency-conversion-service/pom.xml add new dependency

         ```
            <dependency>
                  <groupId>org.springframework.cloud</groupId>
                  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            </dependency>
         ```
      - /currency-exchange-service/pom.xml add new dependency

         ```
            <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            </dependency>
         ```
      - /currency-exchange-service/src/main/resources/application.properties add new Line
      ```
       eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka
      ```
   3. Load Balancing with Eureka, Feign & Spring Cloud LoadBalancer:
      - Just update CurrencyExchangeProxy.java interface in currency-conversion-service service:
      from
      ```
        @FeignClient(name="currency-exchange-service", url="localhost:8000")
      ```
      to
      ```
        @FeignClient(name="currency-exchange-service")
      ```
      - Run currency-exchange-service in port 8000 and 8001
      - => LoadBalancer work fine url to check (refresh the browser to see the server port of currency-exchange-service is changed each time): http://localhost:8100/currency-conversion-feign/from/USD/to/INR/quantity/10
   4. Setting up Spring Cloud API Gateway :
      * Create project api-gateway with those dependency : Config Client (Spring Cloud Config), Eureka Discovery Client (Spring Cloud Discovery), Gateway (Spring Cloud Routing)
        - Add in application.properties file:
         ```
         spring.cloud.config.enabled=false
         spring.application.name=api-gateway
         server.port=8765

         #Connect api-gateway to Eureka Server
         eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka
         ```
   5. Enabling Discovery Locator with Eureka for Spring Cloud Gateway:
      * Setup those urls: \
        http://localhost:8765/CURRENCY-EXCHANGE-SERVICE/currency-exchange-service/from/USD/to/INR  \
        http://localhost:8765/CURRENCY-CONVERSION-SERVICE/currency-conversion-service/from/USD/to/INR/quantity/10  \
        http://localhost:8765/CURRENCY-CONVERSION-SERVICE/currency-conversion-feign/from/USD/to/INR/quantity/10 
      * in application.properties file:  
      ```
       #DiscoveryClient Route Definition Locator:
       #The Gateway can be configured to create routes based on services registered with a DiscoveryClient compatible service registry.
       #To enable this, set
       spring.cloud.gateway.discovery.locator.enabled=true
      ```
      #Make sure a DiscoveryClient is in pom.xml
      ```
       <dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		 </dependency>
      ```
      Change service-id to lowerCase: 
      ```
       spring.cloud.gateway.discovery.locator.lower-case-service-id=true
      ```
      Lower Case URL: \
      http://localhost:8765/currency-exchange-service/currency-exchange-service/from/USD/to/INR \
      http://localhost:8765/currency-conversion-service/currency-conversion-service/from/USD/to/INR/quantity/10 \
      http://localhost:8765/currency-conversion-service/currency-conversion-feign/from/USD/to/INR/quantity/10

   6. Exploring (building a custom route) Routes with Spring Cloud Gateway:
      * Add a config class : ApiGatewayConfiguration in api-gateway project
      ```
         @Configuration
         public class ApiGatewayConfiguration {

            @Bean
            public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
               return builder.routes().route(p -> p.path("/get") // http://localhost:8765/get
                        .filters(f -> f.addRequestHeader("MyHeader", "MyURI").addRequestParameter("Param", "MyValue"))
                        .uri("http://httpbin.org:80"))
                        .route(p -> p.path("/currency-exchange-service/**").uri("lb://currency-exchange-service"))
                        .route(p -> p.path("/currency-conversion-service/**").uri("lb://currency-conversion-service"))
                        .route(p -> p.path("/currency-conversion-feign/**").uri("lb://currency-conversion-service"))
                        .route(p -> p.path("/currency-conversion-new/**")
                                       .filters(f -> f.rewritePath("/currency-conversion-new/(?<segment>.*)",
                                                "/currency-conversion-feign/${segment}"))
                                       .uri("lb://currency-conversion-service"))
                        .build();
            }
         }
      ```
      * /api-gateway/src/main/resources/application.properties Commented :

        ```
         #spring.cloud.gateway.discovery.locator.enabled=true
         #spring.cloud.gateway.discovery.locator.lowerCaseServiceId=true
        ```
      * Custom Routes:  \
      http://localhost:8765/currency-exchange-service/from/USD/to/INR  \
      http://localhost:8765/currency-conversion-service/from/USD/to/INR/quantity/10  \
      http://localhost:8765/currency-conversion-feign/from/USD/to/INR/quantity/10  \
      http://localhost:8765/currency-conversion-new/from/USD/to/INR/quantity/10 
      
   7. Implementing Spring Cloud Gateway Logging Filter:
      * Create class LoggingFilter.java in logger package:
      ```
         import org.slf4j.Logger;
         import org.slf4j.LoggerFactory;
         import org.springframework.cloud.gateway.filter.GatewayFilterChain;
         import org.springframework.cloud.gateway.filter.GlobalFilter;
         import org.springframework.stereotype.Component;
         import org.springframework.web.server.ServerWebExchange;

         import reactor.core.publisher.Mono;

         @Component
         public class LoggingFilter implements GlobalFilter {

            private Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
               logger.info("Path of the request received -> {}", exchange.getRequest().getPath());
               return chain.filter(exchange);
            }

         }
      ```
      In Console : n.g.microservice.logger.LoggingFilter    : Path of the request received -> /currency-exchange-service/from/USD/to/INR
## 6. Resilience4j ##
   1. Getting started with Circuit Breaker - Resilience4j
      * In micro-currency-exchange-service project add new dependencies:
         ```
            <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-aop</artifactId>
            </dependency>

            <dependency>
               <groupId>io.github.resilience4j</groupId>
               <artifactId>resilience4j-spring-boot2</artifactId>
            </dependency>
         ```
      * Add new CircuitBreakerController class:
         ```
            @GetMapping("/sample-api")
            public String sampleApi() {
            return "Sample Api";
            }
         ```
   2. Playing with Resilience4j - Retry and Fallback Methods:
      - Can we return a fallback response if a service is down ?
      * CircuitBreakerController class:
        
         ```
            private Logger logger = LoggerFactory.getLogger(CircuitBreakerController.class);

            @GetMapping("/sample-api")
            @Retry(name="sample-api", fallbackMethod = "hardcodedResponse")
            public String sampleApi() {
               logger.info("Sample api call received");
               ResponseEntity<String> forEntity = new RestTemplate().getForEntity("http://localhost:8080/some-dummy-url",
                        String.class);
               return forEntity.getBody();
               
            }
            public String hardcodedResponse(Exception ex) {
               return "fallback-response";
            }
         ```
         In application.properties: 
         ```
            resilience4j.retry.instances.sample-api.max-attempts=5
            resilience4j.retry.instances.sample-api.waitDuration=1s
            resilience4j.retry.instances.sample-api.enableExponentialBackoff=true
         ```
         In console we see 5 attempts: 
           ```
            INFO 19676 --- [nio-8000-exec-1] n.g.m.c.CircuitBreakerController         : Sample api call received
            INFO 19676 --- [nio-8000-exec-1] n.g.m.c.CircuitBreakerController         : Sample api call received
            INFO 19676 --- [nio-8000-exec-1] n.g.m.c.CircuitBreakerController         : Sample api call received
            INFO 19676 --- [nio-8000-exec-1] n.g.m.c.CircuitBreakerController         : Sample api call received
            INFO 19676 --- [nio-8000-exec-1] n.g.m.c.CircuitBreakerController         : Sample api call received
         ```
         With "fallbackMethod" In browser (http://localhost:8000/sample-api) we see: "fallback-response" message instead of the default message error.

   3. Playing with Circuit Breaker Features of Resilience4j: 
      In terminal:
      ```
         curl http://localhost:8000/sample-api
         watch curl http://localhost:8000/sample-api
         watch -n 0.1 curl http://localhost:8000/sample-api
      ```
       CircuitBreakerController class:
       ```
        //@Retry(name = "sample-api", fallbackMethod = "hardcodedResponse")
          @CircuitBreaker(name = "default", fallbackMethod = "hardcodedResponse")
       ```
       - FailureRateThreshold: Configures the failure rate threshold in percentage. \ When the failure rate is equal or greater than the threshold the CircuitBreaker transitions to open and starts short-circuiting calls. \
       - In application.properties file: 
       ```
        resilience4j.circuitbreaker.instances.default.failure-rate-threshold=90
       ``` 
   4. Exploring Rate Limiting and BulkHead Features of Resilience4j
      * *** Rate limiting is an imperative technique to prepare your API for scale and establish high availability and reliability of your service ***
      * @RateLimiter(name="default") 
      ```
         #The number of permissions available during one limit refresh period
         #For example, you want to restrict the calling rate of some methods to be not higher than 2 req/ms.
         resilience4j.ratelimiter.instances.default.limit-for-period=2 

         #The period of a limit refresh. After each period the rate limiter sets its permissions count back to the limitForPeriod value
         resilience4j.ratelimiter.instances.default.limit-refresh-period=10s
      ```
      * In terminal:
      ```
         curl http://localhost:8000/sample-api
         watch curl http://localhost:8000/sample-api
         watch -n 0.1 curl http://localhost:8000/sample-api
      ```
      * In browser we can see the result after two refresh we get a error because we configure the Rate limiting 'limit-for-period=2 ' and 'limit-for-period=2 '

