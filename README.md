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

##Directory Structure
There are four Twitter Harvesters, one PSMA data preprocessor and one WebService in this directory. All these applications are managed respectively by maven and each sub-directory contains a ReadMe file for the details of that application.

##Melbourne Harvester
This application is used for harvesting historical tweets data from Twitter RESTful API.
- Install via command: <code>$ cd MelbourneHarvester/ && mvn clean package</code>
- Launch via command: 




