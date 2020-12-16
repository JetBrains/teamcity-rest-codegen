java -cp swagger-codegen-cli-2.4.9.jar;../target/teamcity-client-codegen-1.0.0.jar ^
com.jetbrains.codegen.TeamCityCodegen tc-generate ^
-l teamcity-kotlin ^
-i http://localhost/app/rest/swagger.json ^
-o "D:\TC-Kotlin-Client" ^
-DapiTests=false -DapiDocs=false -DmodelTests=false -DmodelDocs=false ^
-DdebugModels > debug.txt