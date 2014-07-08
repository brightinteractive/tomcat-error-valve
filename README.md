Tomcat Error Valve
==================

Introduction
------------

There are 3 paths to get a 404 in tomcat:

1) _http://localhost/asset-bank/broken_url_ - this 404 can be handled by the running webapp

To customise:

Define an error-page node in _webapp/asset-bank/WEB-INF/web.xml_

2) _http://localhost/broken_url_ - where webapp by this name does not exist

Top customise:

Define an error-page node in _tomcat_home/conf/web.xml_ (referenced file is relative to in _webapp/ROOT_)

3) _http://localhost/asset-bank_ - where the webapp has been stopped but does exist

To customise:

There is no simple way to achieve this. This project aims to resolve this.

Build
-----

`mvn clean package`

creates a jar in _target_

Configuration
-------------

- Copy jar from _target_ to _tomcat_home/lib_

- Put tomcat-error-valve.properties in _tomcat_home/conf_

- Put static html file in an accessible location.

- Edit _tomcat-error-valve.properties_ and set _html.path_ property to the html page to serve

- Restart tomcat
