import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author Yikai Gong
 */

public class Processor {
    private static JsonObject jsonObject;
    private static JsonArray roadInfos;

    public static void main(String[] args) {
        roadInfos = new JsonArray();

        //Retrieve data to array
        retrieveMainStreetInfo("PSMA_Road_Inner.json", roadInfos);

        retrieveMainStreetInfo("PSMA_MainRoad_West.json", roadInfos);

        retrieveMainStreetInfo("PSMA_MainRoad_SourceEast.json", roadInfos);

        retrieveMainStreetInfo("PSMA_Road_InnerEast.json", roadInfos);

        //Save
        saveMainStreetInfos(roadInfos, "MainStreetInfos.json");

        System.out.println("finish");

    }

    public static void retrieveMainStreetInfo(String file_path, JsonArray roadInfos){
        InputStream inputStream = Processor.class.getResourceAsStream(file_path);
        Scanner scanner = new Scanner(inputStream);
        jsonObject = (JsonObject) new JsonParser().parse(scanner.nextLine());
        JsonArray features = jsonObject.getAsJsonArray("features");
        for (JsonElement feature : features){
            String streetName = feature.getAsJsonObject().getAsJsonObject("properties").get("full_name").getAsString();
            if(streetName.equals("CITYLINK") || streetName.equals("WEST GATE FREEWAY") || streetName.equals("EASTERN FREEWAY") || streetName.equals("MONASH FREEWAY")){
                JsonObject roadInfo = new JsonObject();
                JsonArray roadCoordinates = new JsonArray();
                JsonArray coordinatesArray = feature.getAsJsonObject().getAsJsonObject("geometry").getAsJsonArray("coordinates");
                for(JsonElement coordinate : coordinatesArray){
                    JsonObject coordinateJson = new JsonObject();
                    coordinateJson.addProperty("latitude", coordinate.getAsJsonArray().get(1).getAsString());
                    coordinateJson.addProperty("longitude", coordinate.getAsJsonArray().get(0).getAsString());
                    roadCoordinates.add(coordinateJson);
                }
                roadInfo.addProperty("streetName", streetName);
                roadInfo.add("coordinates", roadCoordinates);
                roadInfos.add(roadInfo);
            }
        }
    }

    public static void saveMainStreetInfos(JsonArray roadInfos, String path){
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(path, false));
            out.println(roadInfos.toString());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
