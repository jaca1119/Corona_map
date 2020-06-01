package com.itvsme.corona;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Profile("!test")
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration
{
    @Value("${mongo.config.url}")
    private String mongoURL;

    @Override
    public MongoClient mongoClient()
    {
        return MongoClients.create(mongoURL);
    }

    @Override
    protected String getDatabaseName()
    {
        return "info";
    }
}
