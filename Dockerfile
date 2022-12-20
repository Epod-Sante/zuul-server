FROM gradle:5.6.2-jdk11 as java-build
EXPOSE 8762
WORKDIR /opt/pod-isante/zuul
COPY build/libs/zuul-server-0.0.1-SNAPSHOT.jar zuul-service.jar
ENTRYPOINT ["java","-jar","/opt/pod-isante/zuul/zuul-service.jar"]

#docker pull lahcenezinnour/zuul-docker-img:latest
# docker run -p 8762:8762 -t lahcenezinnour/zuul-docker-img:latest
#docker build -t zuul-docker-img .
#
#docker tag zuul-docker-img lahcenezinnour/zuul-docker-img
#
#docker push lahcenezinnour/zuul-docker-img