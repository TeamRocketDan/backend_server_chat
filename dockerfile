FROM --platform=linux/amd64 openjdk:11
COPY build/libs/teamRocket-0.0.1-SNAPSHOT.jar teamRocket-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "teamRocket-0.0.1-SNAPSHOT.jar"]