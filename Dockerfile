FROM openjdk:17-jdk-slim

WORKDIR /app

COPY .env /app/.env

COPY target/ozon-1.0-SNAPSHOT.jar /app/ozon-1.0-SNAPSHOT.jar

ENV BOT_TOKEN=${BOT_TOKEN}
ENV OZON_CLIENT_ID=${OZON_CLIENT_ID}
ENV OZON_API=${OZON_API}

CMD ["java", "-jar", "ozon-1.0-SNAPSHOT.jar"]
