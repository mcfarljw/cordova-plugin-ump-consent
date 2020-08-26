import UserMessagingPlatform

@objc(UMPConsentPlugin)
class UMPConsentPlugin : CDVPlugin {
    var consentForm: UMPConsentForm?
    var requestParameters = UMPRequestParameters()

     @objc(actionRequest:)
     private func actionRequest(command: CDVInvokedUrlCommand) {
        let callbackId = command.callbackId

//        TODO: Can be enabled to debug based on geographical location.
//        let debugSettings = UMPDebugSettings()
//        debugSettings.testDeviceIdentifiers = ["ABB898CE-3556-4919-998B-9E5FD9C309AC"]
//        debugSettings.geography = UMPDebugGeography.EEA
//        requestParameters.debugSettings = debugSettings

        requestParameters.tagForUnderAgeOfConsent = false

        UMPConsentInformation.sharedInstance.requestConsentInfoUpdate(
        with: requestParameters,
        completionHandler: { error in
            if error != nil {
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error?.localizedDescription), callbackId: callbackId)
            } else {
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: self.getConsentResponse()), callbackId: callbackId)
            }
        })
     }

    @objc(actionLoadForm:)
    private func actionLoadForm(command: CDVInvokedUrlCommand) {
        let callbackId = command.callbackId

        if (UMPConsentInformation.sharedInstance.formStatus == UMPFormStatus.available) {
            UMPConsentForm.load(
            completionHandler: { form, loadError in
                if loadError != nil {
                    self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: loadError?.localizedDescription), callbackId: callbackId)
                } else {
                    self.consentForm = form
                    self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: self.getConsentResponse()), callbackId: callbackId)
                }
            })
        } else {
            self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: self.getConsentResponse()), callbackId: callbackId)
        }
    }

    @objc(actionPresentForm:)
    private func actionPresentForm(command: CDVInvokedUrlCommand) {
        let callbackId = command.callbackId

        if (consentForm != nil) {
            consentForm?.present(
                from: self.viewController,
                completionHandler: { dismissError in
                    if dismissError != nil {
                        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: dismissError?.localizedDescription), callbackId: callbackId)
                    } else {
                        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: self.getConsentResponse()), callbackId: callbackId)
                    }

                })
        } else {
            self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Form unavailable!"), callbackId: callbackId)
        }
    }

    @objc(actionReset:)
    private func actionReset(command: CDVInvokedUrlCommand) {
        let callbackId = command.callbackId

        UMPConsentInformation.sharedInstance.reset()

        consentForm = nil

        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: self.getConsentResponse()), callbackId: callbackId)
    }

    private func getConsentResponse() -> [String : Any] {
        var response: [String : Any] = [:]

        response["consentStatus"] = UMPConsentInformation.sharedInstance.consentStatus.rawValue
        response["consentType"] = UMPConsentInformation.sharedInstance.consentType.rawValue
        response["formLoaded"] = consentForm != nil

        return response
    }
}
