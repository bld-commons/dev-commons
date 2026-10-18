"""Cross-service PKCS12 demo, Python side of ``crypto-example``.

The wire shapes here must stay identical to the Java models in
``crypto-example/src/main/java/com/bld/commons/example/model``: field names are the
Jackson ones (``idProfiles``, ``firstName``), not Python conventions, because both
services exchange the same JSON.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Annotated

from pydantic import BeforeValidator, PlainSerializer

from common_encryption import CryptoPkcs12, from_dict, to_dict


@dataclass
class Registry:
    """Mirror of ``com.bld.commons.example.model.Registry``.

    ``dtBirth`` is a plain string because ``@DateTimeZone(format = "yyyy-MM-dd")`` writes
    that exact shape and Python has no date library to give this project.
    """

    firstName: str | None = None
    lastName: str | None = None
    dtBirth: str | None = None


@dataclass
class EmployeePkcs12:
    """One model for both directions.

    Java has ``EmployeeEncryptPkcs12`` and ``EmployeeDecryptPkcs12`` only because Jackson
    picks a serializer or a deserializer per direction; ``to_json`` / ``from_json`` already
    know which way they are going, so one dataclass covers both.
    """

    registry: Annotated[Registry | None, CryptoPkcs12("registry_table")] = None
    idProfiles: Annotated[list[int] | None, CryptoPkcs12("profile_table")] = None
    idAuthorities: Annotated[list[int] | None, CryptoPkcs12("role_table")] = None

    @classmethod
    def sample(cls) -> "EmployeePkcs12":
        """The same payload ``JksController.encryptEmployee`` builds."""
        return cls(
            registry=Registry("Mario", "Rossi", "1990-01-01"),
            idProfiles=[1, 2, 3],
            idAuthorities=[1, 2, 3],
        )


EncryptedEmployee = Annotated[
    EmployeePkcs12,
    BeforeValidator(lambda value: value if isinstance(value, EmployeePkcs12) else from_dict(EmployeePkcs12, value)),
    PlainSerializer(to_dict),
]
