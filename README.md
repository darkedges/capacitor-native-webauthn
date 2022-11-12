# @darkedges/webauthn

WebAuthn

## Install

```bash
npm install @darkedges/webauthn
npx cap sync
```

## API

<docgen-index>

* [`isWebAuthnAvailable()`](#iswebauthnavailable)
* [`isWebAuthnAutoFillAvailable()`](#iswebauthnautofillavailable)
* [`startRegistration(...)`](#startregistration)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### isWebAuthnAvailable()

```typescript
isWebAuthnAvailable() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### isWebAuthnAutoFillAvailable()

```typescript
isWebAuthnAutoFillAvailable() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### startRegistration(...)

```typescript
startRegistration(publicKeyCredentialCreationOptionsJSON: PublicKeyCredentialCreationOptionsJSON) => Promise<RegistrationResponseJSON>
```

| Param                                        | Type                                                                                                      |
| -------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| **`publicKeyCredentialCreationOptionsJSON`** | <code><a href="#publickeycredentialcreationoptionsjson">PublicKeyCredentialCreationOptionsJSON</a></code> |

**Returns:** <code>Promise&lt;<a href="#registrationresponsejson">RegistrationResponseJSON</a>&gt;</code>

--------------------


### Interfaces


#### RegistrationResponseJSON

A slightly-modified RegistrationCredential to simplify working with ArrayBuffers that
are Base64URL-encoded in the browser so that they can be sent as JSON to the server.

https://w3c.github.io/webauthn/#dictdef-registrationresponsejson

| Prop                          | Type                                                                                                    |
| ----------------------------- | ------------------------------------------------------------------------------------------------------- |
| **`id`**                      | <code><a href="#base64urlstring">Base64URLString</a></code>                                             |
| **`rawId`**                   | <code><a href="#base64urlstring">Base64URLString</a></code>                                             |
| **`response`**                | <code><a href="#authenticatorattestationresponsejson">AuthenticatorAttestationResponseJSON</a></code>   |
| **`authenticatorAttachment`** | <code><a href="#authenticatorattachment">AuthenticatorAttachment</a></code>                             |
| **`clientExtensionResults`**  | <code><a href="#authenticationextensionsclientoutputs">AuthenticationExtensionsClientOutputs</a></code> |
| **`type`**                    | <code><a href="#publickeycredentialtype">PublicKeyCredentialType</a></code>                             |


#### AuthenticatorAttestationResponseJSON

A slightly-modified AuthenticatorAttestationResponse to simplify working with ArrayBuffers that
are Base64URL-encoded in the browser so that they can be sent as JSON to the server.

https://w3c.github.io/webauthn/#dictdef-authenticatorattestationresponsejson

| Prop                    | Type                                                        |
| ----------------------- | ----------------------------------------------------------- |
| **`clientDataJSON`**    | <code><a href="#base64urlstring">Base64URLString</a></code> |
| **`attestationObject`** | <code><a href="#base64urlstring">Base64URLString</a></code> |
| **`transports`**        | <code>AuthenticatorTransportFuture[]</code>                 |


#### AuthenticationExtensionsClientOutputs

| Prop                   | Type                                                                              |
| ---------------------- | --------------------------------------------------------------------------------- |
| **`appid`**            | <code>boolean</code>                                                              |
| **`credProps`**        | <code><a href="#credentialpropertiesoutput">CredentialPropertiesOutput</a></code> |
| **`hmacCreateSecret`** | <code>boolean</code>                                                              |


#### CredentialPropertiesOutput

| Prop     | Type                 |
| -------- | -------------------- |
| **`rk`** | <code>boolean</code> |


#### PublicKeyCredentialCreationOptionsJSON

A variant of PublicKeyCredentialCreationOptions suitable for JSON transmission to the browser to
(eventually) get passed into navigator.credentials.create(...) in the browser.

This should eventually get replaced with official TypeScript DOM types when WebAuthn L3 types
eventually make it into the language:

https://w3c.github.io/webauthn/#dictdef-publickeycredentialcreationoptionsjson

| Prop                         | Type                                                                                                  |
| ---------------------------- | ----------------------------------------------------------------------------------------------------- |
| **`rp`**                     | <code><a href="#publickeycredentialrpentity">PublicKeyCredentialRpEntity</a></code>                   |
| **`user`**                   | <code><a href="#publickeycredentialuserentityjson">PublicKeyCredentialUserEntityJSON</a></code>       |
| **`challenge`**              | <code><a href="#base64urlstring">Base64URLString</a></code>                                           |
| **`pubKeyCredParams`**       | <code>PublicKeyCredentialParameters[]</code>                                                          |
| **`timeout`**                | <code>number</code>                                                                                   |
| **`excludeCredentials`**     | <code>PublicKeyCredentialDescriptorJSON[]</code>                                                      |
| **`authenticatorSelection`** | <code><a href="#authenticatorselectioncriteria">AuthenticatorSelectionCriteria</a></code>             |
| **`attestation`**            | <code><a href="#attestationconveyancepreference">AttestationConveyancePreference</a></code>           |
| **`extensions`**             | <code><a href="#authenticationextensionsclientinputs">AuthenticationExtensionsClientInputs</a></code> |


#### PublicKeyCredentialRpEntity

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### PublicKeyCredentialUserEntityJSON

https://w3c.github.io/webauthn/#dictdef-publickeycredentialuserentityjson

| Prop              | Type                |
| ----------------- | ------------------- |
| **`id`**          | <code>string</code> |
| **`name`**        | <code>string</code> |
| **`displayName`** | <code>string</code> |


#### PublicKeyCredentialParameters

| Prop       | Type                                                                        |
| ---------- | --------------------------------------------------------------------------- |
| **`alg`**  | <code><a href="#cosealgorithmidentifier">COSEAlgorithmIdentifier</a></code> |
| **`type`** | <code><a href="#publickeycredentialtype">PublicKeyCredentialType</a></code> |


#### PublicKeyCredentialDescriptorJSON

https://w3c.github.io/webauthn/#dictdef-publickeycredentialdescriptorjson

| Prop             | Type                                                                        |
| ---------------- | --------------------------------------------------------------------------- |
| **`id`**         | <code><a href="#base64urlstring">Base64URLString</a></code>                 |
| **`type`**       | <code><a href="#publickeycredentialtype">PublicKeyCredentialType</a></code> |
| **`transports`** | <code>AuthenticatorTransportFuture[]</code>                                 |


#### AuthenticatorSelectionCriteria

| Prop                          | Type                                                                                |
| ----------------------------- | ----------------------------------------------------------------------------------- |
| **`authenticatorAttachment`** | <code><a href="#authenticatorattachment">AuthenticatorAttachment</a></code>         |
| **`requireResidentKey`**      | <code>boolean</code>                                                                |
| **`residentKey`**             | <code><a href="#residentkeyrequirement">ResidentKeyRequirement</a></code>           |
| **`userVerification`**        | <code><a href="#userverificationrequirement">UserVerificationRequirement</a></code> |


#### AuthenticationExtensionsClientInputs

| Prop                   | Type                 |
| ---------------------- | -------------------- |
| **`appid`**            | <code>string</code>  |
| **`credProps`**        | <code>boolean</code> |
| **`hmacCreateSecret`** | <code>boolean</code> |


### Type Aliases


#### Base64URLString

An attempt to communicate that this isn't just any string, but a Base64URL-encoded string

<code>string</code>


#### AuthenticatorTransportFuture

A super class of TypeScript's `AuthenticatorTransport` that includes support for the latest
transports. Should eventually be replaced by TypeScript's when TypeScript gets updated to
know about it (sometime after 4.6.3)

<code>'ble' | 'internal' | 'nfc' | 'usb' | 'cable' | 'hybrid'</code>


#### AuthenticatorAttachment

<code>"cross-platform" | "platform"</code>


#### PublicKeyCredentialType

<code>"public-key"</code>


#### COSEAlgorithmIdentifier

<code>number</code>


#### ResidentKeyRequirement

<code>"discouraged" | "preferred" | "required"</code>


#### UserVerificationRequirement

<code>"discouraged" | "preferred" | "required"</code>


#### AttestationConveyancePreference

<code>"direct" | "enterprise" | "indirect" | "none"</code>

</docgen-api>
