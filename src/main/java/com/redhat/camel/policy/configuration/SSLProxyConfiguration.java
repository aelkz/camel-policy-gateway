package com.redhat.camel.policy.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSLProxyConfiguration {

    @Value("${proxy.keystore.dest}")
    private String keystoreDest;
    @Value("${proxy.keystore.pass}")
    private String keystorePass;
    @Value("${proxy.schema}")
    private String schema;
    @Value("${proxy.port}")
    private String port;

    public String getKeystoreDest() {
        return keystoreDest;
    }

    public void setKeystoreDest(String keystoreDest) {
        this.keystoreDest = keystoreDest;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

}
