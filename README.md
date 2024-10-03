# hm-grunndata-alternativprodukter

Database med alternativprodukter

## Running hm-grunndata-alternativprodukter locally:

```
cd hm-grunndata-alternativprodukter
docker-compose up -d

export DB_DRIVER=org.postgresql.Driver
export DB_JDBC_URL=jdbc:postgresql://localhost:5435/alternativprodukter
export SERVER_PORT=1338

./gradlew build run
```

Example call to get alternative products: 
```
http://localhost:1338/alternativ/147286
```