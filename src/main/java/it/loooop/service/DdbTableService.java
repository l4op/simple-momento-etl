package it.loooop.service;

import it.loooop.client.DynamoDB;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.HashMap;
import java.util.Map;

public class DdbTableService {
    DynamoDB dynamoDb = new DynamoDB();
    DynamoDbClient dynamoDbBaseClient = dynamoDb.baseClient();

    public void putItem(String table, Map<String, String> item) {

        HashMap<String, AttributeValue> ddbItem = new HashMap<>();

        for (var entry : item.entrySet()) {
            ddbItem.put(entry.getKey(),
                    AttributeValue.builder().s(entry.getValue())
                            .build());
        }

        dynamoDbBaseClient.putItem(PutItemRequest.builder()
                .tableName(table)
                .item(ddbItem)
                .build());

    }

    public void createTable(String table, String key) {

        DynamoDbWaiter dbWaiter = dynamoDbBaseClient.waiter();

        dynamoDbBaseClient.createTable(CreateTableRequest.builder()
                .tableName(table)
                .keySchema(KeySchemaElement.builder()
                        .keyType(KeyType.HASH)
                        .attributeName(key)
                        .build())
                        .attributeDefinitions(AttributeDefinition.builder()
                                .attributeName(key).attributeType(ScalarAttributeType.S).build())
                        .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());

        DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                .tableName(table)
                .build();

        // Wait until the Amazon DynamoDB table is created
        WaiterResponse<DescribeTableResponse> waiterResponse =  dbWaiter.waitUntilTableExists(tableRequest);

    }

}
