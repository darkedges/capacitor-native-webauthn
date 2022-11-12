package com.darkedges.webauthn;

import static com.google.android.gms.fido.Fido.getFido2ApiClient;

import android.app.Activity;
import android.app.PendingIntent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorResponse;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

@CapacitorPlugin(name = "WebAuthn")
public class WebAuthnPlugin extends Plugin {
    private WebAuthn implementation = new WebAuthn();
    ActivityResultLauncher createCredentialIntentLauncher;

    @Override
    public void load() {
        super.load();
        // Obtain the Fido2ApiClient instance.
        implementation.setFido2APICLient(getFido2ApiClient(bridge.getActivity()));
        Log.i("intent", "1a");
        createCredentialIntentLauncher = bridge.registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                result -> verifyResult(result));
        Log.i("intent", "1b");
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void isWebAuthnAvailable(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.isWebAuthnAvailable());
        call.resolve(ret);
    }

    @PluginMethod
    public void isWebAuthnAutoFillAvailable(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.isWebAuthnAutoFillAvailable());
        call.resolve(ret);
    }

    @PluginMethod
    public void startRegistration(PluginCall call) {
        try {
            PublicKeyCredentialCreationOptions.Builder builder = new PublicKeyCredentialCreationOptions.Builder()
                    .setUser(WebAuthnUtil.parseUser(call))
                    .setChallenge(WebAuthnUtil.parseChallenge(call))
                    .setParameters(WebAuthnUtil.parseParameters(call))
                    .setTimeoutSeconds(WebAuthnUtil.parseTimeoutSeconds(call))
                    .setAttestationConveyancePreference(WebAuthnUtil.parseAttestation(call))
                    .setExcludeList(WebAuthnUtil.parseExcludeList(call))
                    //.setAuthenticatorSelection(WebAuthnUtil.parseAuthenticatorSelection(call))
                    .setRp(WebAuthnUtil.parseRp(call));
            PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions = builder.build();
            Log.d("intent", publicKeyCredentialCreationOptions.getAttestationConveyancePreference().toString());
            Log.d("intent", new String(publicKeyCredentialCreationOptions.getChallenge()));
            Log.d("intent", new String(publicKeyCredentialCreationOptions.getUser().getId()));
            Log.d("intent", publicKeyCredentialCreationOptions.getUser().getName());
            Log.d("intent", publicKeyCredentialCreationOptions.getUser().getDisplayName());
            Log.d("intent: Rp.Id:   ", publicKeyCredentialCreationOptions.getRp().getId());
            Log.d("intent: Rp.Name: ", publicKeyCredentialCreationOptions.getRp().getName());
            Log.d("intent", "" + publicKeyCredentialCreationOptions.getParameters().get(0).getType());
            Log.d("intent", "" + publicKeyCredentialCreationOptions.getParameters().get(0).getAlgorithm().describeContents());
            Log.d("intent", "" + publicKeyCredentialCreationOptions.getParameters().get(1).getType());
            Log.d("intent", "" + publicKeyCredentialCreationOptions.getParameters().get(1).getAlgorithm().describeContents());
            Log.i("intent", "1");
            Task result = implementation.getIntent(publicKeyCredentialCreationOptions);
            Log.i("Intent", "2");
            result.addOnSuccessListener(
                    new OnSuccessListener() {

                        @Override
                        public void onSuccess(Object o) {
                            if (o != null) {
                                Log.i("Intent", "All good");
                                PendingIntent pendingIntent = (PendingIntent) o;
                                //bridge.saveCall(call);
                                createCredentialIntentLauncher.launch(new IntentSenderRequest.Builder(pendingIntent).build());
                            }
                        }
                    });
            result.addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Intent", "All bad");
                        }
                    });
//            bridge.saveCall(call);
//            startActivityForResult(call, intent.getResult(), "verifyResult");

            JSObject ret = new JSObject();
            ret.put("value", false);
            call.resolve(ret);
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("value", "Failed");
            call.resolve(ret);
        }
    }

    private void verifyResult(ActivityResult activityResult) {
        Log.i("Intent", "verifyResult");
        Log.i("Intent", "verifyResult: "+activityResult.getResultCode());
        Log.i("Intent", "verifyResult: "+activityResult.getData());
        byte[] bytes = activityResult.getData().getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA);
        if (activityResult.getResultCode() != Activity.RESULT_OK) {
            Log.i("Intent", "verifyResult: cancelled");
        }
        if (bytes==null) {
            Log.i("Intent", "verifyResult: credential_error");
        } else {
            PublicKeyCredential credential = PublicKeyCredential.deserializeFromBytes(bytes);
            AuthenticatorResponse response = credential.getResponse();
            if (response instanceof AuthenticatorErrorResponse) {
                Log.e("Intent", "verifyResult: " + ((AuthenticatorErrorResponse) response).getErrorMessage());
            } else {
                Log.i("Intent", "verifyResult: all good");
            }
        }
    }
}
