RSSISimilarityWeb
=================
Indoor Localization service
---------------------------
>
RSSI-based indoor localization system developed in this project utilizes wireless sensor nodes such as access points (APs) or any other sensor modalities that are capable of generating a Receive Signal Strength Indication (RSSI), which can be used to determine the location of a target object in an indoor environment. A device with an RFID tag or a smartphone with a wireless antenna scans the RSSIs from surrounding active APs, and sends the recorded values to the server using HTTP communication protocol. The ‘Localization service’ deployed on the server receives the RSSIs, extracts location related information and delivers to the component that has similarity matching algorithmic implementation. A room or an area with the most similar RSSIs from the database that has pre-gathered RSSI samples stored in is returned to the client/visitor as a current estimated location.
<

### Responsibilities:
* Design and Implement part of the Localization service containing a Similarity Matching
algorithm API called OBSearch.
* Implement HTTP Request/Response handling components in the web layer using
servlets technology for sending signal related data to the Localization Service and
receiving estimated location name from the service.
* Develop an Android client application that scans signal strength values (RSSIs) from
surrounding APs and sends to the Localization Service, on the flip-side also receives
responses (i.e. estimated locations) sent from the service.
* Implement JUint test on the localization service and deploy the system to Apache web
container.
* Design use case model and write project documentation, which was also included in my
Bachelor thesis.

### Development platform and tools used: 
1. JavaSE, J2EE (Servlet API)
2. Android SDK
3. OBSearch similarity index search API
4. MVC
5. jQuery
6. JSON
7. HTML
8. CouchDB (document-based database)
9. Apache tomcat web container
10. Eclipse
11. Github and eGitrepository
