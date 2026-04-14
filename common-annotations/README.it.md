# common-annotations

> **Modulo:** `com.github.bld-commons:common-annotations`
> **Versione:** 2.2.0
> **Parent:** `dev-commons`

## 🇬🇧 [Read in English](README.md)

---

## Descrizione

`common-annotations` è un modulo libreria Spring Boot che abilita la **dependency injection (DI) di Spring all'interno dei serializer e deserializer Jackson personalizzati**.

Per impostazione predefinita, Jackson istanzia i propri handler (serializer, deserializer, type handler, ecc.) autonomamente, bypassando completamente il contesto Spring. Ciò significa che qualsiasi campo `@Autowired` all'interno di un handler personalizzato non viene mai iniettato e rimane `null` a runtime, provocando `NullPointerException` difficili da diagnosticare.

Questo modulo risolve il problema registrando uno `SpringHandlerInstantiator` — il ponte tra il ciclo di vita degli handler di Jackson e l'`AutowireCapableBeanFactory` di Spring — applicato sia all'`ObjectMapper` primario di Spring Boot (tramite un `Jackson2ObjectMapperBuilderCustomizer`) sia a converter JSON e YAML dedicati. Il supporto YAML viene attivato automaticamente quando `jackson-dataformat-yaml` è sul classpath.

---

## Prerequisiti / Dipendenze

| Dipendenza | Scope |
|---|---|
| `org.springframework.boot:spring-boot-starter-validation` | compile |
| `org.springframework:spring-web` | compile |
| `org.apache.commons:commons-lang3` | compile |
| `org.apache.commons:commons-collections4` | compile |
| `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` | compile / runtime |
| `org.bouncycastle:bcprov-jdk18on` | compile |

Il modulo è gestito dal BOM del parent `dev-commons`; nei progetti consumer non è necessario dichiarare la versione esplicitamente.

---

## Come Abilitarlo

Aggiungere la dipendenza nel `pom.xml`:

```xml
<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-annotations</artifactId>
</dependency>
```

Poi annotare qualsiasi classe Spring `@Configuration` con `@EnableContextAnnotation`:

```java
@Configuration
@EnableContextAnnotation
public class AppConfig {
    // nient'altro necessario
}
```

L'annotazione attiva l'importazione di `EnableContextAnnotationConfiguration`, che registra automaticamente tutti i bean.

---

## Il Problema che Questo Modulo Risolve

Si consideri un deserializer Jackson personalizzato che ha bisogno di un servizio gestito da Spring:

```java
@JsonComponent
public class ProductDeserializer extends JsonDeserializer<Product> {

    @Autowired          // <-- iniettato solo se la DI di Spring è abilitata per Jackson
    private ProductService productService;

    @Override
    public Product deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String id = p.getText();
        return productService.findById(id);   // NullPointerException senza questo modulo!
    }
}
```

Senza `common-annotations`, Jackson crea `ProductDeserializer` invocando direttamente il suo costruttore no-arg. Spring non elabora mai l'istanza, quindi `productService` è `null`.

Con `@EnableContextAnnotation`, Jackson delega l'istanziazione allo `SpringHandlerInstantiator`, che a sua volta chiede all'`AutowireCapableBeanFactory` di Spring di creare l'istanza. Spring inietta tutte le dipendenze prima che l'handler venga utilizzato.

---

## Esempio: Deserializer con un Service Spring Iniettato

```java
// 1. Abilitare il modulo nella propria configurazione
@Configuration
@EnableContextAnnotation
public class JacksonConfig { }

// 2. Scrivere un deserializer che usa @Autowired normalmente
@JsonComponent
public class OrderDeserializer extends JsonDeserializer<Order> {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Order deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String orderId = p.getValueAsString();
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Ordine non trovato: " + orderId));
    }
}
```

Non è richiesta nessuna configurazione aggiuntiva. Il bean `OrderRepository` viene iniettato automaticamente.

---

## Bean Registrati Automaticamente

| Nome bean | Tipo | Descrizione |
|---|---|---|
| `contextHandlerInstantiator` | `HandlerInstantiator` | Collega il ciclo di vita degli handler di Jackson all'`AutowireCapableBeanFactory` di Spring. È il bean principale che abilita `@Autowired` all'interno degli handler. |
| *(customizer)* | `Jackson2ObjectMapperBuilderCustomizer` | Applica `SpringHandlerInstantiator` all'`ObjectMapper` **primario** di Spring Boot, in modo che anche il converter JSON predefinito inietti correttamente gli handler. |
| `contextJackson2ObjectMapperBuilder` | `Jackson2ObjectMapperBuilder` | Un mapper builder completamente configurato che applica l'instantiator Spring-aware e tutti i bean `Jackson2ObjectMapperBuilderCustomizer` registrati. |
| `contextMappingJackson2HttpMessageConverter` | `MappingJackson2HttpMessageConverter` | Converter HTTP JSON costruito dal mapper builder personalizzato. |
| `contextYamlMappingJackson2HttpMessageConverter` | `MappingJackson2HttpMessageConverter` | Converter HTTP YAML (registrato solo se `jackson-dataformat-yaml` è sul classpath). Supporta `application/yaml`, `application/x-yaml`, `text/yaml`. |

---

## Struttura dei Package

```
com.bld.context.annotation.config
├── EnableContextAnnotation.java           — annotazione di attivazione (meta-annotazione @Configuration)
└── EnableContextAnnotationConfiguration.java — configurazione Spring che registra i tre bean
```

---

## Licenza

Consultare il progetto radice `dev-commons` per le informazioni sulla licenza.
