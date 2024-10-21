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

## Add new alternative products mapping files

New mappings may be added to the database by adding new files to the `src/main/resources/substituttlister` directory.
The files should be named `V<version-number>_<filename>.xlsx` and have the following format. The version number should
be increased by one for each new file.

The new file must also be added to the enum: SubstituteFiles.kt

### Grouping products

HMS numbers that should be grouped together should be in subsequent rows without any empty rows in between.

### Removing mappings

To remove a mapping, add rows with the HMS-numbers that should no longer be mapped together and put an 'x' in column G. 
