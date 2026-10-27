FROM eclipse-temurin:25-jre-alpine
RUN apk update && \
    apk add --no-cache --upgrade brotli-libs
WORKDIR /app
ENV TZ="Europe/Oslo"
EXPOSE 8080
COPY build/libs/hm-grunndata-alternativprodukter-all.jar ./app.jar
ADD src/main/resources/substituttlister substituttlister
USER 1000
CMD ["java", "-jar", "app.jar"]
