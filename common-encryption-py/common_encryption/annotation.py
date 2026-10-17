"""Field-level crypto annotations. Counterpart of the Jackson annotations of the Java module.

Java puts the behaviour on a field with ``@CryptoPkcs12`` / ``@CryptoJks`` and lets
Jackson's serializer and deserializer encrypt and decrypt transparently. Python has no
such implicit hook, so the same contract is expressed with :data:`typing.Annotated`
metadata plus :mod:`common_encryption.model`, which walks a dataclass and applies the
annotations on the way in and out::

    @dataclass
    class IdUserFilter:
        id_user: Annotated[int | None, CryptoPkcs12("idUser", url=True)] = None

    to_json(filter)                  # {"id_user": "eyJ...=="}   encrypted
    from_json(IdUserFilter, body)    # id_user == 1               decrypted
"""

from __future__ import annotations

import json
from dataclasses import dataclass, is_dataclass
from enum import Enum
from types import UnionType
from typing import Annotated, Any, Union, get_args, get_origin, get_type_hints

JSON_ARGS = {"separators": (",", ":"), "ensure_ascii": False}


@dataclass(frozen=True)
class CryptoAnnotation:
    """Base for the field annotations. Mirrors the attributes of the Java annotations.

    Args:
        value: logical key identifier embedded in the encrypted payload. Verified on
            decryption so a token cannot be substituted across fields.
        url: add the extra Base64 layer that makes the token safe in a URL path segment
            or query parameter.
    """

    value: str = ""
    url: bool = False

    def encrypt(self, plain: str) -> str:  # pragma: no cover - abstract
        raise NotImplementedError

    def decrypt(self, token: str) -> str:  # pragma: no cover - abstract
        raise NotImplementedError

    # -- scalar / collection / POJO handling shared with the Java serializer ---------

    def encrypt_any(self, value: Any) -> Any:
        """Encrypt one field value: scalars as text, collections element by element.

        Mirrors ``EncryptCertificateSerializer.serialize``: ``Number`` and ``String`` are
        encrypted as text, ``Collection``/array element by element, anything else as JSON.
        """
        if value is None:
            return None
        if isinstance(value, (list, tuple, set)):
            return [self.encrypt_any(item) for item in value]
        return self.encrypt(_to_text(value))

    def decrypt_any(self, token: Any, target: Any) -> Any:
        """Decrypt one field value and coerce it back to ``target``."""
        if token is None:
            return None
        if isinstance(token, (list, tuple)):
            item_type = _item_type(target)
            return [_coerce(self.decrypt(item), item_type) for item in token]
        return _coerce(self.decrypt(token), target)


def _item_type(target: Any) -> Any:
    """Element type of a ``list[int]``-style hint, so each item is coerced, not the list."""
    target = _unwrap_optional(target)
    args = get_args(target)
    if args:
        return args[0]
    return Any


def _to_text(value: Any) -> str:
    """Render a Python value the way ``EncryptCertificateSerializer`` renders it.

    Java writes ``Number`` and ``String`` through ``toString()`` and everything else as
    JSON. ``bool`` is checked first because it is a subclass of ``int`` in Python.
    """
    if isinstance(value, str):
        return value
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, (int, float)):
        return str(value)
    return json.dumps(value, default=_json_default, **JSON_ARGS)


def _json_default(value: Any) -> Any:
    from dataclasses import asdict

    if is_dataclass(value) and not isinstance(value, type):
        return asdict(value)
    if isinstance(value, Enum):
        return value.value
    raise TypeError(f"Object of type {type(value).__name__} is not JSON serialisable")


def _coerce(word: str | None, target: Any) -> Any:
    """Coerce a decrypted string to ``target``, mirroring ``getValue``.

    Mirrors ``DecryptCertificateDeserializer.getValue``: numbers are parsed, strings pass
    through, anything else is read back from JSON.
    """
    if word is None or not str(word).strip():
        return None

    target = _unwrap_optional(target)
    if target is None or target is Any or target is str:
        return word

    origin = get_origin(target)
    if origin in (list, set, frozenset, tuple):
        item_type = (get_args(target) or (Any,))[0]
        return origin(_coerce_item(item_type, item) for item in json.loads(word))

    if target is bool:
        return str(word).strip().lower() == "true"
    if target is int:
        return int(word)
    if target is float:
        return float(word)

    parsed = json.loads(word)
    if is_dataclass(target) and isinstance(parsed, dict):
        return target(**parsed)
    return parsed


def _unwrap_optional(hint: Any) -> Any:
    """Strip ``Optional``/``| None`` so the underlying type can be compared."""
    origin = get_origin(hint)
    if origin is Union or origin is UnionType:
        args = [arg for arg in get_args(hint) if arg is not type(None)]
        if len(args) == 1:
            return args[0]
    return hint


def _coerce_item(item_type: Any, element: Any) -> Any:
    """Coerce one element of a decrypted collection."""
    if item_type in (str, Any, None):
        return element
    if item_type is bool:
        return str(element).strip().lower() == "true"
    if item_type is int:
        return int(element)
    if item_type is float:
        return float(element)
    return element


def find_annotation(hint: Any) -> CryptoAnnotation | None:
    """Return the :class:`CryptoAnnotation` carried by an ``Annotated`` hint, if any."""
    if get_origin(hint) is not Annotated:
        return None
    return next(
        (metadata for metadata in get_args(hint)[1:] if isinstance(metadata, CryptoAnnotation)),
        None,
    )


def strip_annotated(hint: Any) -> Any:
    """Strip ``Annotated`` wrappers to get the underlying type."""
    return get_args(hint)[0] if get_origin(hint) is Annotated else hint


def field_hints(cls: type) -> dict[str, Any]:
    """Resolve ``cls`` field types with ``Annotated`` metadata preserved.

    ``include_extras=True`` is what keeps the crypto metadata visible to the walker.
    """
    return get_type_hints(cls, include_extras=True)
