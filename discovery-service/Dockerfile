FROM adoptopenjdk/openjdk11:alpine-jre
EXPOSE 8761
# Refer to Maven build -> finalName
ARG JAR_FILE=target/discovery-service.jar
## Environmental variable
#ENV APP_HOME = /opt/ecms/eodEngine
## This is the directory where the output of CMD should run : cd /opt/ecms/eodEngine
#WORKDIR $APP_HOME
# Copy files from a specific location into a Docker image
COPY ${JAR_FILE} discovery-service.jar
ENTRYPOINT ["java", "-jar","/discovery-service.jar"]