package com.shahidfoy.japi;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.shahidfoy.japi.services.v1.User;

public class DataManager {
	
	private static final Logger log = Logger.getLogger(DataManager.class.getName());
	
	private static DataManager INSTANCE;
	
	private static MongoDatabase japiDB;
	
	private static MongoCollection<Document> userCollection;
	
	public static DataManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DataManager();
		}
		
		return INSTANCE;
	}
	
	private DataManager() {
		
		// connect to mongodb
		try {
			MongoClient mongoClient = new MongoClient(new ServerAddress("localhost", 27017));
			
			japiDB = mongoClient.getDatabase("japi");
			
			userCollection = japiDB.getCollection("users");
			
		} catch (Exception e) {
			log.error("db connection error e=", e);
		}
	}
	
	// Insert User into database
	public User insertUser(User user) {
		// get document object
		Document document = new Document("name", user.getName());
		// insert document
		userCollection.insertOne(document);
		// put new id into user object
		user.setId(document.get("_id").toString());
		
		// return new object
		return user;
			
	}
	
	public User mapUserFromDBOject(Document document) {
		User user = new User();
		
		user.setId(document.get("_id").toString());
		user.setName((String) document.get("name"));
		
		return user;
	}
	
	// Find User by Id
	public User findUserById(String userIdString) {
		if(userIdString == null) {
			return null;
		}
		
		try {
			
			// find user by id and sets it as Document 
			Document userObject = (Document) userCollection.find(Filters.eq("_id", new ObjectId(userIdString))).first();
			
			if(userObject != null) {
				return mapUserFromDBOject(userObject);
			} else {
				return null;
			}
			
		} catch (Exception e) {
			log.error("DBManager::findUserById Exception e=", e);
		}
		
		return null;
	}
	
	// find all users 
	public List<User> findAllUsers() {
		
		// create list for users found
		List<User> users = new ArrayList<User>();
		
		try {
			
			// finds all 
			FindIterable<Document> cursor = userCollection.find();
			MongoCursor<Document> cursorIt = cursor.iterator();
			
			if (cursorIt != null) {
				while (cursorIt.hasNext()) {
					Document doc = (Document) cursorIt.next();
					
					User item = mapUserFromDBOject(doc);
					users.add(item);
				}
				return users;
			}
		} catch (Exception e) {
			
		}
		
		return null;
	}
	
	
	// update user
	public User updateUserAttributes(String userId, String attribute, String value) {
		
		userCollection.updateOne(Filters.eq("_id", new ObjectId(userId)), Updates.set(attribute, value));
		return findUserById(userId);
		
	}
	
	
	// delete user 
	public void deleteUserById(String userid) {
		
		userCollection.deleteOne(Filters.eq("_id", new ObjectId(userid)));
		
	}
	
	
	
}
