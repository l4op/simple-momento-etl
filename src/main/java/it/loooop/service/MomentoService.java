package it.loooop.service;

import it.loooop.client.Momento;
import momento.sdk.CacheClient;
import momento.sdk.exceptions.AlreadyExistsException;
import momento.sdk.exceptions.LimitExceededException;
import momento.sdk.responses.cache.control.CacheCreateResponse;
import momento.sdk.responses.cache.dictionary.DictionaryFetchResponse;
import momento.sdk.responses.cache.dictionary.DictionarySetFieldResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class MomentoService {

    Logger logger = LoggerFactory.getLogger(MomentoService.class);
    Momento momentoClient;

    CacheClient client;

    Integer cooldown = 2;

    public MomentoService (String momentoToken) {
        momentoClient = new Momento(momentoToken, 4800);
        client = momentoClient.getCacheClient();
    }

    public void create(String cacheName) throws RuntimeException {

            final CacheCreateResponse createResponse = client.createCache(cacheName).join();

            if (createResponse instanceof CacheCreateResponse.Error error) {
                if (error.getCause() instanceof AlreadyExistsException) {
                    logger.atError().setCause(error.getCause())
                            .log("Cache with name {} already exists", cacheName);
                } else {
                    logger.atError().setCause(error.getCause())
                            .log("Unable to create cache with error {} and message {}",
                                    error.getErrorCode(), error.getMessage());
                }
                throw new RuntimeException(error.getCause());
            }

    }

    public void saveItem(String cacheName, String dictionaryName, String field, String value) throws RuntimeException {

            //No need to save a null value in cache
            if(value != null) {

                final DictionarySetFieldResponse setFieldResponse =
                        client.dictionarySetField(cacheName, dictionaryName, field, value).join();

                if (setFieldResponse instanceof DictionarySetFieldResponse.Error error) {

                    if (error.getCause() instanceof LimitExceededException) {

                        waitRateLimiterCoolDown(dictionaryName);

                        this.saveItem(cacheName, dictionaryName, field, value);

                    } else {

                        logger.atError().setCause(error.getCause())
                                .log("Unable to create cache with error {} and message {}",
                                        error.getErrorCode(), error.getMessage());
                        throw new RuntimeException(error.getCause());
                    }
                }
            }

    }

    public LinkedHashMap<String,String> readItem(String cacheName, String dictionaryName) throws RuntimeException {

            final DictionaryFetchResponse fetchResponse =
                    client.dictionaryFetch(cacheName, dictionaryName).join();
            if (fetchResponse instanceof DictionaryFetchResponse.Hit hit) {
                return new LinkedHashMap<>(hit.valueMap());
            } else if (fetchResponse instanceof DictionaryFetchResponse.Miss) {
                logger.atError().log("Did not find dictionary with name {}", dictionaryName);
            } else if (fetchResponse instanceof DictionaryFetchResponse.Error error) {

                if(error.getCause() instanceof LimitExceededException) {

                    waitRateLimiterCoolDown(dictionaryName);

                    this.readItem(cacheName, dictionaryName);

                } else {
                    logger.atError().setCause(error.getCause())
                            .log("Dictionary fetch failed with error {} and message {}",
                                    error.getErrorCode(), error.getMessage());
                    throw new RuntimeException(error.getCause());
                }
            }

        return new LinkedHashMap<>();
    }

    private void waitRateLimiterCoolDown(String key){
        try {
            logger.info("Sleeping for cooling down the rate limiter at key: {}", key);
            TimeUnit.SECONDS.sleep(cooldown);
        }catch (Exception e) {
            logger.atError().setCause(e.getCause())
                    .log("Sleep failed {}",
                            e.getMessage());
        }
    }

}
