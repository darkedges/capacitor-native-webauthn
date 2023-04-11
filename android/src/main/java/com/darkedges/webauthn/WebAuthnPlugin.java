package com.darkedges.webauthn;

import android.app.Activity;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePublicKeyCredentialRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.CreateCredentialCancellationException;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.CreateCredentialInterruptedException;
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException;
import androidx.credentials.exceptions.CreateCredentialUnknownException;
import androidx.credentials.exceptions.CreateCustomCredentialException;
import androidx.credentials.exceptions.domerrors.DomError;
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;


@CapacitorPlugin(name = "WebAuthn")
public class WebAuthnPlugin extends Plugin {
  private final WebAuthn implementation = new WebAuthn();
  ActivityResultLauncher createCredentialIntentLauncher;
  private String TAG = "keypass";
  @Override
  public void load() {
    super.load();
    // Use your app or activity context to instantiate a client instance of CredentialManager.
    implementation.setCredentialManager(CredentialManager.create(bridge.getActivity().getApplicationContext()));
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
    Log.d(TAG, "startRegistration");
    Log.d(TAG, call.getData().toString());
    String requestJson = call.getData().toString();
    boolean preferImmediatelyAvailableCredentials = false;
    CreatePublicKeyCredentialRequest createPublicKeyCredentialRequest =
      // `requestJson` contains the request in JSON format. Uses the standard
      // WebAuthn web JSON spec.
      // `preferImmediatelyAvailableCredentials` defines whether you prefer
      // to only use immediately available credentials, not  hybrid credentials,
      // to fulfill this request. This value is false by default.
      new CreatePublicKeyCredentialRequest(
        requestJson, preferImmediatelyAvailableCredentials);
    Log.d(TAG, createPublicKeyCredentialRequest.getDisplayInfo().getUserDisplayName());
    // Execute CreateCredentialRequest asynchronously to register credentials
    // for a user account. Handle success and failure cases with the result and
    // exceptions, respectively.
    CancellationSignal cancellationSignal = null;
    implementation.getCredentialManager().createCredentialAsync(
      createPublicKeyCredentialRequest, getActivity(),
      cancellationSignal,
      getContext().getMainExecutor(),
      new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
        @Override
        public void onResult(CreateCredentialResponse result) {
          handleSuccessfulCreatePasskeyResult(call,result);
        }

        @Override
        public void onError(CreateCredentialException e) {
          if (e instanceof CreatePublicKeyCredentialDomException) {
            // Handle the passkey DOM errors thrown according to the
            // WebAuthn spec.
            handlePasskeyError(call,"CreatePublicKeyCredentialDomException",e);
          } else if (e instanceof CreateCredentialCancellationException) {
            // The user intentionally canceled the operation and chose not
            // to register the credential.
            handlePasskeyError(call,"CreatePublicKeyCredentialDomException",e);
          } else if (e instanceof CreateCredentialInterruptedException) {
            // Retry-able error. Consider retrying the call.
            handlePasskeyError(call,"CreateCredentialInterruptedException",e);
          } else if (e instanceof CreateCredentialProviderConfigurationException) {
            // Your app is missing the provider configuration dependency.
            // Most likely, you're missing the
            // "credentials-play-services-auth" module.
            handlePasskeyError(call,"CreateCredentialProviderConfigurationException",e);
          } else if (e instanceof CreateCredentialUnknownException) {
            handlePasskeyError(call,"CreateCredentialUnknownException",e);
          } else if (e instanceof CreateCustomCredentialException) {
            // You have encountered an error from a 3rd-party SDK. If
            // you make the API call with a request object that's a
            // subclass of
            // CreateCustomCredentialRequest using a 3rd-party SDK,
            // then you should check for any custom exception type
            // constants within that SDK to match with e.type.
            // Otherwise, drop or log the exception.
            handlePasskeyError(call,"CreateCustomCredentialException",e);
          } else {
            Log.d("passkey", "Unexpected exception type "
              + e.getClass().getName());
          }
        }
      });

    JSObject ret = new JSObject();
    ret.put("value", false);
    call.resolve(ret);
  }

  private void handlePasskeyError(PluginCall call, String type, Exception e) {
    Log.d(TAG, type+": "+e.getMessage());
    JSObject ret = new JSObject();
    ret.put(TAG, type+": "+e.getMessage());
    call.resolve(ret);
  }

  private void handleSuccessfulCreatePasskeyResult(PluginCall call,CreateCredentialResponse e) {
    Log.d(TAG, "CreateCredentialResponse: "+e.getData());
    JSObject ret = new JSObject();
    ret.put(TAG, "CreateCredentialResponse: "+e.getData());
    call.resolve(ret);
  }
}
