FROM adoptopenjdk/openjdk11:alpine-jre
EXPOSE 8055
# Refer to Maven build -> finalName
ARG JAR_FILE=target/eod-engine-producer.jar
## Environmental variable
#ENV APP_HOME = /opt/ecms/eodEngine
## This is the directory where the output of CMD should run : cd /opt/ecms/eodEngine
#WORKDIR $APP_HOME
# Copy files from a specific location into a Docker image
COPY ${JAR_FILE} eod-engine-producer.jar
ENTRYPOINT ["java", "-jar","/eod-engine-producer.jar"]