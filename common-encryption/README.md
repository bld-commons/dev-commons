# common-encryption

> **Module:** `com.github.bld-commons:common-encryption`
> **Version:** 2.1.4
> **Parent:** `dev-commons`

A Spring Boot encryption framework that provides **transparent field-level encryption and decryption** during JSON serialization and deserialization via Jackson annotations. Supports AES symmetric encryption, RSA with PEM public keys, RSA with JKS keystores, X25519 + AES-256-GCM hybrid encryption via PKCS12, and digital signatures — all configured through `application.properties`.

---

## Table of contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Enabling the module](#enabling-the-module)
4. [AES encryption](#aes-encryption)
5. [RSA public key encryption](#rsa-public-key-encryption)
6. [JKS keystore encryption](#jks-keystore-encryption)
7. [PKCS12 hybrid encryption](#pkcs12-hybrid-encryption)
8. [URL-safe encoding](#url-safe-encoding)
9. [Digital signatures](#digital-signatures)
10. [Programmatic API](#programmatic-api)
11. [Encryption types reference](#encryption-types-reference)
12. [Package structure](#package-structure)
13. [Italiano](#italiano)

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

## PKCS12 hybrid encryption

Modern hybrid encryption using **X25519 ECDH key agreement + HKDF-SHA256 + AES-256-GCM** authenticated encryption, backed by a PKCS12 keystore. This is the strongest encryption option in this module.

### Encryption scheme

For every `encryptValue` call:
1. A fresh **ephemeral X25519 key pair** is generated (forward secrecy — different per call).
2. **ECDH** between the ephemeral private key and the static X25519 public key → shared secret.
3. **HKDF-SHA256** derives a 256-bit AES key from the shared secret (random 12-byte GCM nonce as salt).
4. **AES-256-GCM** encrypts the payload (includes 128-bit authentication tag).
5. Output: `Base64(ephemeralPublicKey).Base64(gcmNonce).Base64(ciphertext+authTag)`

Decryption reverses the process using the static X25519 private key from the keystore.

### Why OpenSSL and not keytool

`keytool -genkeypair` always creates a self-signed certificate, which requires the key to sign it.
**X25519 is a key-agreement-only algorithm** — it cannot produce digital signatures, so keytool fails:

```
keytool error: java.lang.IllegalArgumentException: Cannot derive signature algorithm from XDH
```

OpenSSL can package an X25519 private key into PKCS12 without a certificate (`-nocerts`).
The module automatically derives the public key from the private key at startup using BouncyCastle.

> **Note:** Ed25519 (EdDSA) looks similar to X25519 but is a signing algorithm — it cannot be
> used for key agreement. Always use `-algorithm X25519`, not `-algorithm Ed25519`.

### Generating the keystore with OpenSSL

```bash
# 1. Generate X25519 private key
openssl genpkey -algorithm X25519 -out private.key

# 2. Package into PKCS12 (no certificate needed)
openssl pkcs12 -export -nocerts \
    -inkey private.key \
    -out mase-x25519.p12 \
    -name "mase-x25519"
```

The warning `Warning: -chain option ignored with -nocerts` is harmless and can be ignored.

### Configuration

```properties
com.bld.crypto.pkcs12.file=classpath:mase-x25519.p12
com.bld.crypto.pkcs12.password=yourPassword
com.bld.crypto.pkcs12.alias=mase-x25519
com.bld.crypto.pkcs12.instance-jks=PKCS12
```

### Annotation usage

```java
public class MyResponse {
    @CryptoPkcs12(value = "userId")
    private String userId;          // encrypted on serialization, decrypted on deserialization
}

public class MyRequest {
    @CryptoPkcs12(value = "userId")
    private String userId;          // automatically decrypted when the request body is parsed
}
```

The `value` attribute is an identifier embedded in the encrypted payload. During decryption it is
validated against the annotation value to prevent accidental cross-field substitution.

### Typical frontend round-trip

```
Backend                              Frontend
────────────────────────────────────────────────
@CryptoPkcs12 → encrypted blob ─────────────►  receives opaque Base64 blob
                                               (stores or echoes it as-is)
                                ◄───────────── sends blob back unchanged
@CryptoPkcs12 → decrypted value
```

Only one key pair is required. The backend encrypts with the public key and decrypts with the
private key — both are in the same PKCS12 file.

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

    @CryptoPkcs12(value = "searchToken", url = true)
    private String encryptedToken;   // double Base64: safe in URL path segments or query params
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

### PKCS12 (X25519 + AES-256-GCM)

```java
@Autowired
private CryptoPkcs12Utils pkcs12Utils;

// String encrypt / decrypt
String encrypted    = pkcs12Utils.encryptValue(plaintext);
String decrypted    = pkcs12Utils.decryptValue(encrypted);

// URL-safe variant (adds an extra Base64 layer)
String urlEncrypted = pkcs12Utils.encryptUri(plaintext);
String urlDecrypted = pkcs12Utils.decryptUri(urlEncrypted);

// Object serialization: serializes to JSON before encrypting
String encObj       = pkcs12Utils.encryptObject(myDto);
MyDto decObj        = pkcs12Utils.decryptObject(encObj, MyDto.class);

// Object + URL-safe
String encObjUri    = pkcs12Utils.encryptObjectUri(myDto);
MyDto decObjUri     = pkcs12Utils.decryptObjectUri(encObjUri, MyDto.class);

// Expose the public key (e.g. to share with an external encryptor)
String pubKeyBase64 = pkcs12Utils.publicKey();
```

---

## Encryption types reference

| Annotation | Algorithm | Key configuration prefix | Symmetric |
|---|---|---|---|
| `@CryptoAes` | AES | `com.bld.crypto.aes.keys` | Yes |
| `@CryptoPubKey` | RSA | `com.bld.crypto.pubkey.keys` | No |
| `@CryptoJks` | RSA (JKS) | `com.bld.crypto.jks` | No |
| `@CryptoPkcs12` | X25519 + AES-256-GCM (PKCS12) | `com.bld.crypto.pkcs12` | No |

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
├── pkcs12/
│   ├── annotation/CryptoPkcs12.java                 — field annotation
│   ├── CryptoPkcs12Utils.java                       — programmatic API (X25519 + AES-256-GCM)
│   ├── config/
│   │   ├── CryptoPkcs12Configuration.java
│   │   ├── Pkcs12FormatterConfiguration.java
│   │   └── properties/Pkcs12KeyProperties.java
│   ├── deserializer/DecryptPkcs12Deserializer.java
│   ├── serializer/EncryptPkcs12Serializer.java
│   └── formatter/
│       ├── CryptoPkcs12Formatter.java
│       └── CryptoPkcs12AnnotationFormatterFactory.java
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

Framework di cifratura per Spring Boot che fornisce **cifratura e decifratura trasparente dei campi JSON** tramite annotazioni Jackson. Supporta AES simmetrico, RSA con chiavi PEM pubbliche, RSA con keystore JKS, cifratura ibrida X25519 + AES-256-GCM via PKCS12 e firme digitali — tutto configurabile via `application.properties`.

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
| `@CryptoPkcs12` | X25519 + AES-256-GCM | Cifratura ibrida moderna con keystore PKCS12 |

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

### Cifratura ibrida PKCS12 (X25519 + AES-256-GCM)

Schema moderno che combina ECDH su Curve25519 con AES-256-GCM autenticato.

#### Perché OpenSSL e non keytool

`keytool -genkeypair` richiede sempre un certificato auto-firmato, ma **X25519 non può firmare certificati** — è un algoritmo solo per key agreement. Il tentativo con keytool produce:

```
keytool error: java.lang.IllegalArgumentException: Cannot derive signature algorithm from XDH
```

> Nota: Ed25519 è l'algoritmo di **firma** su Curve25519; X25519 è quello per il **key agreement**.
> Sono diversi: non usare `-algorithm Ed25519` al posto di `-algorithm X25519`.

#### Generazione del keystore con OpenSSL

```bash
# 1. Genera la chiave privata X25519
openssl genpkey -algorithm X25519 -out private.key

# 2. Crea il PKCS12 senza certificato
openssl pkcs12 -export -nocerts \
    -inkey private.key \
    -out mase-x25519.p12 \
    -name "mase-x25519"
```

Il warning `Warning: -chain option ignored with -nocerts` è innocuo.
Il modulo ricava automaticamente la chiave pubblica dalla chiave privata tramite BouncyCastle all'avvio.

#### Configurazione

```properties
com.bld.crypto.pkcs12.file=classpath:mase-x25519.p12
com.bld.crypto.pkcs12.password=tuapassword
com.bld.crypto.pkcs12.alias=mase-x25519
com.bld.crypto.pkcs12.instance-jks=PKCS12
```

#### Utilizzo

```java
public class MyResponse {
    @CryptoPkcs12(value = "userId")
    private String userId;   // cifrato nella risposta JSON al frontend
}

public class MyRequest {
    @CryptoPkcs12(value = "userId")
    private String userId;   // decifrato automaticamente dalla richiesta JSON
}
```

È sufficiente **una sola coppia di chiavi**: il backend cifra con la chiave pubblica (risposta) e decifra con la chiave privata (richiesta). Entrambe sono nello stesso file PKCS12.

### API programmatica PKCS12

```java
@Autowired
private CryptoPkcs12Utils pkcs12Utils;

// Cifra / decifra stringhe
String cifrato      = pkcs12Utils.encryptValue(plaintext);
String decifrato    = pkcs12Utils.decryptValue(cifrato);

// Variante URL-safe (doppio livello Base64)
String cifratoUrl   = pkcs12Utils.encryptUri(plaintext);
String decifratoUrl = pkcs12Utils.decryptUri(cifratoUrl);

// Serializzazione oggetti: converte in JSON prima di cifrare
String cifratoObj   = pkcs12Utils.encryptObject(mioDto);
MioDto decifratoObj = pkcs12Utils.decryptObject(cifratoObj, MioDto.class);

// Oggetto + URL-safe
String cifratoObjUrl = pkcs12Utils.encryptObjectUri(mioDto);
MioDto dec           = pkcs12Utils.decryptObjectUri(cifratoObjUrl, MioDto.class);

// Esporre la chiave pubblica (es. per condividerla con un client esterno)
String pubKeyBase64  = pkcs12Utils.publicKey();
```

### Firme digitali

`SignatureKeyUtils` consente di firmare e verificare dati binari tramite una chiave in un keystore JKS configurato con il prefisso `com.bld.crypto.signature.keys`.
