import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import twitter4j.*;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

// Melbourne
public class Console {
	// set your keys first
	private static String api_key = "NLOtn9f81Sv9EMEkcYnwg3dnf";
	private static String api_secret = "C4RyNfU0D99E1NcA1lq8vBGzjvCwZzEVFdwrPF52ICMI4Fnngt";
	private static CouchDbClient dbClient = null;
	private static JsonParser parser = null;
	private static Long indexID = null;
	private static int numOfConflict =0;
	private static PrintWriter output;
	
	public static void main(String[] args) throws TwitterException, FileNotFoundException {
		if(args.length<1){
			System.out.println("The IP address of couchdb should be assigned");
			System.out.println("Usage:java -jar <application> <ip> <index(optional)>");
			System.exit(0);
		}
		String couch_ip = args[0];
        // setup couchdb client, json parser
	    initTools(couch_ip);	
	    // create an twitter factory and change setting for supporting rawJson convert
	    ConfigurationBuilder cb = new ConfigurationBuilder();
	    cb.setApplicationOnlyAuthEnabled(true); //
	    cb.setJSONStoreEnabled(true);
	    TwitterFactory twitter_factory = new TwitterFactory(cb.build());
	    // create an api instance and setup authentication
	    Twitter twitter = twitter_factory.getInstance();
	    twitter.setOAuthConsumer(api_key, api_secret);
	    OAuth2Token au2token = twitter.getOAuth2Token(); //
	    twitter.setOAuth2Token(au2token);  //
	    // create a query and set number of results & geography index
	    Query query = new Query();
	    query.setGeoCode(new GeoLocation(-37.814107, 144.963280), 10.0, Query.KILOMETERS);
//	    query.setGeoCode(new GeoLocation(-37.81837, 144.96672), 0.1, Query.KILOMETERS);
	    // an index
//	    indexID = Long.parseLong("462989079270137856");  //461391201657040896
	    if(args.length==2)
	    	indexID = Long.parseLong(args[1]);
	    System.out.println("Tweets per response:100; Waitting time:0.00 sec");
	    QueryResult result = null;
	    List<Status> status_list = null;
	    while(true){
	    	try {
	    		numOfConflict = 0;
	    		if(indexID!=null){
	    		    query.setCount(100);  // default setting is 15
	    		    query.setMaxId(indexID);
	    		}
	    		else
	    			query.setCount(1);
	    	    // send query and and get a tweets list. 
	        	result = twitter.search(query);
	        	status_list = result.getTweets();
	        	// save each tweet to couchdb
	    	    for(int i =0 ;i<status_list.size(); i++){
	                Status status = status_list.get(i);
	    	        String id = Long.toString(status.getId());
	    	        String rawJSON = TwitterObjectFactory.getRawJSON(status);
	    	        JsonObject tweet = (JsonObject)parser.parse(rawJSON);
	    	        tweet.addProperty("_id", id);
	    	        try{
//	    	        	System.out.println(status.getCreatedAt());
//	    	        	System.out.println(Long.toString(status.getId()));
	    	            dbClient.save(tweet);
	    	        }catch (Exception e){
	    		        System.out.println("duplicated tweets occurs. Discard"+ status.getId());
	    		        System.out.println("Or something wrong with couchdb");
	    		        numOfConflict++;
	    	        }
	    	        if(i==status_list.size()-1){
	    		        indexID = status.getId()-1;
	    	        }
	    	    }
	    	}catch (Exception e){
	    	    e.printStackTrace();
	    	    try {
					Thread.sleep(70*1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
	    	}
	    	finally {
	    	    try {
	    	    	Thread.sleep(1000);
		        	output = new PrintWriter (new FileOutputStream("mel_index.txt", false));
		        	output.println(indexID);
		        	output.close();
		        	System.out.println("Got:"+status_list.size()+" tweets");
		        	System.out.println("Conflict:"+ numOfConflict);
		        	if(numOfConflict>10){
		        		Thread.sleep(2000);
		        		if (numOfConflict>98){
		        			Calendar cal = Calendar.getInstance();
		        	        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		        			output = new PrintWriter (new FileOutputStream("log.txt", true));
				        	output.println("Auto End"+"   "+dateFormat.format(cal.getTime()));
				        	output.close();
		        			System.out.println("Auto End"+"   "+dateFormat.format(cal.getTime()));
		        			System.exit(0);
		        		}		        			
		        	}	
				    if(status_list.size()<70){
				    	Thread.sleep(3000);
				    }
				    if(status_list.size()==1 && numOfConflict==1){
				    	Thread.sleep(1000*5*60);
				    }
			    } catch (InterruptedException e) {
				    e.printStackTrace();
			    }
	    	}
	    }
	}
        public static void initTools(String couch_ip){
            CouchDbProperties properties = new CouchDbProperties()
              .setDbName("melbourne_tweets")
              .setCreateDbIfNotExist(true)
              .setProtocol("http")
              .setHost(couch_ip)
              .setPort(5984)
              .setUsername("admin")
              .setPassword("admin")
              .setMaxConnections(100)
              .setConnectionTimeout(0);
            dbClient = new CouchDbClient(properties);
            parser = new JsonParser();
        }
}
