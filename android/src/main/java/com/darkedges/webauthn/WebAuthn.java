package com.darkedges.webauthn;

import android.app.PendingIntent;
import android.util.Log;

import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;

public class WebAuthn {
    private com.google.android.gms.fido.fido2.Fido2ApiClient fido2ApiClient;

    public void setFido2APICLient(com.google.android.gms.fido.fido2.Fido2ApiClient fido2ApiClient) {
        Log.i("setFido2APICLient", String.valueOf(fido2ApiClient));
        this.fido2ApiClient=fido2ApiClient;
    }

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    public boolean isWebAuthnAvailable() {
        return isAvailable(WebAuthnTypes.WEBAUTHN);
    }
    public boolean isWebAuthnAutoFillAvailable() {
        return isAvailable(WebAuthnTypes.WEBAUTHNAUTOFILL);
    }

    private boolean isAvailable(WebAuthnTypes webAuthnType) {
        boolean val = false;
        switch(webAuthnType) {
            case WEBAUTHN:
                try {
                    Task task = fido2ApiClient.isUserVerifyingPlatformAuthenticatorAvailable();
                    Tasks.await(task);
                    val = (boolean) task.getResult();
                } catch (Exception e) {
                    // do nothing
                }
                break;
            case WEBAUTHNAUTOFILL:
                val=false;
                break;
        }
        return val;
    }

    public boolean startRegistration(PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions) throws ExecutionException, InterruptedException {
        Task<PendingIntent> intent = fido2ApiClient.getRegisterPendingIntent(publicKeyCredentialCreationOptions);
        return false;
    }

    public Task<PendingIntent> getIntent(PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions) {
        return fido2ApiClient.getRegisterPendingIntent(publicKeyCredentialCreationOptions);
    }
}
