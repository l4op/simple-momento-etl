package it.loooop.client;

import momento.sdk.CacheClient;
import momento.sdk.auth.CredentialProvider;
import momento.sdk.auth.EnvVarCredentialProvider;
import momento.sdk.config.Configurations;

import java.time.Duration;

public class Momento {

    private final String AUTH_TOKEN_ENV_VAR;
    private final Duration DEFAULT_ITEM_TTL;

    public Momento(String momentoAuthToken, Integer itemTtlInSeconds) {
        this.AUTH_TOKEN_ENV_VAR = momentoAuthToken;
        this.DEFAULT_ITEM_TTL = Duration.ofSeconds(itemTtlInSeconds);
    }

    public CacheClient getCacheClient(){
        final CredentialProvider credentialProvider = new EnvVarCredentialProvider(AUTH_TOKEN_ENV_VAR);

        return  CacheClient.builder(credentialProvider, Configurations.Laptop.latest(), DEFAULT_ITEM_TTL)
                .build();
    }
}
