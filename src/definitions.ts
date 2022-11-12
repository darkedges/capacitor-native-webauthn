import { PublicKeyCredentialCreationOptionsJSON, RegistrationResponseJSON } from '@simplewebauthn/typescript-types';

export interface WebAuthnPlugin {
  isWebAuthnAvailable(): Promise<{ value: boolean }>;
  isWebAuthnAutoFillAvailable(): Promise<{ value: boolean }>;
  startRegistration(publicKeyCredentialCreationOptionsJSON: PublicKeyCredentialCreationOptionsJSON): Promise<RegistrationResponseJSON>;
}
