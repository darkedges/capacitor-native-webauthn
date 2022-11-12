package com.darkedges.webauthn;

import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.gms.fido.common.Transport;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AttestationConveyancePreference;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WebAuthnUtil {
    public static PublicKeyCredentialUserEntity parseUser(PluginCall call) {
        byte[] id = call.getObject("user").getString("id").getBytes();
        String name = call.getObject("user").getString("name");
        String displayName = call.getObject("user").getString("displayName");
        return new PublicKeyCredentialUserEntity(id, name, null, displayName);
    }

    public static byte[] parseChallenge(PluginCall call) {
        return call.getString("challenge").getBytes();
    }

    public static List<PublicKeyCredentialParameters> parseParameters(PluginCall call) throws JSONException {
        List<PublicKeyCredentialParameters> parameters = new ArrayList<PublicKeyCredentialParameters>();
        JSArray array = call.getArray("pubKeyCredParams");
        for (int i = 0; i < array.length(); i++) {
            JSONObject pubKeyCredParam = (JSONObject) array.get(i);
            String type = pubKeyCredParam.getString("type");
            int alg = pubKeyCredParam.getInt("alg");
            parameters.add(new PublicKeyCredentialParameters(type, alg));
        }
        return parameters;
    }

    public static Double parseTimeoutSeconds(PluginCall call) {
        return call.getDouble("timeout");
    }

    public static List<PublicKeyCredentialDescriptor> parseExcludeList(PluginCall call) throws JSONException, Transport.UnsupportedTransportException {
        List<PublicKeyCredentialDescriptor> list = new ArrayList<PublicKeyCredentialDescriptor>();
//        JSArray array = call.getArray("excludeCredentials");
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject excludeCredential = (JSONObject) array.get(i);
//            byte[] id = excludeCredential.getString("id").getBytes();
//            String type = excludeCredential.getString("type");
//            JSONArray transports = excludeCredential.getJSONArray("transports");
//            List<Transport> transportList = new ArrayList<Transport>();
//            for (int j = 0; j < transports.length(); j++) {
//                try {
//                    transportList.add(Transport.fromString(transports.get(j).toString()));
//                } catch (Exception e) {
//
//                }
//            }
//            list.add(
//                    new PublicKeyCredentialDescriptor(
//                            type,
//                            id,
//                            transportList
//                    ));
//        }
        return list;
    }

    public static AuthenticatorSelectionCriteria parseAuthenticatorSelection(PluginCall call) throws JSONException, Attachment.UnsupportedAttachmentException {
        AuthenticatorSelectionCriteria.Builder builder = new AuthenticatorSelectionCriteria.Builder();
//        builder.setAttachment(Attachment.fromString(""));
//        builder.setRequireResidentKey(call.getObject("authenticatorSelection").getBoolean("requireResidentKey"));
        builder.setAttachment(Attachment.fromString("platform"));
        builder.setRequireResidentKey(false);
        return builder.build();
    }

    public static PublicKeyCredentialRpEntity parseRp(PluginCall call) {
        String id = call.getObject("rp").getString("id");
        String name = call.getObject("rp").getString("name");
        return new PublicKeyCredentialRpEntity(id, name, null);
    }

    public static AttestationConveyancePreference parseAttestation(PluginCall call) throws AttestationConveyancePreference.UnsupportedAttestationConveyancePreferenceException {
        return AttestationConveyancePreference.fromString(call.getString("attestation"));
    }
}
