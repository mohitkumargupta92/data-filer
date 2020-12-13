package com.soprasteria.accesscontrol.filter.accesscontroldatafilter;

import java.io.File;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

@ArquillianSuiteDeployment
public class Deployments {
    
    @Deployment
    public static WebArchive createDeployment() {
        File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
        		.importCompileAndRuntimeDependencies()
        		.resolve()
        		.withTransitivity()
        		.asFile();
        File[] testDependencies = Maven.resolver().loadPomFromFile("pom.xml")
        		.importTestDependencies()
        		.resolve()
        		.withTransitivity()
        		.asFile();
        WebArchive archive = ShrinkWrap.create(WebArchive.class)
        		.addPackages(true, "com.soprasteria")
        		.addAsResource("project-it.yml", "project-defaults.yml")
        		.addAsResource("datashield-admin-response.json")
        		.addAsResource("filter_request_body.json")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(dependencies)
                .addAsLibraries(testDependencies);
                
        return archive;
    }
}
