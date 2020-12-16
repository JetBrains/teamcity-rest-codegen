[![JetBrains incubator project](https://jb.gg/badges/incubator-plastic.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
# TeamCity Swagger Codegen

An implementation of swagger-codegen generator for TeamCity REST API Kotlin/Python clients, as well as REST API autodoc.

# Usage

Pull a swagger-codegen-cli.jar from https://github.com/swagger-api/swagger-codegen and put it to the scripts folder (tested with 2.4.9 version). 
Update the corresponding script in scripts folder to: 

* point to the correct TeamCity installation
* set the output folder as required
* update the swagger-codegen-cli.jar name if necessary

and execute. The generator heavily relies on the internal REST API decorations which are included with 2020.2.1 release of TeamCity.
