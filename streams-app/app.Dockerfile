#FROM eclipse-temurin:17.0.6_10-jre-alpine AS builder
#WORKDIR build

#RUN cat > /usr/local/share/ca-certificates/repo-maven-apache-org.crt
#RUN cat /usr/local/share/ca-certificates/repo-maven-apache-org.crt >> /etc/ssl/certs/ca-certificates.crt
#RUN update-ca-certificates
#RUN keytool -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts2 -storepass changeit -importcert -alias maven -file test.crt 

#RUN ./mvnw clean install -DskipTests

FROM eclipse-temurin:17.0.6_10-jre-alpine AS layers
WORKDIR layer
COPY target/streams-app-0.0.1.jar ./app.jar
#COPY --from=builder /build/target/streams-app-0.0.1.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17.0.6_10-jre-jammy
RUN apt install libstdc++6
WORKDIR /opt/app
RUN adduser --system --shell /usr/sbin/nologin --group appuser
COPY --from=layers /layer/dependencies/ ./
COPY --from=layers /layer/spring-boot-loader/ ./
COPY --from=layers /layer/snapshot-dependencies/ ./
COPY --from=layers /layer/application/ ./
RUN chown -R appuser:appuser /opt/app
USER root
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]