FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app
ENV TZ="Europe/Oslo"
EXPOSE 8080
COPY build/libs/hm-grunndata-alternativprodukter-all.jar ./app.jar
ADD src/main/resources/substituttlister substituttlister
CMD ["-jar", "app.jar"]