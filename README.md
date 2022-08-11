# 1. Add micro-limits-service:  Centratized configuration Client #
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
  
# 2. Add micro-config-server: Centratized configuration Server #
  * server.port=8888
  1.  add dependency config Server (spring cloud config)
  2. add name server in application.properties:
  spring.application.name=spring-cloud-config-servers
  3. Connect Spring Cloud Config Server to Local Git Repository:
     * create folder git-localconfig-repo
     * add spring.cloud.config.server.git.uri=file:///home/meher/j2eews/microservices-project/git-localconfig-repo in application.properties
     * create  file limits-service.properties in "git-localconfig-repo" folder
     * To show limits-service.properties content: http://localhost:8888/limits-service/default
 
# 3. Add micro-currency-exchange-service:  Centratized configuration Client #  
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
   
  
 
