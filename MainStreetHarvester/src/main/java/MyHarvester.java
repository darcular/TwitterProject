import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import twitter4j.*;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author Yikai Gong
 */

public class MyHarvester {
    private String region = null;
    private String ip = null;
    private String api_key = "ypAzZqRUYtpOo1JchWJ0LTLWP";   //default key
    private String api_secret = "PWoMZ9Omsw7nGPWrVEKMQAMxCP6Ie98yf4LcsGsuJJHTPSgfw4";
    private CouchDbClient dbClient;
    private JsonParser jsonParser;
    private Twitter twitter;
    private PrintWriter output;
    private Integer stationIndex = 0;
    private Long tweetsIndex = null;
    private boolean initiated = false;

    public MyHarvester(String couchIp, String region){
        this.ip = couchIp;
        this.region = region;
    }
    public void setApi_key(String key){
        this.api_key = key;
    }
    public void setApi_secret(String secret){
        this.api_secret = secret;
    }
    public void init(){
        //init couchdb
        CouchDbProperties properties = new CouchDbProperties()
                .setDbName("street_tweets")
                .setCreateDbIfNotExist(true)
                .setProtocol("http")
                .setHost(ip)
                .setPort(5984)
                .setUsername("admin")
                .setPassword("admin")
                .setMaxConnections(100)
                .setConnectionTimeout(0);
        dbClient = new CouchDbClient(properties);
        jsonParser = new JsonParser();
        //init twitter api
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setApplicationOnlyAuthEnabled(true);
        cb.setJSONStoreEnabled(true);
        TwitterFactory twitter_factory = new TwitterFactory(cb.build());
        twitter = twitter_factory.getInstance();
        twitter.setOAuthConsumer(api_key, api_secret);
        OAuth2Token au2token = null;
        try {
            au2token = twitter.getOAuth2Token();
        } catch (TwitterException e) {
            e.printStackTrace();
            System.exit(1);
        }
        twitter.setOAuth2Token(au2token);

        initiated = true;
    }

    public void doSearch(String streetName, Double latitude, Double longitude, float radius){
        if(!initiated){
            System.out.println("harvester has not been initiated");
            System.exit(0);
        }
        //init a query
        Query query = new Query();
        query.setGeoCode(new GeoLocation(latitude, longitude), radius, Query.KILOMETERS);
        query.setCount(100);
        List<Status> status_list = null;
        QueryResult result = null;
//        Long indexID = tweetsIndex;
        Long indexID = null;
        boolean end = false;
        while (!end){
            int numOfConflict = 0;
            try{
                if(indexID!=null){
                    query.setMaxId(indexID);
                    indexID = null;
                }
                // send query and and get a tweets list.
                result = twitter.search(query);
                status_list = result.getTweets();
                // save each tweet to couchdb
                for(int i =0 ;i<status_list.size(); i++){
                    Status status = status_list.get(i);
                    String id = Long.toString(status.getId());
                    String rawJSON = TwitterObjectFactory.getRawJSON(status);
                    JsonObject tweet = (JsonObject)jsonParser.parse(rawJSON);
                    tweet.addProperty("_id", id);

                    JsonObject harvesterInfo = new JsonObject();
                    harvesterInfo.addProperty("method", "search");
                    harvesterInfo.addProperty("catalog", "street");
                    harvesterInfo.addProperty("name", streetName);
                    JsonObject geo = new JsonObject();
                    geo.addProperty("type", "round_area");
                    geo.addProperty("latitude", latitude);
                    geo.addProperty("longitude", longitude);
                    geo.addProperty("radius", radius);
                    harvesterInfo.add("geo", geo);
                    tweet.add("harvesterInfo", harvesterInfo);
                    try{
                        dbClient.save(tweet);
                    }catch (Exception e){
//                        System.out.println("duplicated tweets occurs. Discard"+ status.getId());
                        numOfConflict++;
                    }
                    if(i==status_list.size()-1){
                        indexID = status.getId()-1;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                try {
                    Thread.sleep(70*1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {
//                    output = new PrintWriter(new FileOutputStream("road_"+region+"_index.txt", false));
//                    output.println(stationIndex+","+indexID);
//                    output.close();
                    System.out.println("Got:"+status_list.size()+" tweets");
                    System.out.println("Conflict:"+ numOfConflict);
                    Thread.sleep(2200);
                    if(status_list.size()<95){
                        end = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
