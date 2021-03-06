package com.jetbrains.codegen;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import io.swagger.codegen.SwaggerCodegen;
import io.swagger.codegen.cmd.*;

public class TeamCityCodegen extends SwaggerCodegen {
    public static void main(String[] args) {
        String version = Version.readVersionFromResources();
        @SuppressWarnings("unchecked")
        Cli.CliBuilder<Runnable> builder =
                Cli.<Runnable>builder("swagger-codegen-cli")
                        .withDescription(
                                String.format(
                                        "Swagger code generator CLI (version %s). More info on swagger.io",
                                        version))
                        .withDefaultCommand(Langs.class)
                        .withCommands(TeamCityGenerateRunnable.class, Generate.class, Meta.class, Langs.class, Help.class,
                                ConfigHelp.class, Validate.class, Version.class);

        builder.build().parse(args).run();
    }
}
