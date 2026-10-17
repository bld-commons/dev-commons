/*
 * Cross-language interop harness for common-encryption-py.
 *
 * Replays the exact JCA calls that com.bld.crypto.* makes, so Python can prove it reads
 * and writes the same bytes:
 *
 *   CryptoKeyUtils.encryptValue     ->  Cipher.getInstance("RSA")     (RSA/ECB/PKCS1Padding)
 *   CryptoPkcs12Utils.encryptValue  ->  XDH KeyAgreement + HKDF-SHA256 + AES/GCM/NoPadding
 *
 * HKDF is spelled out with javax.crypto.Mac because the JDK has no built-in HKDF before
 * Java 25; the expand loop is RFC 5869, the same thing BouncyCastle's HKDFBytesGenerator
 * does inside the library.
 *
 * Single-file source launcher:
 *   java -Dvec.pkcs12File=... tests/interop/JavaVectors.java gen <out.json>
 *   java -Dvec.pkcs12File=... tests/interop/JavaVectors.java chk <tokens.json>
 *
 * System properties must precede the source file, so they are picked up by the JVM
 * rather than by the program:
 *
 *   -Dvec.pkcs12File  -Dvec.pkcs12Password  -Dvec.pkcs12Alias
 *   -Dvec.jksFile     -Dvec.jksPassword     -Dvec.jksAlias     -Dvec.jksType
 *   -Dvec.aesKey      -Dvec.hmacKey         -Dvec.hmacAlgorithm
 *   -Dvec.signatureInstance
 */

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.Signature;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVectors {

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final byte[] HKDF_INFO = "CryptoPkcs12".getBytes(StandardCharsets.UTF_8);
    private static final Base64.Encoder B64 = Base64.getEncoder();
    private static final Base64.Decoder B64D = Base64.getDecoder();

    /**
     * X25519 base point 9 as a SubjectPublicKeyInfo. Multiplying by it with KeyAgreement
     * is the standard way to derive a public key from a raw scalar without BouncyCastle,
     * which is what CryptoPkcs12Configuration.resolvePublicKey does.
     */
    private static final byte[] X25519_BASE_SPKI = buildBaseSpki();

    private static PrivateKey pkcs12Private;
    private static PublicKey pkcs12Public;
    private static PrivateKey jksPrivate;
    private static PublicKey jksPublic;
    private static final Map<String, String> VECTORS = new LinkedHashMap<>();

    /** Optional: when set, AES and HMAC vectors are produced and checked too. */
    private static byte[] aesKey;
    private static byte[] hmacKey;
    private static String hmacAlgorithm = "HmacSHA256";
    private static String signatureInstance = "SHA256withRSA";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: gen|chk <file>");
            System.exit(2);
        }
        loadPkcs12(option("pkcs12File"), option("pkcs12Password"), option("pkcs12Alias"));
        loadJks(option("jksFile"), option("jksPassword"), option("jksAlias"), option("jksType"));

        String aesKeyOption = option("aesKey");
        if (aesKeyOption != null)
            aesKey = aesKeyOption.getBytes(StandardCharsets.UTF_8);
        String hmacKeyOption = option("hmacKey");
        if (hmacKeyOption != null) {
            hmacKey = hmacKeyOption.getBytes(StandardCharsets.UTF_8);
            hmacAlgorithm = optionOr("hmacAlgorithm", "HmacSHA256");
        }
        signatureInstance = optionOr("signatureInstance", "SHA256withRSA");

        if (args[0].equals("gen")) {
            generate();
            Files.writeString(Path.of(args[1]), toJson(VECTORS));
            System.out.println("wrote " + VECTORS.size() + " vectors to " + args[1]);
            return;
        }
        if (args[0].equals("chk")) {
            check(parseJsonObject(Files.readString(Path.of(args[1]))));
            return;
        }
        throw new IllegalArgumentException("unknown mode " + args[0]);
    }

    // ------------------------------------------------------------------ keystores

    private static void loadPkcs12(String file, String password, String alias) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Path.of(file))) {
            store.load(in, password.toCharArray());
        }
        pkcs12Private = (PrivateKey) store.getKey(alias, password.toCharArray());
        Certificate cert = store.getCertificate(alias);
        if (cert != null) {
            pkcs12Public = cert.getPublicKey();
        } else {
            pkcs12Public = deriveX25519Public(pkcs12Private);
        }
    }

    private static void loadJks(String file, String password, String alias, String type) throws Exception {
        KeyStore store = KeyStore.getInstance(type);
        try (InputStream in = Files.newInputStream(Path.of(file))) {
            store.load(in, password.toCharArray());
        }
        jksPrivate = (PrivateKey) store.getKey(alias, password.toCharArray());
        jksPublic = store.getCertificate(alias).getPublicKey();
        VECTORS.put("jks.privateKey.pkcs8.b64", B64.encodeToString(jksPrivate.getEncoded()));
        VECTORS.put("jks.publicKey.der.b64", B64.encodeToString(jksPublic.getEncoded()));
    }

    /** CryptoPkcs12Configuration.resolvePublicKey: ECDH the private scalar with the base point. */
    private static PublicKey deriveX25519Public(PrivateKey privateKey) throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance("XDH");
        ka.init(privateKey);
        ka.doPhase(KeyFactory.getInstance("XDH").generatePublic(new X509EncodedKeySpec(X25519_BASE_SPKI)), true);
        return KeyFactory.getInstance("XDH")
                .generatePublic(new X509EncodedKeySpec(wrapX25519PublicDer(ka.generateSecret())));
    }

    // ------------------------------------------------------------------ pkcs12

    /** Mirrors CryptoPkcs12Utils.encryptValue. */
    private static String pkcs12Encrypt(String value) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("XDH");
        kpg.initialize(NamedParameterSpec.X25519);
        java.security.KeyPair ephemeral = kpg.generateKeyPair();

        KeyAgreement ka = KeyAgreement.getInstance("XDH");
        ka.init(ephemeral.getPrivate());
        ka.doPhase(pkcs12Public, true);
        byte[] shared = ka.generateSecret();

        byte[] nonce = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(nonce);
        byte[] aesKey = hkdf(shared, nonce, HKDF_INFO, 32);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        return B64.encodeToString(ephemeral.getPublic().getEncoded()) + "."
                + B64.encodeToString(nonce) + "."
                + B64.encodeToString(ciphertext);
    }

    /** Mirrors CryptoPkcs12Utils.decryptValue. */
    private static String pkcs12Decrypt(String value) throws Exception {
        String[] parts = value.split("\\.");
        if (parts.length != 3)
            throw new IllegalArgumentException(
                    "Invalid PKCS12 encrypted value: expected 3 dot-separated Base64 components");
        PublicKey ephemeral = KeyFactory.getInstance("XDH")
                .generatePublic(new X509EncodedKeySpec(B64D.decode(parts[0])));
        byte[] nonce = B64D.decode(parts[1]);
        byte[] ciphertext = B64D.decode(parts[2]);

        KeyAgreement ka = KeyAgreement.getInstance("XDH");
        ka.init(pkcs12Private);
        ka.doPhase(ephemeral, true);
        byte[] aesKey = hkdf(ka.generateSecret(), nonce, HKDF_INFO, 32);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------------- jks

    /** Mirrors CryptoKeyUtils.encryptValue for InstanceType.RSA. */
    private static String rsaEncrypt(String value, java.security.Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return B64.encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    /** Mirrors CryptoKeyUtils.decryptValue for InstanceType.RSA. */
    private static String rsaDecrypt(String value, java.security.Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(B64D.decode(value)), StandardCharsets.UTF_8);
    }

    /** Mirrors CryptoAesUtils: Cipher.getInstance("AES") is AES/ECB/PKCS5Padding. */
    private static String aesEncrypt(String value) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        return B64.encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static String aesDecrypt(String value) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
        return new String(cipher.doFinal(B64D.decode(value)), StandardCharsets.UTF_8);
    }

    /** Mirrors CryptoHmacUtils.signValue: Base64(envelope).Base64(mac). */
    private static String hmacSign(String fieldKey, String value) throws Exception {
        byte[] envelope = envelope(fieldKey, value).getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance(hmacAlgorithm);
        mac.init(new SecretKeySpec(hmacKey, hmacAlgorithm));
        return B64.encodeToString(envelope) + "." + B64.encodeToString(mac.doFinal(envelope));
    }

    private static String hmacSignUri(String fieldKey, String value) throws Exception {
        byte[] envelope = envelope(fieldKey, value).getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance(hmacAlgorithm);
        mac.init(new SecretKeySpec(hmacKey, hmacAlgorithm));
        Base64.Encoder encoder = Base64.getUrlEncoder();
        return encoder.encodeToString(envelope) + "." + encoder.encodeToString(mac.doFinal(envelope));
    }

    private static String hmacVerify(String token, String fieldKey, boolean url) throws Exception {
        int dot = token.lastIndexOf('.');
        if (dot < 0)
            throw new IllegalArgumentException("no separator");
        Base64.Decoder decoder = url ? Base64.getUrlDecoder() : B64D;
        byte[] envelope = decoder.decode(token.substring(0, dot));
        byte[] received = decoder.decode(token.substring(dot + 1));
        Mac mac = Mac.getInstance(hmacAlgorithm);
        mac.init(new SecretKeySpec(hmacKey, hmacAlgorithm));
        if (!MessageDigest.isEqual(mac.doFinal(envelope), received))
            throw new IllegalArgumentException("HMAC mismatch");
        return openEnvelope(new String(envelope, StandardCharsets.UTF_8), fieldKey);
    }

    /** Mirrors SignatureKeyUtils.sign. */
    private static String sign(String text, PrivateKey privateKey, String instance) throws Exception {
        Signature signature = Signature.getInstance(instance);
        signature.initSign(privateKey);
        signature.update(text.getBytes(StandardCharsets.UTF_8));
        return B64.encodeToString(signature.sign());
    }

    private static boolean verify(PublicKey publicKey, String signed, String text, String instance)
            throws Exception {
        Signature signature = Signature.getInstance(instance);
        signature.initVerify(publicKey);
        signature.update(text.getBytes(StandardCharsets.UTF_8));
        return signature.verify(B64D.decode(signed));
    }

    // ------------------------------------------------------------------- vectors

    private static void generate() throws Exception {
        VECTORS.put("pkcs12.value", pkcs12Encrypt("ciao mondo"));
        VECTORS.put("pkcs12.value.unicode", pkcs12Encrypt("città \u20ac 1"));
        VECTORS.put("pkcs12.keyValue", pkcs12Encrypt(envelope("email", "francesco@baldi.it")));
        VECTORS.put("pkcs12.uri",
                B64.encodeToString(pkcs12Encrypt("uri-payload").getBytes(StandardCharsets.UTF_8)));
        VECTORS.put("pkcs12.publicKey.der.b64", B64.encodeToString(pkcs12Public.getEncoded()));

        VECTORS.put("jks.value.publicKey", rsaEncrypt("segreto", jksPublic));
        VECTORS.put("jks.value.privateKey", rsaEncrypt("segreto", jksPrivate));
        VECTORS.put("jks.keyValue", rsaEncrypt(envelope("idUser", "42"), jksPublic));
        VECTORS.put("jks.uri",
                B64.encodeToString(rsaEncrypt("uri-payload", jksPublic).getBytes(StandardCharsets.UTF_8)));

        if (aesKey != null) {
            VECTORS.put("aes.value", aesEncrypt("citta euro 1"));
            VECTORS.put("aes.uri",
                    B64.encodeToString(aesEncrypt("uri-payload").getBytes(StandardCharsets.UTF_8)));
        }
        if (hmacKey != null) {
            VECTORS.put("hmac.value", hmacSign("idUser", "citta euro 1"));
            VECTORS.put("hmac.uri", hmacSignUri("idUser", "uri-payload"));
        }
        VECTORS.put("signature.value", sign("payload", jksPrivate, signatureInstance));
    }

    private static void check(Map<String, String> tokens) throws Exception {
        int failures = 0;
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("pkcs12.value", "py-interop-pkcs12");
        expected.put("pkcs12.keyValue", "py-interop-envelope");
        expected.put("pkcs12.uri", "py-interop-uri");
        expected.put("jks.value.publicKey", "py-interop-rsa");
        expected.put("jks.keyValue", "py-interop-rsa-envelope");
        expected.put("jks.uri", "py-interop-rsa-uri");
        if (aesKey != null) {
            expected.put("aes.value", "py-interop-aes");
            expected.put("aes.uri", "py-interop-aes-uri");
        }
        if (hmacKey != null) {
            expected.put("hmac.value", "py-interop-hmac");
            expected.put("hmac.uri", "py-interop-hmac-uri");
        }

        Map<String, String> actual = new LinkedHashMap<>();
        actual.put("pkcs12.value", pkcs12Decrypt(require(tokens, "pkcs12.value")));
        actual.put("pkcs12.keyValue",
                openEnvelope(pkcs12Decrypt(require(tokens, "pkcs12.keyValue")), "email"));
        actual.put("pkcs12.uri", pkcs12Decrypt(
                new String(B64D.decode(require(tokens, "pkcs12.uri")), StandardCharsets.UTF_8)));
        actual.put("jks.value.publicKey", rsaDecrypt(require(tokens, "jks.value.publicKey"), jksPrivate));
        actual.put("jks.keyValue", openEnvelope(rsaDecrypt(require(tokens, "jks.keyValue"), jksPrivate), "idUser"));
        actual.put("jks.uri", rsaDecrypt(
                new String(B64D.decode(require(tokens, "jks.uri")), StandardCharsets.UTF_8), jksPrivate));
        if (aesKey != null) {
            actual.put("aes.value", aesDecrypt(require(tokens, "aes.value")));
            actual.put("aes.uri", aesDecrypt(
                    new String(B64D.decode(require(tokens, "aes.uri")), StandardCharsets.UTF_8)));
        }
        if (hmacKey != null) {
            actual.put("hmac.value", hmacVerify(require(tokens, "hmac.value"), "idUser", false));
            actual.put("hmac.uri", hmacVerify(require(tokens, "hmac.uri"), "idUser", true));
        }
        expected.put("signature.value", "verified");
        actual.put("signature.value", verify(jksPublic, require(tokens, "signature.value"), "payload",
                signatureInstance) ? "verified" : "rejected");

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String got = actual.get(entry.getKey());
            boolean ok = entry.getValue().equals(got);
            System.out.printf("%-24s %-5s got=%s%n", entry.getKey(), ok ? "OK" : "FAIL", got);
            if (!ok) {
                System.out.printf("%-24s      expected=%s%n", "", entry.getValue());
                failures++;
            }
        }
        if (failures > 0)
            throw new IllegalStateException(failures + " interop check(s) failed");
        System.out.println("all interop checks passed");
    }

    private static String require(Map<String, String> tokens, String name) {
        String value = tokens.get(name);
        if (value == null)
            throw new IllegalArgumentException("Missing token " + name + " in payload");
        return value;
    }

    // ------------------------------------------------------------------- helpers

    /** Mirrors KeyUtility.buildEnvelope. */
    private static String envelope(String key, String value) {
        return "{\"key\":\"" + key + "\",\"value\":\"" + value + "\"}";
    }

    /** Mirrors KeyUtility.openEnvelope. */
    private static String openEnvelope(String envelope, String expectedKey) {
        Matcher key = Pattern.compile("\"key\"\\s*:\\s*\"([^\"]*)\"").matcher(envelope);
        if (!key.find() || !key.group(1).equals(expectedKey))
            throw new IllegalArgumentException("envelope key mismatch");
        Matcher value = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]*)\"").matcher(envelope);
        if (!value.find())
            throw new IllegalArgumentException("envelope value missing");
        return value.group(1);
    }

    /** RFC 5869 HKDF-SHA256, equivalent to BouncyCastle HKDFBytesGenerator. */
    private static byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt.length == 0 ? new byte[32] : salt, "HmacSHA256"));
        byte[] prk = mac.doFinal(ikm);

        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        byte[] output = new byte[0];
        byte[] previous = new byte[0];
        int counter = 1;
        while (output.length < length) {
            byte[] block = new byte[info.length + previous.length + 1];
            System.arraycopy(previous, 0, block, 0, previous.length);
            System.arraycopy(info, 0, block, previous.length, info.length);
            block[block.length - 1] = (byte) counter++;
            previous = mac.doFinal(block);

            byte[] grown = new byte[output.length + previous.length];
            System.arraycopy(output, 0, grown, 0, output.length);
            System.arraycopy(previous, 0, grown, output.length, previous.length);
            output = grown;
        }
        byte[] result = new byte[length];
        System.arraycopy(output, 0, result, 0, length);
        return result;
    }

    private static byte[] buildBaseSpki() {
        byte[] algorithm = { 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x6e };
        byte[] point = new byte[35]; // 03 21 00 || 32 raw bytes
        point[0] = 0x03;
        point[1] = 0x21;
        point[2] = 0x00;
        point[3] = 0x09; // base point u = 9, remaining bytes zero

        byte[] der = new byte[algorithm.length + point.length + 2];
        der[0] = 0x30;
        der[1] = (byte) (algorithm.length + point.length);
        System.arraycopy(algorithm, 0, der, 2, algorithm.length);
        System.arraycopy(point, 0, der, 2 + algorithm.length, point.length);
        return der;
    }

    /** Wrap a raw X25519 public key in a SubjectPublicKeyInfo, as JCA getEncoded() returns. */
    private static byte[] wrapX25519PublicDer(byte[] raw) {
        byte[] algorithm = { 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x6e };
        byte[] point = new byte[35]; // 03 21 00 || 32 raw bytes
        point[0] = 0x03;
        point[1] = 0x21;
        point[2] = 0x00;
        System.arraycopy(raw, 0, point, 3, 32);

        byte[] der = new byte[algorithm.length + point.length + 2];
        der[0] = 0x30;
        der[1] = (byte) (algorithm.length + point.length);
        System.arraycopy(algorithm, 0, der, 2, algorithm.length);
        System.arraycopy(point, 0, der, 2 + algorithm.length, point.length);
        return der;
    }

    private static String option(String name) {
        String value = System.getProperty("vec." + name);
        if (value == null)
            throw new IllegalArgumentException("Missing -Dvec." + name + "=...");
        return value;
    }

    /** Reads an optional property, tolerating a missing one instead of failing. */
    private static String optionOr(String name, String fallback) {
        try {
            return option(name);
        } catch (IllegalArgumentException absent) {
            return fallback;
        }
    }

    private static String toJson(Map<String, String> map) {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet())
            entries.add("\"" + entry.getKey() + "\":\"" + entry.getValue().replace("\\", "\\\\") + "\"");
        return "{" + String.join(",", entries) + "}";
    }

    private static Map<String, String> parseJsonObject(String json) {
        Map<String, String> parsed = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("\"([A-Za-z0-9._]+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        while (matcher.find())
            parsed.put(matcher.group(1), matcher.group(2).replace("\\\\", "\\"));
        return parsed;
    }
}
