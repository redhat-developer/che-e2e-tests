/*******************************************************************************
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.arquillian.che;

import com.redhat.arquillian.che.annotations.Workspace;
import com.redhat.arquillian.che.config.CheExtensionConfiguration;
import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.resource.CheWorkspaceStatus;
import com.redhat.arquillian.che.resource.StackService;
import com.redhat.arquillian.che.service.CheWorkspaceService;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;

import static com.redhat.arquillian.che.util.Validate.isEmpty;
import static com.redhat.arquillian.che.util.Validate.isNotEmpty;

public class CheWorkspaceManager {

    private static final Logger logger = Logger.getLogger(CheWorkspaceManager.class);

    @Inject
    @ApplicationScoped
    private InstanceProducer<CheWorkspace> cheWorkspaceInstanceProducer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<CheWorkspaceProvider> cheWorkspaceProviderInstanceProducer;

    @Inject
    private Instance<CheExtensionConfiguration> configurationInstance;

    private String bearerToken;

    public void setupWorkspace(@Observes BeforeSuite event) {
        CheExtensionConfiguration cheExtensionConfig = configurationInstance.get();
        checkRunParams(cheExtensionConfig);

        if (cheExtensionConfig.getCheStarterUrl() == null || cheExtensionConfig.getCheStarterUrl().isEmpty()) {
            startCheStarter();
            cheExtensionConfig.setCheStarterUrl("http://localhost:10000");
        }

        cheWorkspaceProviderInstanceProducer.set(new CheWorkspaceProvider(cheExtensionConfig));
        if (isNotEmpty(cheExtensionConfig.getCheWorkspaceUrl())) {
            cheWorkspaceInstanceProducer.set(cheWorkspaceProviderInstanceProducer.get().getCreatedWorkspace());
        }
        bearerToken = cheExtensionConfig.getKeycloakToken();
    }

    public void beforeClass(@Observes BeforeClass event) {
        //get setting from annotation
        Workspace workspaceAnnotation = event.getTestClass().getAnnotation(Workspace.class);
        if (workspaceAnnotation == null) {
            throw new RuntimeException("Annotation @Workspace wasn't find in class " + event.getTestClass().getName() + ".");
        }
        CheWorkspace createdWkspc = cheWorkspaceInstanceProducer.get();

        if (createdWkspc == null || createdWkspc.isDeleted()) {
            createWorkspace(workspaceAnnotation);
        } else if (!(createdWkspc.getStack().equals(workspaceAnnotation.stackID()))) {
            CheWorkspaceService.stopWorkspace(cheWorkspaceInstanceProducer.get(), bearerToken);
            CheWorkspaceService.deleteWorkspace(cheWorkspaceInstanceProducer.get(), bearerToken);
            createWorkspace(workspaceAnnotation);

            logger.info("Workspace " + createdWkspc.getName() + " created and started.");
        } else if (!CheWorkspaceService.getWorkspaceStatus(createdWkspc, bearerToken).equals(CheWorkspaceStatus.RUNNING.toString())) {
            boolean isStarted = CheWorkspaceService.startWorkspace(createdWkspc);
            if(isStarted) { logger.info("Workspace " + createdWkspc.getName() + " started."); }
            else {
                logger.info("Can not start given workspace! Creating new one.");
                createWorkspace(workspaceAnnotation);
            }
        }
    }

    public void afterClass(@Observes AfterClass event) {
        Workspace workspaceAnnotation = event.getTestClass().getAnnotation(Workspace.class);
        if (workspaceAnnotation.removeAfterTest()) {
            CheWorkspaceService.stopWorkspace(cheWorkspaceInstanceProducer.get(), bearerToken);
            CheWorkspaceService.deleteWorkspace(cheWorkspaceInstanceProducer.get(), bearerToken);
            //the attribute deleted is used, because workspace can't be set at null - arquillian Exception
            cheWorkspaceInstanceProducer.get().setDeleted(true);
        }
    }

    private void createWorkspace(Workspace workspaceAnnotation) {
        CheWorkspaceProvider provider = cheWorkspaceProviderInstanceProducer.get();

        //creating and starting new workspace
        cheWorkspaceInstanceProducer.set(provider.createCheWorkspace(StackService.getPathOfJsonConfig(workspaceAnnotation.stackID())));
        CheWorkspaceService.waitUntilWorkspaceGetsToState(cheWorkspaceInstanceProducer.get(), CheWorkspaceStatus.RUNNING.getStatus(), bearerToken);
    }

    private void startCheStarter() {
        try {
            File cheStarterDir = new File(System.getProperty("user.dir"), "target" + File.separator + "che-starter");

            cloneGitDirectory(cheStarterDir);

            logger.info("Running che starter.");
            Properties props = new Properties();
            props.setProperty("OPENSHIFT_TOKEN_URL", "https://sso." + configurationInstance.get().getOsioUrlPart()
                    + "/auth/realms/fabric8/broker/openshift-v3/token");
            props.setProperty("GITHUB_TOKEN_URL", "https://auth." + configurationInstance.get().getOsioUrlPart()
                    + "/api/token?for=https://github.com");
            props.setProperty("osio.domain.name", "api.starter-us-east-2.openshift.com");
            props.setProperty("oso.address", "api.starter-us-east-2.openshift.com");
            props.setProperty("MULTI_TENANT_CHE_SERVER_URL",
                    "https://che." + configurationInstance.get().getOsioUrlPart());
            EmbeddedMaven
                    .forProject(cheStarterDir.getAbsolutePath() + File.separator + "pom.xml")
                    .useMaven3Version("3.5.0")
                    .setGoals("spring-boot:run")
                    .setProperties(props)
                    .useAsDaemon()
                    .withWaitUntilOutputLineMathes(".*Started Application in.*", 10, TimeUnit.MINUTES)
                    .build();

        } catch (GitAPIException e) {
            throw new IllegalStateException("There was a problem with getting the git che-starter repository", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("The che-starter haven't started within 300 seconds.", e);
        }
    }

    private void cloneGitDirectory(File cheStarterDir) throws GitAPIException, InvalidRemoteException, TransportException {
        logger.info("Cloning che-starter project.");
        try {
            Git
                    .cloneRepository()
                    .setURI("https://github.com/redhat-developer/che-starter")
                    .setDirectory(cheStarterDir)
                    .call();
        } catch (JGitInternalException ex) {
            //repository already cloned. Do nothing.
        }
    }

    private void checkRunParams(CheExtensionConfiguration configuration) {
        StringBuilder sb = new StringBuilder();
        if (isEmpty(configuration.getOpenshiftMasterUrl())) {
            sb.append("OpenShift master URL cannot be null. Set property "
                    + CheExtensionConfiguration.OPENSHIFT_MASTER_URL_PROPERTY_NAME + "and rerun tests\n");
        }

        if ((configuration.getKeycloakToken().equals("Bearer null") && configuration.getOpenshiftToken() == null)) {
            sb.append("Keycloak and OpenShift tokens are null. Set either "
                    + CheExtensionConfiguration.KEYCLOAK_TOKEN_PROPERTY_NAME
                    + " or "
                    + CheExtensionConfiguration.OPENSHIFT_TOKEN_PROPERTY_NAME
                    + " and rerun tests\n");
        }
        if (sb.length() > 0) {
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public void cleanUp(@Observes AfterSuite event) {
        logger.info("All tests were executed, cleaning up.");
        CheExtensionConfiguration config = configurationInstance.get();
        //if run with created workspace - will not delete it in the end
        if (isNotEmpty(config.getCheWorkspaceUrl())) {
            return;
        }
        CheWorkspace workspace = cheWorkspaceInstanceProducer.get();
        if (workspace.isDeleted()) {
            logger.info("Skipping workspace deletion - workspace is already deleted.");
            return;
        }
        if (workspace != null && !config.getPreserveWorkspace()) {
            String workspaceStatus = CheWorkspaceService.getWorkspaceStatus(workspace, bearerToken);
            if (workspaceStatus.equals(CheWorkspaceStatus.RUNNING.getStatus())) {
                logger.info("Stopping " + workspace);
                CheWorkspaceService.stopWorkspace(workspace, bearerToken);
            }
            logger.info("Deleting workspace.");
            CheWorkspaceService.deleteWorkspace(workspace, bearerToken);
        } else {
            logger.info("Skipping workspace deletion - attribute preserve workspace is true.");
        }
    }

}
