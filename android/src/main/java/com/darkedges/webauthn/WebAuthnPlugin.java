package com.darkedges.webauthn;

import android.os.CancellationSignal;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePublicKeyCredentialRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPasswordOption;
import androidx.credentials.GetPublicKeyCredentialOption;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.CreateCredentialCancellationException;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.CreateCredentialInterruptedException;
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException;
import androidx.credentials.exceptions.CreateCredentialUnknownException;
import androidx.credentials.exceptions.CreateCustomCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONException;


@CapacitorPlugin(name = "WebAuthn")
public class WebAuthnPlugin extends Plugin {
  private final WebAuthn implementation = new WebAuthn();
  ActivityResultLauncher createCredentialIntentLauncher;

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
  public void startAuthentication(PluginCall call) {
    String requestJson = call.getData().toString();
    // Retrieves the user's saved password for your app from their
    // password provider.
    GetPasswordOption getPasswordOption = new GetPasswordOption();

    // Get passkeys from the user's public key credential provider.
    String clientDataHash = null;
    GetPublicKeyCredentialOption getPublicKeyCredentialOption =
      new GetPublicKeyCredentialOption(requestJson, clientDataHash);
    GetCredentialRequest getCredRequest = new GetCredentialRequest.Builder()
      .addCredentialOption(getPasswordOption)
      .addCredentialOption(getPublicKeyCredentialOption)
      .build();
    CancellationSignal cancellationSignal = null;
    implementation.getCredentialManager().getCredentialAsync(
      getCredRequest,
      getActivity(),
      cancellationSignal,
      getContext().getMainExecutor(),
      new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
        @Override
        public void onResult(GetCredentialResponse result) {
          // Handle the successfully returned credential.
          Credential credential = result.getCredential();
          if (credential instanceof PublicKeyCredential) {
            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
            fidoAuthenticateToServer(call, responseJson);
          } else if (credential instanceof PasswordCredential) {
            String id = ((PasswordCredential) credential).getId();
            String password = ((PasswordCredential) credential).getPassword();
            firebaseSignInWithPassword(call, id, password);
          } else {
            handlePasskeyError(call, "Unexpected type of credential", new Exception(credential.getClass().getName()));
          }
        }

        @Override
        public void onError(GetCredentialException e) {
          handlePasskeyError(call, "Sign in failed with exception", e);
        }
      }
    );
  }

  @PluginMethod
  public void startRegistration(PluginCall call) {
    String requestJson = call.getData().toString();
    String clientDataHash = null;
    CreatePublicKeyCredentialRequest createPublicKeyCredentialRequest =
      // `requestJson` contains the request in JSON format. Uses the standard
      // WebAuthn web JSON spec.
      // `preferImmediatelyAvailableCredentials` defines whether you prefer
      // to only use immediately available credentials, not  hybrid credentials,
      // to fulfill this request. This value is false by default.
      new CreatePublicKeyCredentialRequest(requestJson, clientDataHash);
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
          handleSuccessfulCreatePasskeyResult(call, result);
        }

        @Override
        public void onError(CreateCredentialException e) {
          if (e instanceof CreatePublicKeyCredentialDomException) {
            // Handle the passkey DOM errors thrown according to the
            // WebAuthn spec.
            handlePasskeyError(call, "CreatePublicKeyCredentialDomException", e);
          } else if (e instanceof CreateCredentialCancellationException) {
            // The user intentionally canceled the operation and chose not
            // to register the credential.
            handlePasskeyError(call, "CreatePublicKeyCredentialDomException", e);
          } else if (e instanceof CreateCredentialInterruptedException) {
            // Retry-able error. Consider retrying the call.
            handlePasskeyError(call, "CreateCredentialInterruptedException", e);
          } else if (e instanceof CreateCredentialProviderConfigurationException) {
            // Your app is missing the provider configuration dependency.
            // Most likely, you're missing the
            // "credentials-play-services-auth" module.
            handlePasskeyError(call, "CreateCredentialProviderConfigurationException", e);
          } else if (e instanceof CreateCredentialUnknownException) {
            handlePasskeyError(call, "CreateCredentialUnknownException", e);
          } else if (e instanceof CreateCustomCredentialException) {
            // You have encountered an error from a 3rd-party SDK. If
            // you make the API call with a request object that's a
            // subclass of
            // CreateCustomCredentialRequest using a 3rd-party SDK,
            // then you should check for any custom exception type
            // constants within that SDK to match with e.type.
            // Otherwise, drop or log the exception.
            handlePasskeyError(call, "CreateCustomCredentialException", e);
          } else {
            handlePasskeyError(call, "Unexpected exception type", e);
          }
        }
      });
  }

  private void firebaseSignInWithPassword(PluginCall call, String id, String password) {
    handlePasskeyError(call, "Unexpected type of credential", new Exception("Firebase Credentials found."));
  }

  private void fidoAuthenticateToServer(PluginCall call, String responseJson) {
    JSObject ret = null;
    try {
      ret = new JSObject(responseJson);
    } catch (JSONException e) {
      call.reject("Failed to get passkey", e);
    }
    call.resolve(ret);
  }

  private void handlePasskeyError(PluginCall call, String type, Exception e) {
    JSObject ret = new JSObject();
    ret.put(type, e.getMessage());
    call.reject(type,ret);
  }

  private void handleSuccessfulCreatePasskeyResult(PluginCall call, CreateCredentialResponse createCredentialResponse) {
    JSObject ret = null;
    try {
      ret = new JSObject(createCredentialResponse.getData().getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON"));
    } catch (JSONException e) {
      call.reject("Failed to get passkey", e);
    }
    call.resolve(ret);
  }
}
