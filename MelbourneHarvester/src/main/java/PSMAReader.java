import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Yikai Gong
 */

public class PSMAReader {
    private JsonObject jsonObject;

    public PSMAReader(String file_path){
        try {
//            FileInputStream fileInputStream = new FileInputStream(this.getClass().getResource(file_path).getPath());
            InputStream inputStream = this.getClass().getResourceAsStream(file_path);
            Scanner scanner = new Scanner(inputStream);
            jsonObject = (JsonObject) new JsonParser().parse(scanner.nextLine());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public JsonArray getRailwayStationInfos(){
//        ArrayList<Double[]> geoPoints = new ArrayList<Double[]>();
        JsonArray railwayStationInfos = new JsonArray();
        JsonArray features = jsonObject.getAsJsonArray("features");
        for (JsonElement feature : features){
            JsonObject railwayStationInfo = new JsonObject();
            String stationName = feature.getAsJsonObject().getAsJsonObject("properties").get("name").getAsString();
            JsonArray coordinatesArray = feature.getAsJsonObject().getAsJsonObject("geometry").getAsJsonArray("coordinates");
            railwayStationInfo.addProperty("stationName", stationName);
            railwayStationInfo.addProperty("latitude", coordinatesArray.get(1).getAsString());
            railwayStationInfo.addProperty("longitude", coordinatesArray.get(0).getAsString());
            railwayStationInfos.add(railwayStationInfo);
        }
        return railwayStationInfos;
    }

//    public long[][] getGeoPoints2(){
//        JsonArray features = jsonObject.getAsJsonArray("features");
//        int numOfCoordinates = features.size();
//        long[][] coordinates = new long[numOfCoordinates][2];
//        for(int i = 0 ; i < features.size() ; i++){
//            JsonElement feature = features.get(i);
//
//        }
//
//        return coordinates;
//    }

    public String toString(){
        JsonArray features = jsonObject.getAsJsonArray("features");
        int i =0;
        for(JsonElement feature : features){
            i++;
            System.out.println(feature.getAsJsonObject().getAsJsonObject("geometry").getAsJsonArray("coordinates").get(0));
        }
        System.out.println(i);
        System.out.println(features.size());
        return features.toString();
    }


}
