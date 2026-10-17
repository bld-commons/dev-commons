"""Dataclass walker that makes the field annotations transparent.

This is the Python stand-in for Jackson's serializer/deserializer pair. It replaces the
``@JsonSerialize`` / ``@JsonDeserialize`` of the Java module: :func:`to_json` encrypts
every annotated field on the way out, :func:`from_json` decrypts and coerces it back on
the way in.

Only dataclasses are supported, which is what the sibling Python projects in this
workspace already use.
"""

from __future__ import annotations

import json
from dataclasses import fields, is_dataclass
from typing import Any

from .annotation import field_hints, find_annotation, strip_annotated

JSON_ARGS = {"separators": (",", ":"), "ensure_ascii": False}


def to_dict(obj: Any) -> Any:
    """Convert ``obj`` to plain JSON-ready data, encrypting every annotated field."""
    if not is_dataclass(obj) or isinstance(obj, type):
        return obj
    result: dict[str, Any] = {}
    hints = field_hints(type(obj))
    for field in fields(obj):
        hint = hints.get(field.name, field.type)
        annotation = find_annotation(hint)
        result[field.name] = (
            annotation.encrypt_any(getattr(obj, field.name)) if annotation else _plain(getattr(obj, field.name))
        )
    return result


def from_dict(cls: type, data: Any) -> Any:
    """Rebuild an instance of ``cls`` from ``data``, decrypting every annotated field."""
    if data is None:
        return None
    if not isinstance(data, dict):
        raise TypeError(f"Expected a mapping to build {cls.__name__}, got {type(data).__name__}")

    hints = field_hints(cls)
    known = {field.name for field in fields(cls)}
    values: dict[str, Any] = {}
    for name, raw in data.items():
        if name not in known:  # mirrors @JsonIgnoreProperties(ignoreUnknown = true)
            continue
        hint = hints.get(name, name)
        annotation = find_annotation(hint)
        values[name] = (
            annotation.decrypt_any(raw, strip_annotated(hint)) if annotation else raw
        )
    return cls(**values)


def to_json(obj: Any) -> str:
    """Serialise ``obj`` to JSON with the annotated fields encrypted."""
    return json.dumps(to_dict(obj), **JSON_ARGS)


def from_json(cls: type, payload: str | bytes) -> Any:
    """Parse ``payload`` and build an instance of ``cls``, decrypting annotated fields."""
    return from_dict(cls, json.loads(payload))


def encrypt_url_values(obj: Any) -> dict[str, Any]:
    """Field name to token map, for building a query string from a model."""
    return {
        name: token
        for name, token in to_dict(obj).items()
        if token is not None and not isinstance(token, (dict, list))
    }


def _plain(value: Any) -> Any:
    """Recursively convert an unannotated field to JSON-ready data."""
    if is_dataclass(value) and not isinstance(value, type):
        return to_dict(value)
    if isinstance(value, (list, tuple, set)):
        return [_plain(item) for item in value]
    return value
