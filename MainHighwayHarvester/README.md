MainHighwayHarvester
====
- Install via command: $ cd MainHighwayHarvester/ && mvn clean package
- Launch via command: $ java -jar run-MainStreetHavester-1.0-SNAPSHOT.jar (IP of database) (streetIndex(optional))
- Two log files will be created after first start. The log can trace the index for harvester recovery
 
##Description

This harvester aim to collect historical tweets exactly on several main streets in Melbourne(e.g. City Link, West Gate Bridge, Eastern Freeway, Monash Freeway, Nepean Highway, CBD). A Json file provided by PSMA_Preprocessor helps to do this targed harvesting. 
