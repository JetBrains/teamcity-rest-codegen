java -cp swagger-codegen-cli-2.4.9.jar;../target/teamcity-client-codegen.jar ^
io.swagger.codegen.SwaggerCodegen generate ^
-l teamcity-docs ^
-i http://localhost/app/rest/swagger.json ^
-o "D:\TC-REST-Docs"