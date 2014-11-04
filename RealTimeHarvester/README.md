RealTimeHarvester
======
- Install via command: $ cd MainHighwayHarvester/ && mvn clean package
- Launch via command: $ java -jar run-realtimeTweets-1.0-SNAPSHOT.jar
- I hardcode this harvester to send data to a tomcat7 server at the same host (127.0.0.1:8080). So the web server and this harvester need to be launched at the same server (order independent)
 
##Description

This is a realtime tweets harvester that connect to Twitter Stream API. And it act like a data feed for the web service. Once the harvester got a tweet it will try to push the data to web service in tomcat. If service cannot be found it will ignore that tweet in hand. I design this structure to improve durability and scalability (e.g. multiple feeds with multiple analysis service)
