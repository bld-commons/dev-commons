"""``crypto-example`` as a Python FastAPI service.

Endpoints mirror the Java ones so either side can be the caller:

    GET  /pkcs12/encrypt        my ciphertext, ready for the peer to decrypt
    POST /pkcs12/decrypt        my decryption of the peer's ciphertext
    POST /pkcs12/send           encrypt a fresh employee and push it to the peer
    GET  /pkcs12/peer-encrypt   read the peer's ciphertext back and decrypt it

The request body of ``/pkcs12/decrypt`` is typed as ``EncryptedEmployee``: an
``Annotated[EmployeePkcs12, BeforeValidator(from_dict), PlainSerializer(to_dict)]`` pydantic
type, so the body is decrypted while validated and the endpoint receives the already
decrypted object — the same contract as Java's ``@RequestBody``. No dependency and no
custom deserializer.
"""

from __future__ import annotations

import json
import os
from dataclasses import asdict
from pathlib import Path
from typing import Any
from urllib.error import URLError
from urllib.request import Request as UrlRequest
from urllib.request import urlopen

import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from common_encryption import CryptoConfig, CryptoException, from_json, to_json

from .models import EmployeePkcs12, EncryptedEmployee

DEFAULT_KEYSTORE = Path(__file__).resolve().parents[1] / "shared.p12"
KEYSTORE_ALIAS = "mase-x25519"
KEYSTORE_PASSWORD = "pkcs12pass"

PEER_URL = os.environ.get("PEER_URL", "http://localhost:8080").rstrip("/")

app = FastAPI(title="crypto-example-py", version="0.1.0")


def configure() -> CryptoConfig:
    """Load the shared PKCS12 keystore the Java service loads too."""
    return CryptoConfig.from_properties(
        {
            "com.bld.crypto.pkcs12": {
                "file": os.environ.get("PKCS12_FILE", str(DEFAULT_KEYSTORE)),
                "alias": KEYSTORE_ALIAS,
                "password": KEYSTORE_PASSWORD,
                "instance-jks": "PKCS12",
            }
        }
    )


# Loaded here, not in main(): the debugger launches `uvicorn crypto_example.app:app`,
# which imports the module and never calls main().
configure()


def _call_peer(method: str, path: str, body: str | None = None) -> tuple[int, str]:
    """Talk to the Java service, still without an HTTP client dependency."""
    request = UrlRequest(
        f"{PEER_URL}{path}",
        data=body.encode() if body is not None else None,
        headers={"Content-Type": "application/json"} if body is not None else {},
        method=method,
    )
    try:
        with urlopen(request, timeout=10) as response:
            return response.status, response.read().decode()
    except URLError as error:
        raise CryptoException(f"{method} {path} against {PEER_URL} failed: {error}") from error


@app.exception_handler(CryptoException)
def _crypto_error(request: Request, exc: CryptoException) -> JSONResponse:
    """The same 502 the stdlib version answered with."""
    return JSONResponse(status_code=502, content={"detail": str(exc)})


@app.get("/pkcs12/encrypt")
def encrypt() -> EncryptedEmployee:
    """My ciphertext for the peer."""
    return EmployeePkcs12.sample()


@app.post("/pkcs12/decrypt")
def decrypt(employee: EncryptedEmployee) -> dict[str, Any]:
    """The plaintext behind someone else's ciphertext, echoed back so curl can show it.

    pydantic decrypted the body while validating the parameter, so ``employee`` is a
    plain object — the same contract as Java's ``@RequestBody EmployeeDecryptPkcs12``.
    """
    return asdict(employee)


@app.post("/pkcs12/send")
def send_to_peer() -> dict[str, Any]:
    """Encrypt a fresh employee and hand it to the peer's decrypt endpoint."""
    status, body = _call_peer("POST", "/pkcs12/decrypt", to_json(EmployeePkcs12.sample()))
    if status >= 400:
        raise CryptoException(f"the peer refused the message: HTTP {status} {body}")
    return json.loads(body)


@app.get("/pkcs12/peer-encrypt")
def peer_encrypt() -> dict[str, Any]:
    """Read the peer's ciphertext and decrypt it here."""
    status, body = _call_peer("GET", "/pkcs12/encrypt")
    if status >= 400:
        raise CryptoException(f"the peer refused to encrypt: HTTP {status} {body}")
    return asdict(from_json(EmployeePkcs12, body))


def main() -> None:
    port = int(os.environ.get("PORT", "8090"))
    print(f"crypto-example-py on :{port}, peer {PEER_URL}", flush=True)
    uvicorn.run(app, host="0.0.0.0", port=port)


if __name__ == "__main__":
    main()
