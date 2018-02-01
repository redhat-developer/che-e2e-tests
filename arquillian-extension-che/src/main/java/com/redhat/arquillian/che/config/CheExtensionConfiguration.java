package com.redhat.arquillian.che.config;

import com.redhat.arquillian.che.util.Validate;
import java.util.Map;

import static com.redhat.arquillian.che.util.Validate.isNotEmpty;

public class CheExtensionConfiguration {

    // Properties
    public static final String CHE_STARTER_PROPERTY_NAME = "cheStarterURL";
    public static final String OPENSHIFT_MASTER_URL_PROPERTY_NAME = "openShiftMasterURL";
    public static final String KEYCLOAK_TOKEN_PROPERTY_NAME = "keycloakToken";
    public static final String OPENSHIFT_TOKEN_PROPERTY_NAME = "openShiftToken";
    public static final String PRESERVE_WORKSPACE_PROPERTY_NAME = "preserveWorkspace";
    public static final String OPENSHIFT_NAMESPACE_PROPERTY_NAME = "openShiftNamespace";
    public static final String CHE_WORKSPACE_NAME = "cheWorkspaceName";
    public static final String OSIO_USERNAME_PROPERTY_NAME = "osioUsername";
    public static final String OSIO_PASSWORD_PROPERTY_NAME = "osioPassword";
    public static final String OSIO_URL_PART = "osioUrlPart";

    private String cheStarterUrl;
    private String openshiftMasterUrl;
    private String keycloakToken;
    private String openshiftToken;
    private String openshiftNamespace;
    private Boolean preserveWorkspace;
    private String osioUrlPart;
    private String cheWorkspaceName;

    private String osioUsername;
    private String osioPassword;

    static CheExtensionConfiguration fromMap(Map<String, String> reporterProps) {
        CheExtensionConfiguration config = new CheExtensionConfiguration();
        config.cheStarterUrl = loadProperty(reporterProps, CHE_STARTER_PROPERTY_NAME);
        config.openshiftMasterUrl = loadProperty(reporterProps, OPENSHIFT_MASTER_URL_PROPERTY_NAME);
        config.keycloakToken = loadProperty(reporterProps, KEYCLOAK_TOKEN_PROPERTY_NAME);
        config.openshiftToken = loadProperty(reporterProps, OPENSHIFT_TOKEN_PROPERTY_NAME);
        config.openshiftNamespace = loadProperty(reporterProps, OPENSHIFT_NAMESPACE_PROPERTY_NAME, "eclipse-che");
        config.preserveWorkspace = Boolean.valueOf(loadProperty(reporterProps, PRESERVE_WORKSPACE_PROPERTY_NAME));
        config.osioUrlPart = loadProperty(reporterProps, OSIO_URL_PART, "openshift.io");

        config.osioUsername = loadPropertyAndSetAsSystemProperty(reporterProps, OSIO_USERNAME_PROPERTY_NAME);
        config.osioPassword = loadPropertyAndSetAsSystemProperty(reporterProps, OSIO_PASSWORD_PROPERTY_NAME);

        config.cheWorkspaceName = loadProperty(reporterProps, CHE_WORKSPACE_NAME);

        return config;
    }

    private static String loadProperty(Map<String, String> reporterProps, String propertyName) {
        String value = PropertySecurityAction.getProperty(propertyName);
        if (Validate.isEmpty(value)) {
            value = reporterProps.get(propertyName);
        }
        return value;
    }

    private static String loadPropertyAndSetAsSystemProperty(Map<String, String> reporterProps, String propertyName) {
        String value = loadProperty(reporterProps, propertyName);
        PropertySecurityAction.setProperty(propertyName, value);
        return value;
    }

    private static String loadProperty(Map<String, String> reporterProps, String propertyName, String defaultValue) {
        String value = loadProperty(reporterProps, propertyName);
        if (Validate.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public String getCheStarterUrl() {
        return cheStarterUrl;
    }

    public void setCheStarterUrl(String cheStarterUrl) {
        this.cheStarterUrl = cheStarterUrl;
    }

    public String getOpenshiftMasterUrl() {
        return openshiftMasterUrl;
    }

    public void setOpenshiftMasterUrl(String openshiftMasterUrl) {
        this.openshiftMasterUrl = openshiftMasterUrl;
    }

    public String getKeycloakToken() {
        return "Bearer " + keycloakToken;
    }

    public void setKeycloakToken(String keycloakToken) {
        this.keycloakToken = keycloakToken;
    }

    public String getOpenshiftToken() {
        return openshiftToken;
    }

    public void setOpenshiftToken(String openshiftToken) {
        this.openshiftToken = openshiftToken;
    }

    public String getOpenshiftNamespace() {
        return openshiftNamespace;
    }

    public void setOpenshiftNamespace(String openshiftNamespace) {
        this.openshiftNamespace = openshiftNamespace;
    }

    public Boolean getPreserveWorkspace() {
        return preserveWorkspace;
    }

    public void setPreserveWorkspace(Boolean preserveWorkspace) {
        this.preserveWorkspace = preserveWorkspace;
    }

    public String getAuthorizationToken() {
        return isNotEmpty(keycloakToken) ? getKeycloakToken() : openshiftToken;
    }

    public String getOsioUsername() {
        return osioUsername;
    }

    public void setOsioUsername(String osioUsername) {
        this.osioUsername = osioUsername;
    }

    public String getOsioPassword() {
        return osioPassword;
    }

    public void setOsioPassword(String osioPassword) {
        this.osioPassword = osioPassword;
    }
    
	public String getOsioUrlPart() {
		return osioUrlPart;
	}
	
	public void setOsioUrlPart(String osioUrlPart) {
		this.osioUrlPart = osioUrlPart;
	}

	public void setCheWorkspaceName(String cheWorkspacename) { this.cheWorkspaceName = cheWorkspacename; }

	public String getCheWorkspaceName(){ return this.cheWorkspaceName; }
}
