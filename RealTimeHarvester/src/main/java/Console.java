import twitter4j.FilterQuery;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Yikai Gong
 */

public class Console {
    public static PrintWriter out;

    public static void main(String[] args) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setJSONStoreEnabled(true);
        cb.setOAuthConsumerKey("WbOlRQ57jyp9vnvb5ADLH5bTR");
        cb.setOAuthConsumerSecret("VwtIz3DtPcxFhYrk0PuP9yoldyz9Da9Ej6FzHTtNPyHJDNXNLW");
        cb.setOAuthAccessToken("2467677446-WNTeaJVNyIvgl7aWhyOanh1sXX0PsN7hOww9kfu");
        cb.setOAuthAccessTokenSecret("DU8kRPStao0lFzkGKNqYlGeojs34lPyttOh4R0dbU5HdW");

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new MyListener();
        twitterStream.addListener(listener);

        FilterQuery filterQuery = new FilterQuery();
        //{{longitude1, lat1}, {longitude2, lat2}}
        double[][] boundingBox = {{144.811478, -37.929863}, {145.135574, -37.731370}};
        filterQuery.locations(boundingBox);

        try {
            out = new PrintWriter(new FileOutputStream("RealTimeLog", true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //Launch
        twitterStream.filter(filterQuery);
    }
}
