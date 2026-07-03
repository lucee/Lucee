# LDEV-6372 — Exception template: use createAISession instead of LuceeCreateAISession alias

| Field | Value |
|-------|-------|
| **Type** | Bug / Task |
| **Priority** | Medium |
| **Affects version(s)** | 7.0.x (7.0 branch) |
| **Component/s** | AI, Debug templates, FLD |
| **Related** | [LDEV-6256](https://luceeserver.atlassian.net/browse/LDEV-6256) — function alias comma-split fix |

---

## Summary

The modern exception template **Analyse** action calls `LuceeCreateAISession()`, an FLD alias of `CreateAISession`. On 7.0 builds between the introduction of comma-separated function aliases and **LDEV-6256**, that alias is not registered as a separate function (the whole string `LuceeCreateAISession,aiCreateSession` is stored as one name). Users see:

```
The function LUCEECREATEAISESSION does not exist
```

Core templates should call the primary function name `createAISession()` so analysis works regardless of alias registration.

---

## Root cause

1. **LDEV-5316** renamed the FLD entry from `<name>LuceeCreateAISession</name>` to `<name>CreateAISession</name>` with `<alias>LuceeCreateAISession</alias>`.
2. **Later**, the alias became `<alias>LuceeCreateAISession,aiCreateSession</alias>`.
3. Until **LDEV-6256**, `FunctionLib.setFunction()` did not split comma-separated aliases, so `LuceeCreateAISession` was not resolvable.
4. `error.cfm` and `reference.cfm` continued to use the alias name.

Function aliases are supported on 7.0; the failure is alias registration for multi-value `<alias>` strings on older patches, not missing alias support.

---

## Changes made

| File | Change |
|------|--------|
| `core/src/main/cfml/context/debug/modern/error.cfm` | `LuceeCreateAISession` → `createAISession` |
| `core/src/main/cfml/context/debug/modern/reference.cfm` | `LuceeCreateAISession` → `createAISession` |
| `core/src/main/java/resource/fld/core-base.fld` | Remove `LuceeCreateAISession` from `<alias>` (keep `aiCreateSession`); update argument descriptions |
| `test/tickets/LDEV6256.cfc` | Multi-alias tests use `arrayAppend` instead of `CreateAISession` |

---

## Acceptance criteria

- [ ] Exception template **Analyse** works on 7.0 without requiring `LuceeCreateAISession` alias.
- [ ] `createAISession()` resolves (primary FLD name `CreateAISession`).
- [ ] Optional: document that `LuceeCreateAISession` remains available only when LDEV-6256 is present and alias is defined (or deprecate alias entirely).

---

## Notes

- **Workaround for deployed servers:** rebuild with LDEV-6256+, or use single-value `<alias>LuceeCreateAISession</alias>` in FLD until upgraded.
- **Backward compatibility:** External code may still call `LuceeCreateAISession` if alias is kept in FLD and runtime includes LDEV-6256; core templates no longer depend on it.
