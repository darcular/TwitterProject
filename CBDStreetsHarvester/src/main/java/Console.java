import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Yikai Gong
 */

public class Console {
    public static void main(String[] args) {
        //setting the parameters
        if(args.length<1){
            System.out.println("The IP address of couchdb should be assigned");
            //TODO parameter can include PSMA file and specify region
            System.out.println("Usage:java -jar <application> <ip> <streetIndex(optional)>");
            System.exit(0);
        }
        String ip = args[0];
        int streetIndex = args.length>1 ? new Integer(args[1]) : 0;
        String region = "Inner_MelbourneCity";
        //TODO loop for continuously reading PSMA files

        //init harvester
        MyHarvester harvester = new MyHarvester(ip, region);
        harvester.setApi_key("GpAo6IYN80MydhBMgkGJBgtwd");
        harvester.setApi_secret("H5hkXMQCwtawtDcjzkQopMiamuzRWRGAVxiGPjDECaFXg4oAHl");
        harvester.init();


        //read PSMA file and convert into json object
        final int l = 8;
        PSMAReader psmaReader = new PSMAReader("PSMA_Road_"+region+".json");
        JsonArray roadInfos = psmaReader.getRoadInfos();
        int totalNumOfRoads = roadInfos.size();

        /**
         * Start travels points
         */
//        while(true) {
            log("Start harvest streets in " + region);
            //each road
            int k = -1;
            for (JsonElement roadInfo : roadInfos) {
                k++;
                if (k < streetIndex) continue;
                String streetName = roadInfo.getAsJsonObject().get("streetName").getAsString();
                System.out.println(streetName);
                printStreetIndex(region, streetName, k, totalNumOfRoads - 1);
                JsonArray coordinates = roadInfo.getAsJsonObject().getAsJsonArray("coordinates");
                //each pair of two points
                for (int i = 0; i < coordinates.size() - 1; i++) {
                    JsonObject coordinate1 = coordinates.get(i).getAsJsonObject();
                    JsonObject coordinate2 = coordinates.get(i + 1).getAsJsonObject();
                    Double lat1 = coordinate1.get("latitude").getAsDouble();
                    Double lon1 = coordinate1.get("longitude").getAsDouble();
                    Double lat2 = coordinate2.get("latitude").getAsDouble();
                    Double lon2 = coordinate2.get("longitude").getAsDouble();
                    Double diffLat = lat2 - lat1;
                    Double diffLon = lon2 - lon1;
                    Double distance = getDistance(lat1, lon1, lat2, lon2);
//                System.out.println(distance);
                    int block = (int) (distance / l) + 1;
                    Double dlat = diffLat / block;
                    Double dlon = diffLon / block;
                    //each sample point
                    for (int j = 0; j <= block; j++) {
                        Double currentLat = lat1 + j * dlat;
                        Double currentLon = lon1 + j * dlon;
                        harvester.doSearch(streetName, currentLat, currentLon, l / (1000f));
//                    System.out.println(l/(2*1000f));
                        System.out.println(currentLat + ", " + currentLon);
                    }
                }
            }
            System.out.println("Auto finished harvesting streets in " + region);
            log("Auto finished harvesting streets in " + region);
//        }
    }

    public static Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2){
        Double dlat = rad(lat1) - rad(lat2);
        Double dlon = rad(lon1) - rad(lon2);
        Double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(dlat/2),2) +
                Math.cos(rad(lat1))*Math.cos(rad(lat2))*Math.pow(Math.sin(dlon/2),2)));
        s = s * 6378.137 ;  // EARTH_RADIUS
//        s = Math.round(s * 10000d)/ 10d;
        s = s *1000;
        return s;
    }

    public static Double rad(Double deg){
        return deg * Math.PI / 180.0;
    }

    public static void printStreetIndex(String region, String streetName, int k, int total){
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream("street_"+region+"_index.txt", false));
            out.println("streetName: "+streetName+", "+"index: "+ k+"/"+total);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void log(String content){
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream("log", true));
            Calendar cal = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            out.println(content+"   "+dateFormat.format(cal.getTime()));
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
