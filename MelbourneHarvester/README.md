MelbourneHarvester
======
This application is used for harvesting historical tweets data from Twitter RESTful API.
- Install via command: $ cd MelbourneHarvester/ && mvn clean package
- Launch via command: $ java -jar run-MelbourneHarvester-1.0-SNAPSHOT.jar (IP of database) (index(optional))
- Two log files will be created after first start. The log can trace the index for harvester recovery
 
##Description

This harvester aim to collect all the historical tweets in Melbourne (with a big bounding box).
