package com.amplifino.nestor.mongodb.driver;

import java.util.function.Consumer;

import org.bson.Document;
import org.junit.Test;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoTest {

	@Test
	public void test() {
		try (MongoClient mongoClient = new MongoClient()) {
			mongoClient.listDatabaseNames().forEach((Block<String>) System.out::println);
			MongoDatabase database = mongoClient.getDatabase("test");
			MongoCollection<Document> collection = database.getCollection("test");
			FindIterable<Document> iterable = collection.find();
			iterable.forEach((Block<Document>) System.out::println);
		} 
	}
}
