# bld-common-utils

> **Module:** `com.github.bld-commons:bld-common-utils`
> **Version:** 2.2.0
> **Parent:** `dev-commons`

A general-purpose Spring Boot utility library providing date/time handling, string transformations, custom Jackson annotations, JSR-380 validators, geometry data models (PostGIS, WKT, WKB, KML, GeoJSON), and Spring formatter factories — all activated with a single `@EnableCommonUtils` annotation.

---

## Table of contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Enabling the module](#enabling-the-module)
4. [Date utilities](#date-utilities)
5. [JSON annotations](#json-annotations)
   - [@DateTimeZone](#datetimezone)
   - [@DateChange](#datechange)
   - [@UpperLowerCase](#upperlowercase)
   - [@TextClob](#textclob)
   - [@Base64File](#base64file)
   - [@GeometryPostgis](#geometrypostgis)
   - [@CleanExcessSpaces](#cleanexcessspaces)
6. [Validators](#validators)
7. [Geometry data models](#geometry-data-models)
8. [Data models](#data-models)
9. [Enumerations](#enumerations)
10. [Spring formatters](#spring-formatters)
11. [Package structure](#package-structure)
12. [Italiano](#italiano)

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.x |

Key transitive dependencies included automatically:

| Dependency | Purpose |
|---|---|
| `org.locationtech.jts:jts-core` | Geometry operations (WKT, WKB) |
| `org.apache.commons:commons-text` | String utilities |
| `org.apache.commons:commons-lang3` | General language utilities |
| `org.apache.commons:commons-beanutils` | Bean property utilities |
| `org.apache.commons:commons-collections4` | Collection utilities |

---

## Installation

```xml
<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>bld-common-utils</artifactId>
    <version>2.2.0</version>
</dependency>
```

If you import the `dev-commons` BOM in `dependencyManagement`, omit the `<version>` tag.

---

## Enabling the module

Annotate any Spring `@Configuration` class with `@EnableCommonUtils`:

```java
@SpringBootApplication
@EnableCommonUtils
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

This triggers component scanning for `com.bld.commons.utils` and registers all annotation-based Spring formatter factories in the MVC `FormatterRegistry`.

---

## Date utilities

`DateUtils` provides static helper methods for all common date/time operations.

```java
// Current date/time
Date now    = DateUtils.currentDate();   // with time component
Date today  = DateUtils.today();         // time reset to 00:00:00

// Conversions
String s    = DateUtils.dateToString(now, "yyyy-MM-dd HH:mm:ss");
Date   d    = DateUtils.stringToDate("2026-03-30", "yyyy-MM-dd");
Timestamp t = DateUtils.dateToTimestamp(d);
Calendar  c = DateUtils.dateToCalendar(d);

// Reset time component
Date startOfDay = DateUtils.resetHour(d);

// Arithmetic
Date tomorrow     = DateUtils.sumDate(today, 1, TimeUnitMeasureType.DAY);
Date nextHour     = DateUtils.sumDate(now, 1, TimeUnitMeasureType.HOUR);
Long daysBetween  = DateUtils.differneceDate(d1, d2, TimeUnitMeasureType.DAY);
Long hoursBetween = DateUtils.differneceDate(d1, d2, TimeUnitMeasureType.HOUR);

// Component extraction
int year  = DateUtils.getYear(d);
int month = DateUtils.getMonth(d);   // 1-based
int day   = DateUtils.getDay(d);
```

`TimeUnitMeasureType` values: `MILLISECOND`, `SECOND`, `MINUTE`, `HOUR`, `DAY`.

---

## JSON annotations

All annotations work at field level on Jackson-serialized/deserialized objects. They require `@EnableContextAnnotation` (from `common-annotations`) to be active so that the underlying handlers are Spring-managed.

### @DateTimeZone

Applies a specific timezone during JSON date formatting. The format defaults to the Spring property `spring.jackson.date-format` if not specified.

```java
public class EventDto {
    @DateTimeZone("Europe/Rome")
    private Date startAt;

    @DateTimeZone(value = "UTC", format = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date endAt;
}
```

### @DateChange

Converts dates between timezones or changes the date format on deserialization.

```java
public class ReportDto {
    @DateChange(sourceFormat = "dd/MM/yyyy", targetFormat = "yyyy-MM-dd")
    private Date reportDate;
}
```

### @UpperLowerCase

Converts a `String` field to upper case, lower case, or first-letter upper case automatically during JSON deserialization.

```java
public class UserDto {
    @UpperLowerCase(UpperLowerType.UPPER)
    private String fiscalCode;       // always stored in upper case

    @UpperLowerCase(UpperLowerType.LOWER)
    private String email;            // always stored in lower case

    @UpperLowerCase(UpperLowerType.FIRST_UPPER)
    private String firstName;        // "alice" → "Alice"
}
```

`UpperLowerType` values: `UPPER`, `LOWER`, `FIRST_UPPER`.

### @TextClob

Handles `java.sql.Clob` fields — reads the Clob content as a plain `String` during deserialization and converts back on serialization.

```java
public class DocumentDto {
    @TextClob
    private String content;   // backed by a database CLOB column
}
```

### @Base64File

Encodes a `byte[]` field to a Base64 string on serialization and decodes it back on deserialization.

```java
public class AttachmentDto {
    @Base64File
    private byte[] fileData;
}
```

### @GeometryPostgis

Deserializes and serializes geometry fields in various spatial formats.

```java
public class LocationDto {
    @GeometryPostgis(SpatialType.POSTGIS)
    private PostgisGeometry location;    // PostGIS WKB hex

    @GeometryPostgis(SpatialType.WKT)
    private WKTGeometry boundary;        // Well-Known Text

    @GeometryPostgis(SpatialType.GEOJSON)
    private GeoJsonGeometry area;        // GeoJSON
}
```

`SpatialType` values: `POSTGIS`, `WKB`, `WKT`, `KML`, `GEOJSON`.

### @CleanExcessSpaces

Removes leading, trailing, and internal duplicate whitespace from a `String` field on deserialization.

```java
public class AddressDto {
    @CleanExcessSpaces
    private String streetName;   // "  Via   Roma  " → "Via Roma"
}
```

---

## Validators

Custom JSR-380 constraint annotations for whitelisting allowed values.

### @AllowedString

```java
public class ContractDto {
    @AllowedString({"ACTIVE", "INACTIVE", "PENDING"})
    private String status;
}
```

### @AllowedNumber

```java
public class RatingDto {
    @AllowedNumber({1, 2, 3, 4, 5})
    private Integer score;
}
```

Both annotations work with standard Bean Validation (`@Valid` / `@Validated`). A `ConstraintViolationException` is thrown when the value is not in the allowed set.

---

## Geometry data models

| Class | Format | Description |
|---|---|---|
| `PostgisGeometry` | PostGIS WKB hex | PostgreSQL/PostGIS native binary format |
| `WKBGeometry` | Well-Known Binary | OGC standard binary geometry encoding |
| `WKTGeometry` | Well-Known Text | OGC standard text geometry encoding (e.g. `POINT(9.19 45.46)`) |
| `KMLGeometry` | KML | Keyhole Markup Language geometry |
| `GeoJsonGeometry` | GeoJSON | RFC 7946 geometry object |

All geometry classes integrate with JTS (`org.locationtech.jts`) for in-memory operations.

---

## Data models

| Class | Description |
|---|---|
| `BaseModel` | Base class for domain model objects |
| `TypologicalModel` | Base class for typology/lookup entities |
| `CollectionResponse<T>` | Generic wrapper for paginated/list responses |
| `ObjectResponse<T>` | Generic wrapper for single-object responses |

---

## Enumerations

| Enum | Values | Description |
|---|---|---|
| `UpperLowerType` | `UPPER`, `LOWER`, `FIRST_UPPER` | String case transformation type |
| `SpatialType` | `POSTGIS`, `WKB`, `WKT`, `KML`, `GEOJSON` | Spatial data format |
| `MimeType` | Common MIME strings | Predefined MIME type constants |
| `TimeUnitMeasureType` | `MILLISECOND`, `SECOND`, `MINUTE`, `HOUR`, `DAY` | Time unit for date arithmetic |

---

## Spring formatters

When `@EnableCommonUtils` is active, the following `AnnotationFormatterFactory` beans are registered in the MVC `FormatterRegistry`. They apply the same transformations as the JSON annotations but for Spring data binding (form parameters, `@RequestParam`, etc.):

| Factory | Annotation | Field type |
|---|---|---|
| `DateTimeZoneAnnotationFormatterFactory` | `@DateTimeZone` | `Date`, `Calendar` |
| `DateFilterAnnotationFormatterFactory` | `@DateChange` | `Date`, `Calendar` |
| `UpperLowerAnnotationFormatterFactory` | `@UpperLowerCase` | `String` |
| `ClobAnnotationFormatterFactory` | `@TextClob` | `Clob` |

---

## Package structure

```
com.bld.commons.utils
├── CommonUtility.java                            — type-checking helpers
├── DateUtils.java                                — date/time operations
├── CamelCaseUtils.java                           — camel-case transformations
├── config/
│   ├── EnableCommonUtilsConfiguration.java       — Spring MVC configuration
│   └── annotation/EnableCommonUtils.java         — activation annotation
├── data/
│   ├── BaseModel.java
│   ├── TypologicalModel.java
│   ├── CollectionResponse.java
│   ├── ObjectResponse.java
│   ├── PostgisGeometry.java
│   ├── WKBGeometry.java
│   ├── WKTGeometry.java
│   ├── KMLGeometry.java
│   └── GeoJsonGeometry.java
├── formatter/
│   ├── DateTimeZoneAnnotationFormatterFactory.java
│   ├── DateFilterAnnotationFormatterFactory.java
│   ├── UpperLowerAnnotationFormatterFactory.java
│   ├── ClobAnnotationFormatterFactory.java
│   ├── DateFormatter.java
│   ├── UpperLowerFormatter.java
│   └── ClobFormatter.java
├── json/annotations/
│   ├── DateTimeZone.java
│   ├── DateChange.java
│   ├── UpperLowerCase.java
│   ├── TextClob.java
│   ├── Base64File.java
│   ├── GeometryPostgis.java
│   ├── CleanExcessSpaces.java
│   ├── deserialize/         — custom Jackson deserializers
│   └── serialize/           — custom Jackson serializers
├── types/
│   ├── UpperLowerType.java
│   ├── SpatialType.java
│   ├── MimeType.java
│   └── TimeUnitMeasureType.java
└── validator/
    ├── AllowedStringValidator.java
    ├── AllowedNumberValidator.java
    ├── AllowedValueValidator.java
    └── annotations/
        ├── AllowedString.java
        └── AllowedNumber.java
```

---

## License

See the root [`dev-commons`](../README.md) project for license information (MIT).

---

---

## Italiano

Libreria di utilità generali per Spring Boot che fornisce:

- **DateUtils** — oltre 30 metodi statici per conversioni, aritmetica, estrazione di componenti e gestione del fuso orario sulle date.
- **Annotazioni JSON** — annotazioni a livello di campo che trasformano automaticamente i valori durante la serializzazione/deserializzazione Jackson: `@DateTimeZone` (fuso orario), `@UpperLowerCase` (conversione maiuscolo/minuscolo), `@TextClob` (gestione CLOB), `@Base64File` (codifica binaria), `@GeometryPostgis` (geometrie spaziali), `@CleanExcessSpaces` (normalizzazione spazi bianchi), `@DateChange` (cambio formato data).
- **Validatori JSR-380** — `@AllowedString` e `@AllowedNumber` per validare che un campo contenga solo i valori consentiti.
- **Modelli geometrici** — classi dati per PostGIS, WKT, WKB, KML e GeoJSON, integrate con la libreria JTS.
- **Formatter Spring** — factory registrate automaticamente nel `FormatterRegistry` MVC per applicare le stesse trasformazioni ai parametri di form e di richiesta.

### Abilitazione

```java
@SpringBootApplication
@EnableCommonUtils
public class MyApplication { }
```

### Annotazioni JSON — esempi rapidi

```java
public class UserDto {
    @UpperLowerCase(UpperLowerType.UPPER)
    private String fiscalCode;               // convertito automaticamente in maiuscolo

    @DateTimeZone("Europe/Rome")
    private Date birthDate;                  // formattato nel fuso orario specificato

    @AllowedString({"ACTIVE", "INACTIVE"})
    private String status;                   // validazione whitelist

    @GeometryPostgis(SpatialType.WKT)
    private WKTGeometry location;            // geometria WKT
}
```
