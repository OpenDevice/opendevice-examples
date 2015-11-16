FROM andreptb/maven
USER root
ADD start.sh /start.sh
RUN apt-get -y install git
RUN cd / && mkdir opendevice && cd opendevice && git clone https://github.com/Sharathmk99/Docker-Opendevice-Sample.git\
	&& cd Docker-Opendevice-Sample && mvn clean && mvn package
ENTRYPOINT ["sh", "/start.sh"]
