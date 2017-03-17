#!/bin/sh -
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address="8000" -cp mozabackendserver.jar:libs/* Main -p 8081