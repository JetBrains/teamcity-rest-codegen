package com.jetbrains.codegen.kotlin;

import io.swagger.codegen.*;
import io.swagger.codegen.languages.KotlinClientCodegen;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

public class TeamCityKotlinCodegen extends KotlinClientCodegen implements CodegenConfig {
    String X_SUBPACKAGE = "x-subpackage";

    public TeamCityKotlinCodegen() {
        super();

        String sourceFolder = "src/main/kotlin";

        embeddedTemplateDir = templateDir = "teamcity_kotlin";
        artifactId = "teamcity-kotlin-rest-client";
        packageName = "org.jetbrains.teamcity.rest";
        apiPackage = packageName + ".apis";
        modelPackage = packageName + ".models";

        languageSpecificPrimitives = new HashSet<String>(Arrays.asList(
                "Byte",
                "Short",
                "Int",
                "Long",
                "Float",
                "Double",
                "Boolean",
                "Char",
                "String",
                "Array",
                "List",
                "Map",
                "Set"
        ));

        defaultIncludes = new HashSet<String>(Arrays.asList(
                "Byte",
                "Short",
                "Int",
                "Long",
                "Float",
                "Double",
                "Boolean",
                "Char",
                "Array",
                "List",
                "Set",
                "Map"
        ));

        typeMapping = new HashMap<>();
        typeMapping.put("string", "String");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("integer", "Int");
        typeMapping.put("float", "Float");
        typeMapping.put("long", "Long");
        typeMapping.put("double", "Double");
        typeMapping.put("number", "java.math.BigDecimal");
        typeMapping.put("date-time", "LocalDateTime");
        typeMapping.put("date", "LocalDateTime");
        typeMapping.put("array", "List");
        typeMapping.put("list", "List");
        typeMapping.put("map", "Map");
        typeMapping.put("object", "Any");
        typeMapping.put("binary", "Array<Byte>");
        typeMapping.put("Date", "LocalDateTime");
        typeMapping.put("DateTime", "LocalDateTime");

        reservedWords.remove("data"); // for the Datas class which includes param named "data"
    }

    @Override
    public String getName() {
        return "teamcity-kotlin";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.clear();
        supportingFiles.add(new SupportingFile("build.gradle.mustache", "", "build.gradle"));
        supportingFiles.add(new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));

        final String infrastructureFolder = (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", "/");
        final String baseFolder = (sourceFolder + File.separator + packageName + File.separator + "base").replace(".", "/");

        supportingFiles.add(new SupportingFile("infrastructure/ApiClient.kt.mustache", infrastructureFolder, "ApiClient.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ApiAbstractions.kt.mustache", infrastructureFolder, "ApiAbstractions.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ApiInfrastructureResponse.kt.mustache", infrastructureFolder, "ApiInfrastructureResponse.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ApplicationDelegates.kt.mustache", infrastructureFolder, "ApplicationDelegates.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/RequestConfig.kt.mustache", infrastructureFolder, "RequestConfig.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/RequestMethod.kt.mustache", infrastructureFolder, "RequestMethod.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ResponseExtensions.kt.mustache", infrastructureFolder, "ResponseExtensions.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/Serializer.kt.mustache", infrastructureFolder, "Serializer.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/Errors.kt.mustache", infrastructureFolder, "Errors.kt"));

        supportingFiles.add(new SupportingFile("infrastructure/ClientConfig.kt.mustache", infrastructureFolder, "ClientConfig.kt"));

        supportingFiles.add(new SupportingFile("base/DataEntity.kt.mustache", baseFolder, "DataEntity.kt"));
        supportingFiles.add(new SupportingFile("base/ListEntity.kt.mustache", baseFolder, "ListEntity.kt"));
        supportingFiles.add(new SupportingFile("base/LocatorEntity.kt.mustache", baseFolder, "LocatorEntity.kt"));
        supportingFiles.add(new SupportingFile("base/PaginatedEntity.kt.mustache", baseFolder, "PaginatedEntity.kt"));
    }

    @Override
    public String sanitizeName(String name) {
        // NOTE: performance wise, we should have written with 2 replaceAll to replace desired
        // character with _ or empty character. Below aims to spell out different cases we've
        // encountered so far and hopefully make it easier for others to add more special
        // cases in the future.

        // better error handling when map/array type is invalid
        if (name == null) {
            return "ERROR_UNKNOWN";
        }

        // if the name is just '$', map it to 'value' for the time being.
        if ("$".equals(name)) {
            return "value";
        }

        // input[] => input
        name = name.replaceAll("\\[\\]", ""); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // input[a][b] => input_a_b
        name = name.replaceAll("\\[", "_");
        name = name.replaceAll("\\]", "");

        // input(a)(b) => input_a_b
        name = name.replaceAll("\\(", "_");
        name = name.replaceAll("\\)", "");

        // input-name => input_name
        name = name.replaceAll("-", "_");

        // input name and age => input_name_and_age
        name = name.replaceAll(" ", "_");

        return name;
    }


    @Override
    public String toModelName(String name) {
        name = sanitizeName(name); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.
        // remove dollar sign
        name = name.replaceAll("\\$", "");

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            name = "model_" + name; // e.g. return => ModelReturn (after camelize)
        }

        // model name starts with number
        if (name.matches("^\\d.*")) {
            name = "model_" + name; // e.g. 200Response => Model200Response (after camelize)
        }

        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name);
    }

    @Override
    public String getSwaggerType(Property p) {
        String swaggerType = super.getSwaggerType(p);
        // This maps, for example, long -> kotlin.Long based on hashes in this type's constructor
        return typeMapping.getOrDefault(swaggerType, swaggerType);
    }

    public CodegenProperty fromProperty(String name, Property p) {
        CodegenProperty property = super.fromProperty(name, p);
        property.name = property.name.replace(".", "|");
        property.nameInCamelCase = camelize(property.name.replace("$", ""), true);
        property.name = property.name.replace("|", ".");
        property.baseName = property.baseName.replace("$", "\\$");

        return property;
    }
}