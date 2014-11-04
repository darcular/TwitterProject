##PSMA_Preprocessor
- Install via command: $ cd PSMA_Preprocessor/ && mvn clean package
- This application is used to read PSMA data and produce a metadata file as an input for Streets Harvester. For now, I hardcode the input file path. It will be more flexible in the future.
 

##Description

This application aim to read PSMA data in JSON format. I got these PSMA data (see the resource folder) from ARIN (http://aurin.org.au/). The output are also in JSON format and can be used by Streets Harvesters (e.g. CBDStreetsHarvester and MainHighwayHarvester).
