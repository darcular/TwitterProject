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
//        cb.setOAuthConsumerKey("bAPV3ZNXqQ6umXAWk26VlNcVI");
//        cb.setOAuthConsumerSecret("lIirhcXtfL4tt1Y5t0Qom64c1Yx1Y8IMmfNRxULQoPgRW5oVEV");
//        cb.setOAuthAccessToken("2467677446-8qg673tMPsMdJqrJ4FBUm3EzKADd6XPqXhLQJid");
//        cb.setOAuthAccessTokenSecret("Bw0NcU37LIsGmoJJflDfY6t4lRyfKl74zN4FAZm234Tyl");

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
