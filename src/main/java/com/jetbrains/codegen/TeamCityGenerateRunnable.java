package com.jetbrains.codegen;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.cmd.Generate;
import io.swagger.codegen.config.CodegenConfigurator;
import io.swagger.codegen.config.CodegenConfiguratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Command(name = "tc-generate", description = "Generate code with chosen lang")
public class TeamCityGenerateRunnable extends Generate {
    public static final Logger LOG = LoggerFactory.getLogger(Generate.class);
    @Option(
            name = {"-v", "--verbose"},
            description = "verbose mode"
    )
    protected Boolean verbose;
    @Option(
            name = {"-l", "--lang"},
            title = "language",
            required = true,
            description = "client language to generate (maybe class name in classpath, required)"
    )
    protected String lang;
    @Option(
            name = {"-o", "--output"},
            title = "output directory",
            description = "where to write the generated files (current dir by default)"
    )
    protected String output = "";
    @Option(
            name = {"-i", "--input-spec"},
            title = "spec file",
            required = true,
            description = "location of the swagger spec, as URL or file (required)"
    )
    protected String spec;
    @Option(
            name = {"-t", "--template-dir"},
            title = "template directory",
            description = "folder containing the template files"
    )
    protected String templateDir;
    @Option(
            name = {"-a", "--auth"},
            title = "authorization",
            description = "adds authorization headers when fetching the swagger definitions remotely. Pass in a URL-encoded string of name:header with a comma separating multiple values"
    )
    protected String auth;
    @Option(
            name = {"-D"},
            title = "system properties",
            description = "sets specified system properties in the format of name=value,name=value (or multiple options, each with name=value)"
    )
    protected List<String> systemProperties = new ArrayList();
    @Option(
            name = {"-c", "--config"},
            title = "configuration file",
            description = "Path to json configuration file. File content should be in a json format {\"optionKey\":\"optionValue\", \"optionKey1\":\"optionValue1\"...} Supported options can be different for each language. Run config-help -l {lang} command for language specific config options."
    )
    protected String configFile;
    @Option(
            name = {"-s", "--skip-overwrite"},
            title = "skip overwrite",
            description = "specifies if the existing files should be overwritten during the generation."
    )
    protected Boolean skipOverwrite;
    @Option(
            name = {"--api-package"},
            title = "api package",
            description = "package for generated api classes"
    )
    protected String apiPackage;
    @Option(
            name = {"--model-package"},
            title = "model package",
            description = "package for generated models"
    )
    protected String modelPackage;
    @Option(
            name = {"--model-name-prefix"},
            title = "model name prefix",
            description = "Prefix that will be prepended to all model names. Default is the empty string."
    )
    protected String modelNamePrefix;
    @Option(
            name = {"--model-name-suffix"},
            title = "model name suffix",
            description = "Suffix that will be appended to all model names. Default is the empty string."
    )
    protected String modelNameSuffix;
    @Option(
            name = {"--instantiation-types"},
            title = "instantiation types",
            description = "sets instantiation type mappings in the format of type=instantiatedType,type=instantiatedType.For example (in Java): array=ArrayList,map=HashMap. In other words array types will get instantiated as ArrayList in generated code. You can also have multiple occurrences of this option."
    )
    protected List<String> instantiationTypes = new ArrayList();
    @Option(
            name = {"--type-mappings"},
            title = "type mappings",
            description = "sets mappings between swagger spec types and generated code types in the format of swaggerType=generatedType,swaggerType=generatedType. For example: array=List,map=Map,string=String. You can also have multiple occurrences of this option."
    )
    protected List<String> typeMappings = new ArrayList();
    @Option(
            name = {"--additional-properties"},
            title = "additional properties",
            description = "sets additional properties that can be referenced by the mustache templates in the format of name=value,name=value. You can also have multiple occurrences of this option."
    )
    protected List<String> additionalProperties = new ArrayList();
    @Option(
            name = {"--language-specific-primitives"},
            title = "language specific primitives",
            description = "specifies additional language specific primitive types in the format of type1,type2,type3,type3. For example: String,boolean,Boolean,Double. You can also have multiple occurrences of this option."
    )
    protected List<String> languageSpecificPrimitives = new ArrayList();
    @Option(
            name = {"--import-mappings"},
            title = "import mappings",
            description = "specifies mappings between a given class and the import that should be used for that class in the format of type=import,type=import. You can also have multiple occurrences of this option."
    )
    protected List<String> importMappings = new ArrayList();
    @Option(
            name = {"--invoker-package"},
            title = "invoker package",
            description = "root package for generated code"
    )
    protected String invokerPackage;
    @Option(
            name = {"--group-id"},
            title = "group id",
            description = "groupId in generated pom.xml"
    )
    protected String groupId;
    @Option(
            name = {"--artifact-id"},
            title = "artifact id",
            description = "artifactId in generated pom.xml"
    )
    protected String artifactId;
    @Option(
            name = {"--artifact-version"},
            title = "artifact version",
            description = "artifact version in generated pom.xml"
    )
    protected String artifactVersion;
    @Option(
            name = {"--library"},
            title = "library",
            description = "library template (sub-template)"
    )
    protected String library;
    @Option(
            name = {"--git-user-id"},
            title = "git user id",
            description = "Git user ID, e.g. swagger-api."
    )
    protected String gitUserId;
    @Option(
            name = {"--git-repo-id"},
            title = "git repo id",
            description = "Git repo ID, e.g. swagger-codegen."
    )
    protected String gitRepoId;
    @Option(
            name = {"--release-note"},
            title = "release note",
            description = "Release note, default to 'Minor update'."
    )
    protected String releaseNote;
    @Option(
            name = {"--http-user-agent"},
            title = "http user agent",
            description = "HTTP user agent, e.g. codegen_csharp_api_client, default to 'Swagger-Codegen/{packageVersion}}/{language}'"
    )
    protected String httpUserAgent;
    @Option(
            name = {"--reserved-words-mappings"},
            title = "reserved word mappings",
            description = "specifies how a reserved name should be escaped to. Otherwise, the default _<name> is used. For example id=identifier. You can also have multiple occurrences of this option."
    )
    protected List<String> reservedWordsMappings = new ArrayList();
    @Option(
            name = {"--ignore-file-override"},
            title = "ignore file override location",
            description = "Specifies an override location for the .swagger-codegen-ignore file. Most useful on initial generation."
    )
    protected String ignoreFileOverride;
    @Option(
            name = {"--remove-operation-id-prefix"},
            title = "remove prefix of the operationId",
            description = "Remove prefix of operationId, e.g. config_getId => getId"
    )
    protected Boolean removeOperationIdPrefix;
    @Option(
            name = {"--skip-alias-generation"},
            title = "skip alias generation.",
            description = "skip code generation for models identified as alias."
    )
    protected Boolean skipAliasGeneration;
    @Option(
            name = {"--ignore-import-mapping"},
            title = "ignore import mapping",
            description = "allow generate model classes using names previously listed on import mappings."
    )
    protected String ignoreImportMappings;

