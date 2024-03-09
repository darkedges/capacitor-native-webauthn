import Foundation
import Capacitor
import AuthenticationServices

enum Attachment: String {
    case CROSSPLATFORM = "crossplatform"
    case PLATFORM = "platform"
}

enum PasskeyError: String {
    case MISSING_ATTESTATION_OBJECT_ERROR = "MISSING_ATTESTATION_OBJECT"
    case USER_CANCELED_ERROR = "USER_CANCELED"
    case INVALID_RESPONSE_ERROR = "INVALID_RESPONSE"
    case NOT_HANDLED_ERROR = "NOT_HANDLED"
    case FAILED_ERROR = "FAILED"
    case NOT_INTERACTIVE_ERROR = "NOT_INTERACTIVE"
    case UNKNOWN = "UKNOWN"
}

class GetAppleSignInHandler: NSObject, ASAuthorizationControllerDelegate {
    var call: CAPPluginCall
    var window : UIWindow;
    
    init(call: CAPPluginCall, window:UIWindow) {
        self.call = call
        self.window = window
        super.init()
    }
    
    func register() {
        let _challenge = Data(call.getString("challenge")!.utf8)
        let _username: String = call.getObject("user")?["name"] as! String
        let _userId = Data((call.getObject("user")?["id"] as! String).utf8)
        let _rp: String = call.getObject("rp")?["id"] as! String
        let platformProvider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier: _rp)
        let platformKeyRequest = platformProvider.createCredentialRegistrationRequest(challenge: _challenge, name: _username, userID: _userId)
        let authController = ASAuthorizationController(authorizationRequests: [platformKeyRequest])
        authController.delegate = self
        authController.presentationContextProvider = self
        authController.performRequests()
    }
    
    func authenticate() {
        let _challenge = Data(call.getString("challenge")!.utf8)
        let _rp: String = call.getString("rpId")!
        let platformProvider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier: _rp)
        let platformKeyRequest = platformProvider.createCredentialAssertionRequest(challenge: _challenge)
        let authController = ASAuthorizationController(authorizationRequests: [platformKeyRequest])
        authController.delegate = self
        authController.presentationContextProvider = self
        authController.performRequests()
    }
    
    func getAuthenticatorAttachment(attachment: ASAuthorizationPublicKeyCredentialAttachment) -> String {
        var type = Attachment.PLATFORM.rawValue
        if attachment.rawValue == 1 {
            type = Attachment.CROSSPLATFORM.rawValue
        }
        return type
            
      }
    
    func URLSafe(data: String) -> String {
        return data.replacingOccurrences(of: "/", with: "_")
                        .replacingOccurrences(of: "+", with: "-")
                        .replacingOccurrences(of: "=", with: "")
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        switch authorization.credential {
        case let credentialRegistration as ASAuthorizationPlatformPublicKeyCredentialRegistration:
            let attestationObject = URLSafe(data: credentialRegistration.rawAttestationObject!.base64EncodedString())
            let id = URLSafe(data: credentialRegistration.credentialID.base64EncodedString())
            let rawId = URLSafe(data: credentialRegistration.credentialID.base64EncodedString())
            let authenticatorAttachment = getAuthenticatorAttachment(attachment: credentialRegistration.attachment)
            let type = "public-key"
            let clientDataJSON = URLSafe(data: credentialRegistration.rawClientDataJSON.base64EncodedString())
            let transports: [String] = ["internal"]
            call.resolve([
                "rawId": rawId,
                "authenticatorAttachment": authenticatorAttachment,
                "type": type,
                "id": id,
                "response": [
                    "transports": transports,
                    "clientDataJSON": clientDataJSON,
                    "authenticatorData": attestationObject
                ]
            ])
        case let credentialAssertion as ASAuthorizationPlatformPublicKeyCredentialAssertion:
            print("A passkey was used to sign in: \(credentialAssertion)")
            // Verify the below signature and clientDataJSON with your service for the given userID.
            let id = URLSafe(data: credentialAssertion.credentialID.base64EncodedString())
            let rawId = URLSafe(data: credentialAssertion.credentialID.base64EncodedString())
            let type = "public-key"
            let authenticatorAttachment = getAuthenticatorAttachment(attachment: credentialAssertion.attachment)
            let clientDataJSON = URLSafe(data: credentialAssertion.rawClientDataJSON.base64EncodedString())
            let authenticatorData = URLSafe(data: credentialAssertion.rawAuthenticatorData.base64EncodedString())
            let signature = URLSafe(data: credentialAssertion.signature.base64EncodedString())
            let userHandle = URLSafe(data: credentialAssertion.userID.base64EncodedString())
            // After the server verifies the assertion, sign in the user.
            call.resolve([
                "rawId": rawId,
                "authenticatorAttachment": authenticatorAttachment,
                "type": type,
                "id": id,
                "response": [
                    "clientDataJSON": clientDataJSON,
                    "authenticatorData": authenticatorData,
                    "signature": signature,
                    "userHandle": userHandle
                ]
            ])
        default:
            call.reject(PasskeyError.UNKNOWN.rawValue)
        }
    }
    
    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        guard let authorizationError = error as? ASAuthorizationError else {
            call.reject(PasskeyError.UNKNOWN.rawValue)
            return
        }
        var errorDescription: String =
            switch authorizationError.code {
                case .notInteractive:
                    PasskeyError.USER_CANCELED_ERROR.rawValue
                case .failed:
                   PasskeyError.FAILED_ERROR.rawValue
                case .invalidResponse:
                     PasskeyError.INVALID_RESPONSE_ERROR.rawValue
                case .notHandled:
                    PasskeyError.NOT_HANDLED_ERROR.rawValue
                case .canceled:
                    PasskeyError.USER_CANCELED_ERROR.rawValue
                case .unknown:
                    PasskeyError.UNKNOWN.rawValue
                default:
                    PasskeyError.UNKNOWN.rawValue
            }
        call.reject(errorDescription)
    }
}

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(WebAuthnPlugin)
public class WebAuthnPlugin: CAPPlugin {
    private let implementation = WebAuthn()
    var signInHandler: GetAppleSignInHandler?
    
    override public func load() {
        implementation.setCredentialManager("hello");
    }
    
    @objc func isWebAuthnAvailable(_ call: CAPPluginCall) {
        call.resolve([
            "isWebAuthnAvailable": implementation.isWebAuthnAvailable()
        ])
    }
    
    @objc func isWebAuthnAutoFillAvailable(_ call: CAPPluginCall) {
        call.resolve([
            "isWebAuthnAutoFillAvailable": implementation.isWebAuthnAutoFillAvailable()
        ])
    }
    
    @objc func startRegistration(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.signInHandler = GetAppleSignInHandler(call: call, window: (self.bridge?.webView?.window)!)
            self.signInHandler?.register()
        }
    }
    
    @objc func startAuthentication(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.signInHandler = GetAppleSignInHandler(call: call, window: (self.bridge?.webView?.window)!)
            self.signInHandler?.authenticate()
        }
    }

}

extension GetAppleSignInHandler: ASAuthorizationControllerPresentationContextProviding {
    public func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        return self.window
  }
}

