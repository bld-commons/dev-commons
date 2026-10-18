# crypto-example-py

Python counterpart of [`crypto-example`](../crypto-example): the two services exchange
messages encrypted with `@CryptoPkcs12`, so the same field annotation has to produce a
token Java can decrypt and accept one Java produced.

Both services load the **same** X25519 keystore, `shared.p12` (identical bytes in
`crypto-example-py/shared.p12` and `crypto-example/src/main/resources/shared.p12`). The
PKCS12 scheme encrypts to the public key of that pair and decrypts with its private key,
so with one keystore each side can both write and read.

A FastAPI app (uvicorn underneath), so `GET /docs` serves the interactive OpenAPI page.
`/pkcs12/decrypt` takes an `EncryptedEmployee` parameter: an `Annotated[EmployeePkcs12,
BeforeValidator(from_dict), PlainSerializer(to_dict)]` pydantic type, so the body is
decrypted while pydantic validates it and the endpoint receives the already-decrypted
object **before** the method is entered — the same contract as Java's
`@RequestBody EmployeeDecryptPkcs12`. The same type on a return value encrypts on the way
out. See the
[common-encryption-py README](../common-encryption-py/README.md#receiving-encrypted-input-in-python-like-java-requestbody)
for the reusable pattern.

## Run

```bash
# one venv, both projects
python -m venv .venv && . .venv/bin/activate
pip install -e common-encryption-py -e crypto-example-py
pip install -e "common-encryption-py[test]"   # optional, for pytest

crypto-example-py                      # :8090, peer http://localhost:8080
PORT=8091 PEER_URL=http://localhost:8080 crypto-example-py
```

The Java side is the same as always:

```bash
cd crypto-example && ./mvnw spring-boot:run   # :8080
```

## Endpoints

| Method | Path | What it proves |
| --- | --- | --- |
| `GET` | `/pkcs12/encrypt` | this side's ciphertext |
| `POST` | `/pkcs12/decrypt` | this side can read the peer's ciphertext |
| `POST` | `/pkcs12/send` | push an encrypted employee to the peer and get its plaintext back |
| `GET` | `/pkcs12/peer-encrypt` | read the peer's ciphertext and decrypt it locally |

## Exchanging messages

Both sides expose the same four routes, so either can be the caller:

```bash
# Java -> Python: encrypt over here, decrypt over there
curl -s localhost:8080/pkcs12/encrypt | jq . | tee /tmp/java.json
curl -s -XPOST localhost:8090/pkcs12/decrypt -d @/tmp/java.json | jq .

# Python -> Java: the same round trip in the other direction
curl -s localhost:8090/pkcs12/encrypt | jq . | tee /tmp/py.json
curl -s -XPOST localhost:8080/pkcs12/decrypt -d @/tmp/py.json | jq .

# each service pushing its own message to the peer
curl -s localhost:8090/pkcs12/send
curl -s localhost:8080/pkcs12/send

# each service reading what the other produces
curl -s localhost:8090/pkcs12/peer-encrypt
curl -s localhost:8080/pkcs12/peer-encrypt
```

A `POST /pkcs12/decrypt` answers with the plaintext it recovered, which is how the exchange
is checked from the shell rather than from a log line.

## Configured keystore

```yaml
com:
  bld:
    crypto:
      pkcs12:
        file: classpath:shared.p12      # PKCS12_FILE in Python
        alias: mase-x25519
        password: pkcs12pass
        instance-jks: PKCS12
```

Regenerate both copies together, they must stay byte identical:

```bash
openssl genpkey -algorithm X25519 -out /tmp/x.key
openssl pkcs12 -export -inkey /tmp/x.key -out shared.p12 -passout pass:pkcs12pass \
  -name mase-x25519
cp shared.p12 ../crypto-example/src/main/resources/shared.p12
```
