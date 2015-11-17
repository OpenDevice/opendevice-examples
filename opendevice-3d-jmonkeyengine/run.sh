#!/usr/bin/env bash

CURDIR=`pwd`
JARNAME=opendevice-3d-jmonkeyengine-app.jar

if [ ! -x /usr/bin/java ]; then
   echo "Please install java" >&2
   exit 1
fi

if [ ! -x /usr/bin/mvn ]; then
   echo "Maven not instaled (sudo apt-get install maven)" >&2
   exit 1
fi

if [ ! -x $CURDIR/$JARNAME  ]; then
	echo "Compiling..."
	mvn package
	cp $CURDIR/target/$JARNAME $CURDIR/$JARNAME
fi

java -splash:./assets/splash.png -jar $CURDIR/$JARNAME 


