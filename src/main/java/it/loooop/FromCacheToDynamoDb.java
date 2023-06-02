package it.loooop;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import it.loooop.model.Cache;
import it.loooop.service.DdbTableService;
import it.loooop.service.MomentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FromCacheToDynamoDb implements RequestHandler<Cache, Cache> {

    Logger logger = LoggerFactory.getLogger(FromDbToCache.class);
    MomentoService momentoService = new MomentoService("MOMENTO_AUTH_TOKEN");
    DdbTableService dynamoDbService = new DdbTableService();

    @Override
    public Cache handleRequest(Cache cache, Context context) {
       Integer movedItems = 0;

       dynamoDbService.createTable(cache.getName(), cache.getKey());

       for(int i = 0; i < cache.getRows(); i++){
           try {
               Map<String, String> cacheItem = momentoService.readItem(cache.getName(), "" + i);

               dynamoDbService.putItem(cache.getName(), cacheItem);

               movedItems++;
           } catch(Exception e) {
               logger.atError().setCause(e).log("Error moving item from Momento Cache to DynamoDb: {}", e.getMessage());
               throw e;
           }
       }

        return new Cache(cache.getName(), movedItems, cache.getKey());
    }
}
