import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import twitter4j.*;

import java.util.logging.Logger;


/**
 * @author Yikai Gong
 */

public class MyListener implements StatusListener {
    public static JsonParser jsonParser = new JsonParser();

    private WebServiceConnector connector = new WebServiceConnector();

    @Override
    public void onStatus(Status status) {
        String id = Long.toString(status.getId());
        String rawJSON = TwitterObjectFactory.getRawJSON(status);
        JsonObject tweet = (JsonObject)jsonParser.parse(rawJSON);
//        tweet.remove("entities");
        tweet.remove("id");
//        tweet.remove("retweeted_status");
//        System.out.println(tweet.toString());
//        Console.out.println(tweet.toString());
//        Console.out.println();
//        Console.out.flush();
        try{
            //try send tweet to server once got a tweet
            connector.send(tweet);
        }catch(Exception e){
            try{
                // try connect or reconnect to the server when send method fails
                String url = "ws://localhost:8080/web/streamAppEndPoint";
                System.out.println("Try connect/reconnect to server "+url);
                connector.connectToServer(url);
            }catch (Exception ex){
                System.out.println("Cannot find the web server");
            }
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

    @Override
    public void onTrackLimitationNotice(int i) {
        System.out.println("Got track limitation notice:" + i);
    }

    @Override
    public void onScrubGeo(long l, long l2) {
        System.out.println("Got scrub_geo event userId:" + l + " upToStatusId:" + l2);
    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {
        System.out.println("Got Stall Warning:" + stallWarning.getMessage());
    }

    @Override
    public void onException(Exception e) {
        e.printStackTrace();
    }
}
