package com.jetbrains.codegen.docs;

import io.swagger.codegen.*;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import org.apache.commons.lang3.StringUtils;
import sun.reflect.generics.tree.Tree;

import java.io.File;
import java.util.*;

public class TeamCityDocsCodegen extends DefaultCodegen implements CodegenConfig {
    protected String invokerPackage = "io.swagger.client";
    protected String groupId = "io.swagger";
    protected String artifactId = "swagger-client";
    protected String artifactVersion = "1.0.0";
    protected String sourceFolder = "topics";

    protected HashSet<String> primitiveTypes = new HashSet<String>(Arrays.asList(
            "byte",
            "short",
            "integer",
            "long",
            "float",
            "double",
            "boolean",
            "char",
            "string",
            "array",
            "list",
            "map",
            "set",
            "any",
            "datetime",
            "file",
            "object"
    ));

    public TeamCityDocsCodegen() {
        super();

        importMapping.clear();
        typeMapping.clear();

        outputFolder = "docs";
        modelTemplateFiles.put("model.mustache", ".md");
        apiTemplateFiles.put("api.mustache", ".md");
        embeddedTemplateDir = templateDir = "teamcity_docs";

        cliOptions.add(new CliOption(CodegenConstants.INVOKER_PACKAGE, CodegenConstants.INVOKER_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.GROUP_ID, CodegenConstants.GROUP_ID_DESC));
        cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_ID, CodegenConstants.ARTIFACT_ID_DESC));
        cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_VERSION, CodegenConstants.ARTIFACT_VERSION_DESC));

        additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
        additionalProperties.put(CodegenConstants.GROUP_ID, groupId);
        additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
        additionalProperties.put(CodegenConstants.ARTIFACT_VERSION, artifactVersion);

        instantiationTypes.put("array", "ArrayList");
        instantiationTypes.put("map", "HashMap");
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.clear();
        supportingFiles.add(
                new SupportingFile("tcr.tree.mustache", "", "tcr.tree")
        );
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public String getName() {
        return "teamcity-docs";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + sourceFolder;
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + sourceFolder;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // just return the original string
        return input;
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // just return the original string
        return input;
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
    public String getTypeDeclaration(Property p) {
        if (p instanceof ArrayProperty) {
            return getArrayTypeDeclaration((ArrayProperty) p);
        }
        return super.getTypeDeclaration(p);
    }

    private String getArrayTypeDeclaration(ArrayProperty arr) {
        String arrayType = "List";
        StringBuilder instantiationType = new StringBuilder(arrayType);
        Property items = arr.getItems();
        String nestedType = StringUtils.capitalize(getTypeDeclaration(items));
        instantiationType.append("<").append(nestedType).append(">");
        return instantiationType.toString();
    }

    @Override
    public String toModelFilename(String name) {
        return camelize(super.toModelFilename(name), true);
    }

    @Override
    public String toApiFilename(String name) {
        return camelize(super.toApiFilename(name), true);
    }

    private String patchWithModelLink(String rawField) {
        String linkPart = camelize(rawField, true);
        String visiblePart = camelize(rawField);
        return String.format("[%s](%s.md)", visiblePart, linkPart);
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);

        //patch operation parameter with a link to <model>.md
        if (op.returnType != null && !this.primitiveTypes.contains(op.returnType.toLowerCase())) {
            op.returnType = patchWithModelLink(op.returnType);
        }
        for (CodegenParameter param : op.allParams) {
            //patch dataType with a link to <model>.md
            if (!this.primitiveTypes.contains(param.dataType)) {
                param.dataType = patchWithModelLink(param.dataType);
            }

            //patch dataType with a link to <model>.md from dataFormat
            else if (param.dataFormat != null && definitions.containsKey(param.dataFormat)) {
                String newDataType = camelize(param.dataFormat, true);
                String capitalizedNewDataType = camelize(param.dataFormat);
                param.dataType = String.format("%s[<%s>](%s.md)", param.dataType, capitalizedNewDataType, newDataType);
            }
        }
        return op;
    }

    @Override
    public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
        CodegenModel m = super.fromModel(name, model, allDefinitions);
        for (CodegenProperty prop : m.vars) {

            //patch ListEntity description with a link to <model>.md
            if (prop.vendorExtensions.get("x-is-first-container-var") != null) {
                String lookupKey = camelize(prop.complexType);
                String replacement = patchWithModelLink(prop.complexType);
                Object description = m.vendorExtensions.get("x-description");
                if (description != null) {
                    String newDescription = description.toString().replace(lookupKey, replacement);
                    m.vendorExtensions.put("x-description", newDescription);
                }
            }

            //patch data type with a link to <model>.md
            if (!prop.isContainer && !this.primitiveTypes.contains(prop.datatype.toLowerCase())) {
                prop.datatype = patchWithModelLink(prop.datatype);
            } else if (prop.isContainer && !this.primitiveTypes.contains(prop.complexType.toLowerCase())) {
                prop.complexType = patchWithModelLink(prop.complexType);
            }

            //patch data format with a link to <model>.md
            if (prop.dataFormat != null && allDefinitions.containsKey(prop.dataFormat)) {
                prop.dataFormat = patchWithModelLink(prop.dataFormat);
            }
        }

        return m;
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        List<Object> models = (List<Object>) objs.get("models");

        HashMap<String, Object> resultModels = new HashMap<>();
        ArrayList<Object> groupedModels = new ArrayList<>();
        ArrayList<Object> ungroupedModels = new ArrayList<>();
        ArrayList<Object> locators = new ArrayList<>();

        TreeMap<String, ArrayList<Object>> groups = new TreeMap<>();

        for (Object model : models) {
            CodegenModel cmodel = (CodegenModel) ((HashMap<String, Object>) model).get("model");
            HashMap<String, Object> modelMap = new HashMap<>();
            modelMap.put("model", cmodel);

            if (!cmodel.vendorExtensions.containsKey("x-subpackage")) {
                ungroupedModels.add(modelMap);
            } else {
                String subpackage = (String) cmodel.vendorExtensions.get("x-subpackage");
                if (subpackage.equals("locator")) {
                    locators.add(modelMap);
                } else {
                    if (!groups.containsKey(subpackage)) {
                        groups.put(subpackage, new ArrayList<>());
                    }
                    groups.get(subpackage).add(modelMap);
                }
            }
        }

        for (Map.Entry<String, ArrayList<Object>> entry : groups.entrySet()) {
            HashMap<String, Object> packageMap = new HashMap<>();
            String capitalizedKey = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
            packageMap.put("subpackage", capitalizedKey);
            packageMap.put("models", entry.getValue());
            groupedModels.add(packageMap);
        }
        resultModels.put("groupedModels", groupedModels);
        resultModels.put("ungroupedModels", ungroupedModels);
        resultModels.put("locators", locators);
        objs.put("models", resultModels);

        return objs;
    }

//    @Override
//    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
//
//        List<String> definitionNames = new ArrayList<>();
//
//        for (Object modelObject : objs.values()) {
//            CodegenModel model = (CodegenModel) modelObject;
//            definitionNames.add(model.classname);
//        }
//
//        for (Map.Entry<String, Object> entry : objs.entrySet()) {
//            String key = entry.getKey();
//            CodegenModel model = (CodegenModel) entry.getValue();
//
//            //patch description with a link to <model>.md
//            if (model.vendorExtensions.containsKey("x-description")) {
//                String description = (String) model.vendorExtensions.get("x-description");
//                for (String modelName : definitionNames) {
//                    if (modelName != null && description.contains(modelName)) {
//                        String newModelName = camelize(modelName, true);
//                        String patchedModelName = String.format("[%s](%s.md)", modelName, newModelName);
//                        description.replace(modelName, patchedModelName);
//                    }
//                }
//                model.vendorExtensions.put("x-description", description);
//                objs.put(key, model);
//            }
//        }
//
//        return objs;
//    }
}