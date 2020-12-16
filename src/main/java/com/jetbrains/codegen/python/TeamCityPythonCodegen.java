package com.jetbrains.codegen.python;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.PythonClientCodegen;

import java.io.File;

public class TeamCityPythonCodegen extends PythonClientCodegen implements CodegenConfig {
    protected String basePackage;

    public TeamCityPythonCodegen() {
        super();

        embeddedTemplateDir = templateDir = "teamcity_python";
    }

    @Override
    public String getName() {
        return "teamcity-python";
    }

    @Override
    public void processOpts() {
        if (additionalProperties.containsKey(CodegenConstants.TEMPLATE_DIR)) {
            this.setTemplateDir((String) additionalProperties.get(CodegenConstants.TEMPLATE_DIR));
        }

        if (additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE)) {
            this.setModelPackage((String) additionalProperties.get(CodegenConstants.MODEL_PACKAGE));
        }

        if (additionalProperties.containsKey(CodegenConstants.API_PACKAGE)) {
            this.setApiPackage((String) additionalProperties.get(CodegenConstants.API_PACKAGE));
        }

        if (additionalProperties.containsKey(CodegenConstants.HIDE_GENERATION_TIMESTAMP)) {
            setHideGenerationTimestamp(convertPropertyToBooleanAndWriteBack(CodegenConstants.HIDE_GENERATION_TIMESTAMP));
        } else {
            additionalProperties.put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, hideGenerationTimestamp);
        }

        if (additionalProperties.containsKey(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG)) {
            this.setSortParamsByRequiredFlag(Boolean.valueOf(additionalProperties
                    .get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG).toString()));
        }

        if (additionalProperties.containsKey(CodegenConstants.ENSURE_UNIQUE_PARAMS)) {
            this.setEnsureUniqueParams(Boolean.valueOf(additionalProperties
                    .get(CodegenConstants.ENSURE_UNIQUE_PARAMS).toString()));
        }

        if (additionalProperties.containsKey(CodegenConstants.ALLOW_UNICODE_IDENTIFIERS)) {
            this.setAllowUnicodeIdentifiers(Boolean.valueOf(additionalProperties
                    .get(CodegenConstants.ALLOW_UNICODE_IDENTIFIERS).toString()));
        }

        if(additionalProperties.containsKey(CodegenConstants.MODEL_NAME_PREFIX)){
            this.setModelNamePrefix((String) additionalProperties.get(CodegenConstants.MODEL_NAME_PREFIX));
        }

        if(additionalProperties.containsKey(CodegenConstants.MODEL_NAME_SUFFIX)){
            this.setModelNameSuffix((String) additionalProperties.get(CodegenConstants.MODEL_NAME_SUFFIX));
        }

        if (additionalProperties.containsKey(CodegenConstants.REMOVE_OPERATION_ID_PREFIX)) {
            this.setRemoveOperationIdPrefix(Boolean.parseBoolean(additionalProperties
                    .get(CodegenConstants.REMOVE_OPERATION_ID_PREFIX).toString()));
        }

        if (this.additionalProperties.containsKey("packageName")) {
            this.setPackageName((String)this.additionalProperties.get("packageName"));
        } else {
            this.setPackageName("teamcity_python");
        }

        if (this.additionalProperties.containsKey("projectName")) {
            this.setProjectName((String)this.additionalProperties.get("projectName"));
        } else {
            this.setProjectName(this.packageName.replaceAll("_", "-"));
        }

        if (this.additionalProperties.containsKey("packageVersion")) {
            this.setPackageVersion((String)this.additionalProperties.get("packageVersion"));
        } else {
            this.setPackageVersion("1.0.0");
        }

        this.additionalProperties.put(CodegenConstants.EXCLUDE_TESTS, true);

        this.additionalProperties.put("projectName", this.projectName);
        this.additionalProperties.put("packageName", this.packageName);
        this.additionalProperties.put("packageVersion", this.packageVersion);
        if (this.additionalProperties.containsKey("packageUrl")) {
            this.setPackageUrl((String)this.additionalProperties.get("packageUrl"));
        }

        this.supportingFiles.add(new SupportingFile("__init__model.mustache", this.packageName + File.separatorChar + this.modelPackage, "__init__.py"));
        this.supportingFiles.add(new SupportingFile("__init__api.mustache", this.packageName + File.separatorChar + this.apiPackage, "__init__.py"));

        this.modelPackage = this.packageName + "." + this.modelPackage;
        this.apiPackage = this.packageName + "." + this.apiPackage;

        this.basePackage = this.packageName + File.separatorChar + "base";
        this.supportingFiles.add(new SupportingFile("__init__base.mustache", this.basePackage, "__init__.py"));
        this.supportingFiles.add(new SupportingFile("dataEntity.mustache", this.basePackage, "dataEntity.py"));
        this.supportingFiles.add(new SupportingFile("listEntity.mustache", this.basePackage, "listEntity.py"));
        this.supportingFiles.add(new SupportingFile("locatorEntity.mustache", this.basePackage, "locatorEntity.py"));
        this.supportingFiles.add(new SupportingFile("paginatedEntity.mustache", this.basePackage, "paginatedEntity.py"));

        this.supportingFiles.add(new SupportingFile("const.mustache", this.packageName, "const.py"));
        this.supportingFiles.add(new SupportingFile("content_handler.mustache", this.packageName, "content_handler.py"));
        this.supportingFiles.add(new SupportingFile("request_handler.mustache", this.packageName, "request_handler.py"));
        this.supportingFiles.add(new SupportingFile("setup.mustache", ".", "setup.py"));

        additionalProperties.put("basePackage", this.packageName + "." + "base");
    }
}