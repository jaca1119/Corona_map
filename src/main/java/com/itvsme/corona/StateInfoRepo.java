package com.itvsme.corona;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StateInfoRepo extends MongoRepository<StateInfo, String>
{
}
