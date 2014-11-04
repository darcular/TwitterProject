import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import twitter4j.*;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yikai Gong
 */

public class Console {
    private static String ip = null;
    private static String api_key = "ypAzZqRUYtpOo1JchWJ0LTLWP";
    private static String api_secret = "PWoMZ9Omsw7nGPWrVEKMQAMxCP6Ie98yf4LcsGsuJJHTPSgfw4";
    private static CouchDbClient dbClient;
    private static JsonParser jsonParser;
    private static Twitter twitter;
    private static PrintWriter output;
    private static Integer stationIndex = 0;
    private static Long tweetsIndex = null;


    public static void main(String[] args) throws TwitterException {
        if(args.length<1){
            System.out.println("The IP address of couchdb should be assigned");
            System.out.println("Usage:java -jar <application> <ip> <stationIndex(optional)> <tweetsIndex(optional)>");
            System.exit(0);
        }
        ip = args[0];
        stationIndex = args.length>1 ? new Integer(args[1]) : 0;
        tweetsIndex = args.length>2 ? new Long(args[2]) : null;

        //get coordinates arraylist
        PSMAReader psmaReader = new PSMAReader("PSMA_RailwayStations.json");
        JsonArray railwayStationInfos = psmaReader.getRailwayStationInfos();

        //init couchdb clients
        initTools(ip);

        // create an twitter factory and change setting for supporting rawJson convert and ApplicationOnlyAuth
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setApplicationOnlyAuthEnabled(true);
        cb.setJSONStoreEnabled(true);
        TwitterFactory twitter_factory = new TwitterFactory(cb.build());
        // create an api instance and setup authentication
        twitter = twitter_factory.getInstance();
        twitter.setOAuthConsumer(api_key, api_secret);
        OAuth2Token au2token = twitter.getOAuth2Token();
        twitter.setOAuth2Token(au2token);

        for (int i = stationIndex ; i<railwayStationInfos.size() ; i++,stationIndex++){
            JsonElement railwayStationInfo = railwayStationInfos.get(i);
            Double latitude = railwayStationInfo.getAsJsonObject().get("latitude").getAsDouble();
            Double longitude = railwayStationInfo.getAsJsonObject().get("longitude").getAsDouble();
            String stationName = railwayStationInfo.getAsJsonObject().get("stationName").getAsString();
            System.out.println(stationName+": "+latitude+", "+longitude+" start!");
            doSearchByGeo(stationName, latitude, longitude, 0.2f, tweetsIndex);
            tweetsIndex = null;
            log(stationName+": "+latitude+", "+longitude+" finished"+" "+(stationIndex+1)+"/"+317);
        }
//        doSearchByGeo("testName", -37.639402564d, 145.192013026d, 0.1f, null);
    }

    public static void doSearchByGeo(String stationName, Double latitude, Double longitude, float radius, Long tweetsIndex){
        //init a query
        Query query = new Query();
        query.setGeoCode(new GeoLocation(latitude, longitude), radius, Query.KILOMETERS);
        query.setCount(100);
        List<Status> status_list = null;
        QueryResult result = null;
        Long indexID = tweetsIndex;
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
                    harvesterInfo.addProperty("catalog", "railway_station");
                    harvesterInfo.addProperty("name", stationName);
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
                        System.out.println("duplicated tweets occurs. Discard"+ status.getId());
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
                    output = new PrintWriter(new FileOutputStream("railway_index.txt", false));
                    output.println(stationIndex+","+indexID);
                    output.close();
                    System.out.println("Got:"+status_list.size()+" tweets");
                    System.out.println("Conflict:"+ numOfConflict);
                    Thread.sleep(3000);
                    if(status_list.size()<90 || numOfConflict > status_list.size()-1){
                        end = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void initTools(String couch_ip){
        CouchDbProperties properties = new CouchDbProperties()
                .setDbName("railway_station_tweets")
                .setCreateDbIfNotExist(true)
                .setProtocol("http")
                .setHost(couch_ip)
                .setPort(5984)
                .setUsername("admin")
                .setPassword("admin")
                .setMaxConnections(100)
                .setConnectionTimeout(0);
        dbClient = new CouchDbClient(properties);
        jsonParser = new JsonParser();
    }

    public static void log(String log){
        try{
            PrintWriter printWriter = new PrintWriter(new FileOutputStream("railway_log.txt", false));
            printWriter.println(log);
            printWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