    public TeamCityGenerateRunnable() {
        super();
    }

    public void run() {
        CodegenConfigurator configurator = CodegenConfigurator.fromFile(this.configFile);
        if (configurator == null) {
            configurator = new CodegenConfigurator();
        }

        if (this.verbose != null) {
            configurator.setVerbose(this.verbose);
        }

        if (this.skipOverwrite != null) {
            configurator.setSkipOverwrite(this.skipOverwrite);
        }

        if (StringUtils.isNotEmpty(this.spec)) {
            configurator.setInputSpec(this.spec);
        }

        if (StringUtils.isNotEmpty(this.lang)) {
            configurator.setLang(this.lang);
        }

        if (StringUtils.isNotEmpty(this.output)) {
            configurator.setOutputDir(this.output);
        }

        if (StringUtils.isNotEmpty(this.auth)) {
            configurator.setAuth(this.auth);
        }

        if (StringUtils.isNotEmpty(this.templateDir)) {
            configurator.setTemplateDir(this.templateDir);
        }

        if (StringUtils.isNotEmpty(this.apiPackage)) {
            configurator.setApiPackage(this.apiPackage);
        }

        if (StringUtils.isNotEmpty(this.modelPackage)) {
            configurator.setModelPackage(this.modelPackage);
        }

        if (StringUtils.isNotEmpty(this.modelNamePrefix)) {
            configurator.setModelNamePrefix(this.modelNamePrefix);
        }

        if (StringUtils.isNotEmpty(this.modelNameSuffix)) {
            configurator.setModelNameSuffix(this.modelNameSuffix);
        }

        if (StringUtils.isNotEmpty(this.invokerPackage)) {
            configurator.setInvokerPackage(this.invokerPackage);
        }

        if (StringUtils.isNotEmpty(this.groupId)) {
            configurator.setGroupId(this.groupId);
        }

        if (StringUtils.isNotEmpty(this.artifactId)) {
            configurator.setArtifactId(this.artifactId);
        }

        if (StringUtils.isNotEmpty(this.artifactVersion)) {
            configurator.setArtifactVersion(this.artifactVersion);
        }

        if (StringUtils.isNotEmpty(this.library)) {
            configurator.setLibrary(this.library);
        }

        if (StringUtils.isNotEmpty(this.gitUserId)) {
            configurator.setGitUserId(this.gitUserId);
        }

        if (StringUtils.isNotEmpty(this.gitRepoId)) {
            configurator.setGitRepoId(this.gitRepoId);
        }

        if (StringUtils.isNotEmpty(this.releaseNote)) {
            configurator.setReleaseNote(this.releaseNote);
        }

        if (StringUtils.isNotEmpty(this.httpUserAgent)) {
            configurator.setHttpUserAgent(this.httpUserAgent);
        }

        if (StringUtils.isNotEmpty(this.ignoreFileOverride)) {
            configurator.setIgnoreFileOverride(this.ignoreFileOverride);
        }

        if (this.removeOperationIdPrefix != null) {
            configurator.setRemoveOperationIdPrefix(this.removeOperationIdPrefix);
        }

        if (this.ignoreImportMappings != null) {
            this.additionalProperties.add(String.format("%s=%s", "ignoreImportMappings", Boolean.parseBoolean(this.ignoreImportMappings)));
        }

        CodegenConfiguratorUtils.applySystemPropertiesKvpList(this.systemProperties, configurator);
        CodegenConfiguratorUtils.applyInstantiationTypesKvpList(this.instantiationTypes, configurator);
        CodegenConfiguratorUtils.applyImportMappingsKvpList(this.importMappings, configurator);
        CodegenConfiguratorUtils.applyTypeMappingsKvpList(this.typeMappings, configurator);
        CodegenConfiguratorUtils.applyAdditionalPropertiesKvpList(this.additionalProperties, configurator);
        CodegenConfiguratorUtils.applyLanguageSpecificPrimitivesCsvList(this.languageSpecificPrimitives, configurator);
        CodegenConfiguratorUtils.applyReservedWordsMappingsKvpList(this.reservedWordsMappings, configurator);
        ClientOptInput clientOptInput = configurator.toClientOptInput();
        (new TeamCityGenerator()).opts(clientOptInput).generate();
    }
}
