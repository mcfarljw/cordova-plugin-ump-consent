package com.jernung.plugins.ump;

import android.util.Log;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UMPConsentPlugin extends CordovaPlugin {

  private static final String PLUGIN_NAME = "UMPConsentPlugin";

  public ConsentInformation consentInformation;
  public ConsentForm consentForm;
  public ConsentDebugSettings debugSettings;
  public ConsentRequestParameters requestParameters;

  public void initialize (CordovaInterface cordova, CordovaWebView webview) {
    super.initialize(cordova, webview);
  }

  public boolean execute (String action, JSONArray args, CallbackContext callbackContext) {
    Log.d(PLUGIN_NAME, "Action: " + action);

    switch (action) {
      case "actionLoadForm":
        actionLoadForm(callbackContext);
        return true;
      case "actionPresentForm":
        actionPresentForm(callbackContext);
        return true;
      case "actionRequest":
        actionRequest(args, callbackContext);
        return true;
      case "actionReset":
        actionReset(callbackContext);
        return true;
    }

    return false;
  }

  private void actionRequest (JSONArray args, CallbackContext callbackContext) {
//    TODO: Can be enabled to debug based on geographical location.
//    debugSettings = new ConsentDebugSettings.Builder(cordova.getContext())
//      .addTestDeviceHashedId("replace-with-device-hash-id")
//      .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//      .build();

    if (debugSettings != null) {
      requestParameters = new ConsentRequestParameters.Builder()
        .setConsentDebugSettings(debugSettings)
        .build();
    } else {
      requestParameters = new ConsentRequestParameters.Builder()
        .build();
    }

    consentInformation = UserMessagingPlatform.getConsentInformation(cordova.getContext());

    cordova.getActivity().runOnUiThread((Runnable) () -> consentInformation.requestConsentInfoUpdate(
      cordova.getActivity(),
      requestParameters,
      (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
        Log.d(PLUGIN_NAME, "Consent info updated!");

        callbackContext.success(this.getConsentResponse());
      },
      (ConsentInformation.OnConsentInfoUpdateFailureListener) formError -> callbackContext.error(formError.getErrorCode())
    ));
  }

  private void actionLoadForm (CallbackContext callbackContext) {
    Log.d(PLUGIN_NAME, "Consent form available: " + consentInformation.isConsentFormAvailable());

    if (consentInformation.isConsentFormAvailable()) {
      cordova.getActivity().runOnUiThread(() -> UserMessagingPlatform.loadConsentForm(
        cordova.getActivity(),
        consentForm -> {
          Log.d(PLUGIN_NAME, "Consent form loaded!");

          this.consentForm = consentForm;

          callbackContext.success(this.getConsentResponse());
        },
        formError -> callbackContext.error(formError.getErrorCode())
      ));
    } else {
      callbackContext.success(this.getConsentResponse());
    }
  }

  private void actionPresentForm (CallbackContext callbackContext) {
    if (this.consentForm == null) {
      callbackContext.error("Consent form has not been loaded!");

      return;
    }

    cordova.getActivity().runOnUiThread(() -> this.consentForm.show(
      cordova.getActivity(),
      formError -> {
        if (formError != null) {
          callbackContext.error(formError.getErrorCode());
        } else {
          callbackContext.success(this.getConsentResponse());
        }
      }
    ));
  }

  private void actionReset (CallbackContext callbackContext) {
    cordova.getActivity().runOnUiThread(() -> {
      this.consentInformation.reset();

      callbackContext.success(this.getConsentResponse());
    });
  }

  private JSONObject getConsentResponse () {
    JSONObject response = new JSONObject();

    try {
      response.put("consentStatus", this.consentInformation.getConsentStatus());
      response.put("consentType", this.consentInformation.getConsentType());
      response.put("formLoaded", this.consentForm != null);
    } catch (JSONException error) {
      Log.d(PLUGIN_NAME, "Error parsing consent response: " + error.getLocalizedMessage());
    }

    return response;
  }
}
