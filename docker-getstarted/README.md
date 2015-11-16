Note: Assuming docker is installed

To Build Dockerfile use below command
docker build -t opendevice-docker-sample .

To pull from Docker Hub
docker pull sharathmk99/opendevice-docker-sample

To Run the Getstarted docker container
docker run -d sharathmk99/opendevice-docker-sample

Before running please connect the arduino to the system, arduino should have Opendevice USB example code 

The Docker is pre configured with java, maven, git and sample led blink java code of opendevice api via usb