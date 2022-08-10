# 1. Add micro-limits-service:  Centratized configuration Client#
  * server.port=8181
  1.  add dependency config client (spring cloud config)
  2. create rest controller
  3. add limits-service.minimum=1 and limits-service.maximum=998
  4. add @ConfigurationProperties("limits-service") in Configuration, "limits-service" is the name of service 
  
# 2. Add micro-config-server: Centratized configuration Server#
  * server.port=8888
  1.  add dependency config Server (spring cloud config)
  2. add name server in application.properties:
  spring.application.name=spring-cloud-config-servers
  3. create folder git-localconfig-repo:
     * create  file limits-service.properties in "git-localconfig-repo" folder
     * To show limits-service.properties content: http://localhost:8888/limits-service/default
 
