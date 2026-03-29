# common-encryption

> **Module:** `com.github.bld-commons:common-encryption`
> **Version:** 2.1.4
> **Parent:** `dev-commons`

A Spring Boot encryption framework that provides **transparent field-level encryption and decryption** during JSON serialization and deserialization via Jackson annotations. Supports AES symmetric encryption, RSA with PEM public keys, RSA with JKS keystores, and digital signatures — all configured through `application.properties`.

---

## Table of contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Enabling the module](#enabling-the-module)
4. [AES encryption](#aes-encryption)
5. [RSA public key encryption](#rsa-public-key-encryption)
6. [JKS keystore encryption](#jks-keystore-encryption)
7. [URL-safe encoding](#url-safe-encoding)
8. [Digital signatures](#digital-signatures)
9. [Programmatic API](#programmatic-api)
10. [Encryption types reference](#encryption-types-reference)
11. [Package structure](#package-structure)
12. [Italiano](#italiano)

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.x |

Key transitive dependencies:

| Dependency | Purpose |
|---|---|
| `org.bouncycastle:bcprov-jdk18on` | Cryptography provider (AES, RSA, EC) |
| `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` | JWT support |
| `common-annotations` (this project) | Spring DI in Jackson handlers |

---

## Installation

```xml
<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-encryption</artifactId>
    <version>2.1.4</version>
</dependency>
```

If you import the `dev-commons` BOM in `dependencyManagement`, omit the `<version>` tag.

---

## Enabling the module

Annotate any Spring `@Configuration` class with `@EnableCrypto`:

```java
@Configuration
@EnableCrypto
public class AppConfig {
}
```

This activates `CryptoConfiguration`, which itself imports `@EnableContextAnnotation` (from `common-annotations`) so that all encryption handlers can use Spring-managed beans.

---

## AES encryption

AES symmetric encryption. The same key is used for encryption (serialization) and decryption (deserialization).

### Configuration

```properties
# application.properties
com.bld.crypto.aes.keys.myAesKey=your-base64-encoded-aes-secret
```

Multiple named keys are supported:

```properties
com.bld.crypto.aes.keys.passwordKey=<base64-secret-1>
com.bld.crypto.aes.keys.tokenKey=<base64-secret-2>
```

### Annotation usage

```java
public class UserEntity {
    @CryptoAes("passwordKey")
    private String password;

    @CryptoAes("tokenKey")
    private String apiToken;
}
```

The field is encrypted when the object is serialized to JSON and decrypted when deserialized — completely transparent to application code.

---

## RSA public key encryption

Asymmetric RSA encryption using a PEM-encoded public key. Encryption uses the public key; decryption requires the corresponding private key (managed externally).

### Configuration

```properties
# application.properties — PEM inline (use \n for line breaks)
com.bld.crypto.pubkey.keys.myPublicKey=-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkq...\n-----END PUBLIC KEY-----
```

### Annotation usage

```java
public class ApiRequest {
    @CryptoPubKey("myPublicKey")
    private String clientSecret;
}
```

---

## JKS keystore encryption

RSA encryption backed by a Java KeyStore (JKS) file. You can independently choose which key type (public or private) is used for encryption and which for decryption, giving full control over the asymmetric workflow.

### Configuration

```properties
# application.properties
com.bld.crypto.jks.keys.myJksKey.path=/etc/keystore/app.jks
com.bld.crypto.jks.keys.myJksKey.password=keystorePassword
com.bld.crypto.jks.keys.myJksKey.alias=myKeyAlias
```

### Annotation usage

```java
public class SecurePayload {
    // encrypt with public key, decrypt with private key (standard asymmetric flow)
    @CryptoJks(value = "myJksKey",
               encrypt = CryptoType.publicKey,
               decrypt = CryptoType.privateKey)
    private String sensitiveData;
}
```

`CryptoType` values: `publicKey`, `privateKey`.

---

## URL-safe encoding

All three annotations support a `url = true` parameter that applies double Base64 encoding with URL-safe characters. Use this when the encrypted value will appear in a URL query parameter or path segment.

```java
public class SearchRequest {
    @CryptoAes(value = "tokenKey", url = true)
    private String encryptedFilter;

    @CryptoPubKey(value = "myPublicKey", url = true)
    private String encryptedId;

    @CryptoJks(value = "myJksKey",
               encrypt = CryptoType.publicKey,
               decrypt = CryptoType.privateKey,
               url = true)
    private String encryptedCode;
}
```

---

## Digital signatures

`SignatureKeyUtils` provides signature generation and verification backed by a JKS keystore.

### Configuration

```properties
com.bld.crypto.signature.keys.mySignKey.path=/etc/keystore/sign.jks
com.bld.crypto.signature.keys.mySignKey.password=keystorePassword
com.bld.crypto.signature.keys.mySignKey.alias=signAlias
```

### Usage

```java
@Autowired
private SignatureKeyUtils signatureKeyUtils;

// Sign
byte[] signature = signatureKeyUtils.sign(dataBytes, "mySignKey");

// Verify
boolean valid = signatureKeyUtils.verify(dataBytes, signature, "mySignKey");
```

---

## Programmatic API

All utilities can be injected and used directly without annotations.

### AES

```java
@Autowired
private CryptoAesUtils aesUtils;

String encrypted    = aesUtils.encryptValue(plaintext, "passwordKey");
String decrypted    = aesUtils.decryptValue(encrypted, "passwordKey");

// URL-safe variant
String urlEncrypted = aesUtils.encryptUri(plaintext, "tokenKey");
String urlDecrypted = aesUtils.decryptUri(urlEncrypted, "tokenKey");
```

### RSA public key

```java
@Autowired
private CryptoPublicKeyUtils pubKeyUtils;

String encrypted    = pubKeyUtils.encryptValue(plaintext, "myPublicKey");
String decrypted    = pubKeyUtils.decryptValue(encrypted, "myPublicKey");
String urlEncrypted = pubKeyUtils.encryptUri(plaintext, "myPublicKey");
```

### JKS

```java
@Autowired
private CryptoJksUtils jksUtils;

String encrypted    = jksUtils.encryptValue(plaintext, "myJksKey");
String decrypted    = jksUtils.decryptValue(encrypted, "myJksKey");
```

---

## Encryption types reference

| Annotation | Algorithm | Key configuration prefix | Symmetric |
|---|---|---|---|
| `@CryptoAes` | AES | `com.bld.crypto.aes.keys` | Yes |
| `@CryptoPubKey` | RSA | `com.bld.crypto.pubkey.keys` | No |
| `@CryptoJks` | RSA | `com.bld.crypto.jks.keys` | No |

| Enum | Values | Description |
|---|---|---|
| `CryptoType` | `publicKey`, `privateKey` | Key role in JKS operations |
| `InstanceType` | `AES`, `RSA`, `EC` | Cipher algorithm |
| `AesSizeType` | `AES_128`, `AES_192`, `AES_256` | AES key size |

---

## Package structure

```
com.bld.crypto
├── aes/
│   ├── annotation/CryptoAes.java                    — field annotation
│   ├── CryptoAesUtils.java                          — programmatic API
│   ├── config/
│   │   ├── AesConfiguration.java
│   │   ├── AesConditional.java
│   │   ├── AesFormatterConfiguration.java
│   │   └── data/
│   │       ├── Aes.java
│   │       ├── AesProperties.java
│   │       └── CipherAesSecret.java
│   ├── deserializer/DecryptAesDeserializer.java
│   ├── serializer/EncryptAesSerializer.java
│   └── formatter/
│       ├── CryptoAesFormatter.java
│       └── CryptoAesAnnotationFormatterFactory.java
├── pubkey/
│   ├── annotations/
│   │   ├── CryptoPubKey.java                        — field annotation
│   │   ├── DecryptPubKey.java
│   │   └── EncryptPubKey.java
│   ├── CryptoPublicKeyUtils.java                    — programmatic API
│   ├── CryptoMapPublicKeyUtils.java
│   ├── config/
│   │   ├── CryptoPublicKeyConfiguration.java
│   │   ├── PubKeyConditional.java
│   │   ├── PubKeyFormatterConfiguration.java
│   │   └── data/
│   │       ├── PublicKeyProperties.java
│   │       └── CipherPublicKeys.java
│   ├── deserializer/DecryptPubKeyDeserializer.java
│   ├── serializer/EncryptPubKeySerializer.java
│   └── formatter/
│       ├── CryptoPubKeyFormatter.java
│       └── CryptoPubKeyAnnotationFormatterFactory.java
├── jks/
│   ├── annotation/CryptoJks.java                    — field annotation
│   ├── CryptoJksUtils.java                          — programmatic API
│   ├── config/
│   │   ├── CryptoJksConfiguration.java
│   │   ├── JksFormatterConfiguration.java
│   │   └── properties/JksKeyProperties.java
│   ├── deserializer/DecryptJksDeserializer.java
│   ├── serializer/EncryptJksSerializer.java
│   └── formatter/
│       ├── CryptoJksFormatter.java
│       └── CryptoJksAnnotationFormatterFactory.java
├── signature/
│   ├── SignatureKeyUtils.java
│   └── config/
│       ├── SignatureConfiguration.java
│       └── properties/SignatureKeyProperties.java
├── bean/
│   ├── CryptoUtils.java
│   ├── CryptoKeyUtils.java                          — abstract base with cipher ops
│   └── CryptoKeyData.java
├── commons/KeyUtility.java
├── config/
│   ├── CryptoConfiguration.java
│   └── annotation/EnableCrypto.java
├── exception/CryptoException.java
├── formatter/CryptoFormatter.java                   — abstract base formatter
├── deserializer/DecryptCertificateDeserializer.java
├── serializer/EncryptCertificateSerializer.java
└── type/
    ├── CryptoType.java
    ├── InstanceType.java
    └── AesSizeType.java
```

---

## License

See the root [`dev-commons`](../README.md) project for license information (MIT).

---

---

## Italiano

Framework di cifratura per Spring Boot che fornisce **cifratura e decifratura trasparente dei campi JSON** tramite annotazioni Jackson. Supporta AES simmetrico, RSA con chiavi PEM pubbliche, RSA con keystore JKS e firme digitali — tutto configurabile via `application.properties`.

### Abilitazione

```java
@Configuration
@EnableCrypto
public class AppConfig { }
```

### Tipi di cifratura

| Annotazione | Algoritmo | Descrizione |
|---|---|---|
| `@CryptoAes` | AES | Cifratura simmetrica; stessa chiave per cifrare e decifrare |
| `@CryptoPubKey` | RSA | Cifratura asimmetrica con chiave pubblica PEM |
| `@CryptoJks` | RSA | Cifratura asimmetrica con chiave da keystore JKS |

### Configurazione AES

```properties
com.bld.crypto.aes.keys.myAesKey=<segreto-base64>
```

### Utilizzo delle annotazioni

```java
public class UserEntity {
    @CryptoAes("myAesKey")
    private String password;            // cifrato nel JSON, decifrato in lettura

    @CryptoPubKey(value = "myPubKey", url = true)
    private String apiToken;            // cifrato con codifica URL-safe

    @CryptoJks(value = "myJksKey",
               encrypt = CryptoType.publicKey,
               decrypt = CryptoType.privateKey)
    private String sensitiveData;       // flusso asimmetrico standard
}
```

### API programmatica

È possibile iniettare direttamente `CryptoAesUtils`, `CryptoPublicKeyUtils` o `CryptoJksUtils` per cifrare e decifrare valori senza usare le annotazioni.

### Firme digitali

`SignatureKeyUtils` consente di firmare e verificare dati binari tramite una chiave in un keystore JKS configurato con il prefisso `com.bld.crypto.signature.keys`.
