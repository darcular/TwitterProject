Distributed Computing Project
==============

##Overview
This repository contains the source of my Distributed Computing Project (COMP90019) in University of Melbourne. It is a 25 credit points subject. Databased side scripts are not included in this repository.
- Supervisor: Richard Sinnott
- Author: Yikai Gong  (ID: 600206)  
- Email: yikaig@student.unimelb.edu.au
- WebService URL: http://115.146.94.12:8080/web/
- GitHub: https://github.com/darcular/TwitterProject

##Enviroment Requirements
For installing and launching these system components, you will need to use these tools/enviroments:
- JDK 1.7 or above
- Tomcat7 or above
- Maven
- CouchDB

##Directory Structure
There are four Twitter Harvesters, one PSMA data preprocessor and one WebService in this directory. All these applications are managed respectively by maven and each sub-directory contains a ReadMe file for the details of that application.

##MelbourneHarvester
This application is used for harvesting historical tweets data from Twitter RESTful API.
- Install via command: $ cd MelbourneHarvester/ && mvn clean package
- Launch via command: $ java -jar run-MelbourneHarvester-1.0-SNAPSHOT.jar <IP of database> <index(optional)>
- Two log files will be created after first start. The log can trace the index for harvester recovery

##CBDStreetsHarvester
- Install via command: $ cd CBDStreetsHarvester/ && mvn clean package
- Launch via command: $ java -jar run-RoadHarvester-1.0-SNAPSHOT.jar <IP of database> <streetIndex(optional)>
- Two log files will be created after first start. The log can trace the index for harvester recovery

##MainHighwayHarvester
- Install via command: $ cd MainHighwayHarvester/ && mvn clean package
- Launch via command: $ java -jar run-MainStreetHavester-1.0-SNAPSHOT.jar <IP of database> <streetIndex(optional)>
- Two log files will be created after first start. The log can trace the index for harvester recovery

##RealTimeHarvester
- Install via command: $ cd MainHighwayHarvester/ && mvn clean package
- Launch via command: $ java -jar run-realtimeTweets-1.0-SNAPSHOT.jar
- I hardcode this harvester to send data to a tomcat7 server at the same host (127.0.0.1:8080). So the web server and this harvester need to be launched at the same server (order independent)

##WebService
- Install via command: $ cd WebService/ && mvn clean package
- Launch: Move the packaged web.war file to your the webapp/ folder under your tomcat7 directory. Launch your tomcat, by default the index page can be accessed at http://localhost:8080/web
 
##PSMA_Preprocessor
- Install via command: $ cd PSMA_Preprocessor/ && mvn clean package
- This application is used to read PSMA data and produce a metadata file as an input for Streets Harvester. For now, I hardcode the input file path. It will be more flexible in the future.









