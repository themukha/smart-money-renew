FROM gradle:jdk21 AS build

WORKDIR /app

COPY --chown=gradle:gradle . /app

RUN gradle buildFatJar --no-daemon

FROM openjdk:21-jdk

WORKDIR /app

COPY create_env_file.sh /app/

ARG JWT_SECRET
ARG DB_USERNAME
ARG DB_URL
ARG DB_PASSWORD
ARG KTOR_ENV

ENV KEYS="JWT_SECRET DB_USERNAME DB_URL DB_PASSWORD KTOR_ENV"

RUN chmod +x /app/create_env_file.sh && /app/create_env_file.sh

ENV JWT_SECRET=$JWT_SECRET
ENV DB_USERNAME=$DB_USERNAME
ENV DB_URL=$DB_URL
ENV DB_PASSWORD=$DB_PASSWORD
ENV KTOR_ENV=$KTOR_ENV

COPY --from=build /app/build/libs/smart-money-renew-all.jar /app/smart-money-renew-all.jar

EXPOSE 8080

CMD ["java", "-jar", "smart-money-renew-all.jar"]