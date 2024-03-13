#FROM eclipse-temurin:17.0.6_10-jre-alpine AS builder
#WORKDIR build

#RUN cat > /usr/local/share/ca-certificates/repo-maven-apache-org.crt
#RUN cat /usr/local/share/ca-certificates/repo-maven-apache-org.crt >> /etc/ssl/certs/ca-certificates.crt
#RUN update-ca-certificates
#RUN keytool -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts2 -storepass changeit -importcert -alias maven -file test.crt 

#RUN ./mvnw clean install -DskipTests

FROM eclipse-temurin:17.0.6_10-jre-jammy
RUN apt install libstdc++6
WORKDIR /opt/app
COPY target/data-faker-0.0.1.jar ./app.jar
USER root
ENTRYPOINT ["java", "-jar", "./app.jar"]