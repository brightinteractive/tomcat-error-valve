Tomcat Error Valve
==================

There are 3 paths to get a 404 in tomcat:

1) http://localhost/asset-bank/broken_url - this 404 can be handled by the running webapp

To customise:
Define an error-page node in webapp/asset-bank/WEB-INF/web.xml

2) http://localhost/broken_url - where webapp by this name does not exist

Top customise:
Define an error-page node in tomcat_home/conf/web.xml (referenced file is relative to in webapp/ROOT)

3) http://localhost/asset-bank - where the webapp has been stopped but does exist

To customise:
There is no simple way to achieve this. This project aims to resolve this.

Build
=====

mvn clean package (creates a jar in 'target')

Configuration
=============

- Copy jar from target

to tomcat_home/lib

- Put tomcat-error-valve.properties in tomcat_home/conf

- Put static html file in an accessible location.

- Edit tomcat-error-valve.properties and enter location of html

- Restart tomcat
