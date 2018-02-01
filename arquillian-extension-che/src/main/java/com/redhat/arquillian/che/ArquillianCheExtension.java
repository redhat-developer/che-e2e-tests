package com.redhat.arquillian.che;

import com.redhat.arquillian.che.config.CheExtensionConfigurator;
import com.redhat.arquillian.che.service.CheWorkspaceService;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class ArquillianCheExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.observer(CheWorkspaceManager.class);
        extensionBuilder.observer(CheExtensionConfigurator.class);
        extensionBuilder.observer(CheWorkspaceService.class);
        extensionBuilder.service(ResourceProvider.class, CheWorkspaceProducer.class);
        extensionBuilder.service(ResourceProvider.class, CheWorkspaceProviderProducer.class);
    }
}
