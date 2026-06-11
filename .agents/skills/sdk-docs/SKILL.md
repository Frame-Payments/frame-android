---
name: sdk-docs
description: How to author KDoc comments on public symbols in frame-android. Loaded automatically when an agent is writing or editing KDoc in this repo.
---

# frame-android — KDoc authoring

KDoc on public symbols is the source of truth for the Frame Android SDK reference docs. Dokka reads these comments to generate HTML output hosted at the stable URL linked from the Frame SDK orientation page.

## Central standards (read first)

Cross-SDK style, terminology, and naming live in `frame-docs/lib/sdk-docs-standards/`. They override anything in this file.

1. **Local first** — if `~/Development/frame-docs/lib/sdk-docs-standards/` exists, read `STYLE.md`, `EXAMPLES.md`, and `CROSS_SDK_NAMING.md` from there.
2. **GitHub fallback** — otherwise fetch from `https://github.com/Frame-Payments/frame-docs/tree/main/lib/sdk-docs-standards/` (requires `gh` auth; the repo is private).
3. **If absent** — those documents don't exist yet. Use this file as the baseline until they do.

## Public surface

Every `public` or `open` Kotlin symbol in the three SDK modules requires a KDoc block:

* **FrameSDK** (`com.framepayments.framesdk`) — API classes, data classes, request/response objects, enums, interfaces.
* **FrameSDK-UI** (`com.framepayments.framesdk_ui`) — public Compose components, view models, validation helpers.
* **FrameSDK-Onboarding** (`com.framepayments.frameonboarding`) — public onboarding classes, view models, Compose screens exposed to integrators.

Internal-only classes (`private`, `internal`) do not require KDoc. If an internal type surfaces in a public signature, document it at the use site with a `@see` link rather than adding KDoc to the internal symbol.

## Format

All public symbols require a `/** ... */` KDoc block. Use `//` single-line comments only for implementation notes inside function bodies.

```kotlin
/**
 * A brief one-line summary ending with a period.
 *
 * Extended discussion when needed. Keep it concise and caller-focused.
 *
 * @param param Description of this parameter.
 * @return What the function returns.
 * @throws ExceptionType When this is thrown.
 */
public fun exampleFunction(param: String): Boolean { ... }
```

For data classes, document the class itself and each constructor property:

```kotlin
/**
 * Options for presenting the checkout sheet.
 *
 * @property clientSecret Short-lived token from the merchant's `POST /charge-intents`. Always starts with `cs_`.
 * @property items Line items to display in the cart.
 */
public data class CheckoutOptions(
    val clientSecret: String,
    val items: List<CartItem>,
)
```

## Summary line

- One sentence, active voice. Functions: imperative ("Submits the payment"). Types/properties: noun phrase ("A checkout flow coordinator").
- End with a period.
- No redundant prefixes like "This class…" or "A function that…".

## Tags

* **`@param`** / **`@property`** — every parameter and constructor property on public API. Say what the value means, not just its type.
* **`@return`** — what the caller gets back. Omit only for `Unit`-returning functions with no meaningful result.
* **`@throws`** — every exception the function can propagate to callers.
* **`@sample`** — at least one realistic merchant-side calling example for entry-point classes and functions.
* **`@see`** — link related symbols using `[ClassName]` or `[ClassName.methodName]` syntax.
* **`@deprecated`** — when retiring, include migration guidance inline.

## Voice and terminology (baseline; central standards override)

* Active voice, caller-facing perspective. "Presents the checkout sheet." Not "The checkout sheet is presented."
* **Merchant** = the integrating app developer. **Customer** = the merchant's end user.
* **Card** (not "credit card" — debit is supported).
* **Publishable key** for `pk_...`, **secret key** for `sk_...`, **client secret** for `cs_...`.
* Examples use `pk_test_...` / `sk_test_...`, never live keys.
* When behavior differs between API levels or SDK versions, use `@throws` or inline prose rather than leaving it undocumented.

## Cross-SDK naming

Match naming used in `frame-ios` and `frame-js` / `frame-react-native` for equivalent concepts. Check `CROSS_SDK_NAMING.md` before introducing new terminology.

## What NOT to document

- Implementation details or internal state invisible to callers.
- Truly self-evident properties where the name carries the full contract and there are no constraints, side effects, or non-obvious nullability to describe. Note: Detekt enforces `UndocumentedPublicProperty` with no exceptions — use `@Suppress("UndocumentedPublicProperty")` on the declaration if you genuinely need to skip one, and leave a code comment explaining why.
- Private or internal symbols.

## Existing symbols

Existing public symbols without KDoc are tracked in [FRA-3959](https://linear.app/framepayments/issue/FRA-3959) (KDoc backfill). When touching an undocumented symbol, add a KDoc block. New public symbols must always have KDoc — CI will fail otherwise (Detekt `UndocumentedPublicClass` / `UndocumentedPublicFunction` / `UndocumentedPublicProperty`).

## Lint gate

Detekt KDoc rules are enforced on this repo. CI fails on any public symbol missing a `/** ... */` block. Run `./gradlew detekt` locally before pushing.
