package com.mongodb.jndi;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * Mongo JNDI factory, getObjectInstance method will return an object of type {@code com.mongodb.DB}  if everything goes fine.
 * 
 * 
 * @author lrgoncalves
 *
 */
public class MongoDataBaseJNDIFactory implements ObjectFactory {

	private final Attribute attribute 	= new Attribute();

	private static MongoClient mongoInstance;
	
	public Object getObjectInstance(Object obj, Name name, Context context, Hashtable<?, ?> environment) throws Exception {
		
		if(mongoInstance == null){
			getMongoInstance(environment);
		}

		return mongoInstance.getDB(attribute.database);
	}

	private synchronized void getMongoInstance(Hashtable<?, ?> environment) throws UnknownHostException{
		
		attribute.fillAttributes(environment).validateParameters();
		
		MongoCredential credential = MongoCredential.createCredential(
				attribute.username, attribute.database, attribute.password.toCharArray());
		
		mongoInstance = new MongoClient(fillAddress(),Arrays.asList(credential));
	}
	
	private List<ServerAddress> fillAddress() throws UnknownHostException{

		String [] hosts = attribute.host.split(",");

		List<ServerAddress> serverAddresses = new LinkedList<ServerAddress>();

		for(String hostAddress : hosts){
			serverAddresses.add(new ServerAddress(hostAddress));
		}
		
		return serverAddresses;
	}

	private final class Attribute{

		private static final String MONGO_ADDRESS 	= "host";
		private static final String MONGO_PASSWORD 	= "password";
		private static final String MONGO_USERNAME 	= "username";
		private static final String MONGO_DATABASE	= "database";

		private String host; 
		private String password;
		private String username; 	
		private String database;

		private final void validateParameters()throws RuntimeException{

			if (host == null || host.isEmpty()) {
				throw new RuntimeException(MONGO_ADDRESS + " resource property is empty");
			}
			else if (database == null || database.isEmpty()) {
				throw new RuntimeException(MONGO_DATABASE + " resource property is empty");
			}
			else if (password == null || password.isEmpty()) {
				throw new RuntimeException(MONGO_PASSWORD + " resource property is empty");
			}
			else if (username == null || username.isEmpty()) {
				throw new RuntimeException(MONGO_USERNAME + " resource property is empty");
			}

		}	

		private final Attribute fillAttributes(final Hashtable<?, ?> environment){

			host 		= (String) environment.get(MONGO_ADDRESS);
			password 	= (String) environment.get(MONGO_PASSWORD);
			username	= (String) environment.get(MONGO_USERNAME);
			database 	= (String) environment.get(MONGO_DATABASE);

			return this;
		}
	}
}
