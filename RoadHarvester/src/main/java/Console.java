import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * @author Yikai Gong
 */

public class Console {
    public static void main(String[] args) {
        //setting the parameters
        if(args.length<1){
            System.out.println("The IP address of couchdb should be assigned");
            //TODO parameter can include PSMA file, roadIndex, etc
            System.out.println("Usage:java -jar <application> <ip> <stationIndex(optional)> <tweetsIndex(optional)>");
            System.exit(0);
        }
        String ip = args[0];

        //TODO add index for continue

        //init harvester
        MyHarvester harvester = new MyHarvester(ip, "CBD");
        harvester.init();


        //read PSMA file and convert into json object
        final int l = 16;
        PSMAReader psmaReader = new PSMAReader("PSMA_Road_CBD.json");
        JsonArray roadInfos = psmaReader.getRoadInfos();


        /**
         * Start travels points
         */
        //each road
        for (JsonElement roadInfo : roadInfos){
            String streetName = roadInfo.getAsJsonObject().get("streetName").getAsString();
            System.out.println(streetName);
            JsonArray coordinates = roadInfo.getAsJsonObject().getAsJsonArray("coordinates");
            //each pair of two points
            for (int i=0; i<coordinates.size()-1; i++){
                JsonObject coordinate1 = coordinates.get(i).getAsJsonObject();
                JsonObject coordinate2 = coordinates.get(i+1).getAsJsonObject();
                Double lat1 = coordinate1.get("latitude").getAsDouble();
                Double lon1 = coordinate1.get("longitude").getAsDouble();
                Double lat2 = coordinate2.get("latitude").getAsDouble();
                Double lon2 = coordinate2.get("longitude").getAsDouble();
                Double diffLat = lat2 - lat1;
                Double diffLon = lon2 - lon1;
                Double distance = getDistance(lat1, lon1, lat2, lon2);
                System.out.println(distance);
                int block = (int)(distance/l)+1;
                Double dlat = diffLat / block;
                Double dlon = diffLon / block;
                //each sample point
                for(int j = 0; j <= block; j++){
                    Double currentLat = lat1 + j * dlat;
                    Double currentLon = lon1 + j * dlon;
                    //TODO do search
                    harvester.doSearch(streetName, currentLat, currentLon, l/(2*1000f));
//                    System.out.println(l/(2*1000f));
                    System.out.println(currentLat+", "+currentLon);
                }
            }
        }
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
        Double rad = deg * Math.PI / 180.0;
        return rad;
    }
}
