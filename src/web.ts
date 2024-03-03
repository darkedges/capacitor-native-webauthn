import { WebPlugin } from '@capacitor/core';
import { browserSupportsWebAuthn, browserSupportsWebAuthnAutofill, startAuthentication, startRegistration } from '@simplewebauthn/browser';
import { AuthenticationResponseJSON, PublicKeyCredentialCreationOptionsJSON, PublicKeyCredentialRequestOptionsJSON, RegistrationResponseJSON } from '@simplewebauthn/types';

import type { WebAuthnPlugin } from './definitions';

export class WebAuthnWeb extends WebPlugin implements WebAuthnPlugin {

  async startRegistration(publicKeyCredentialCreationOptionsJSON: PublicKeyCredentialCreationOptionsJSON): Promise<RegistrationResponseJSON> {
    return startRegistration(publicKeyCredentialCreationOptionsJSON);
  }

  async startAuthentication(requestOptionsJSON: PublicKeyCredentialRequestOptionsJSON, useBrowserAutofill?: boolean): Promise<AuthenticationResponseJSON> {
    return startAuthentication(requestOptionsJSON, useBrowserAutofill);
  }

  async isWebAuthnAvailable(): Promise<{ value: boolean }> {
    return this.isAvailable('webauthn');
  }

  async isWebAuthnAutoFillAvailable(): Promise<{ value: boolean }> {
    return this.isAvailable('webauthnautofill');
  }

  private async isAvailable(type: 'webauthn' | 'webauthnautofill'): Promise<{ value: boolean }> {
    let val = false;
    if (type === 'webauthn') {
      val = await browserSupportsWebAuthn();
    }
    if (type === 'webauthnautofill') {
      val = await browserSupportsWebAuthnAutofill();
    }
    return Promise.resolve({ value: val });
  }

}
