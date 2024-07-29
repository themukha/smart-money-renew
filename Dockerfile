FROM openjdk:21-jdk

WORKDIR /app

COPY build/libs/smart-money-renew-all.jar /app/smart-money-renew-all.jar

EXPOSE 8080

CMD ["java", "-jar", "smart-money-renew-all.jar"]