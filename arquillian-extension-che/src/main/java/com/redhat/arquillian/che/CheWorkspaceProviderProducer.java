package com.redhat.arquillian.che;

import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

public class CheWorkspaceProviderProducer implements ResourceProvider {

    @Inject
    private Instance<CheWorkspaceProvider> cheExtensionConfigurationInstance;

    @Override
    public boolean canProvide(Class<?> aClass) {
        return aClass.equals(CheWorkspaceProvider.class);
    }

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {
        return cheExtensionConfigurationInstance.get();
    }
}
