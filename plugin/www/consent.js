var exec = require('cordova/exec')

/**
 * Consent status values.
 * Android: https://developers.google.com/admob/ump/android/api/reference/com/google/android/ump/ConsentInformation.ConsentStatus
 * iOS: https://developers.google.com/admob/ump/ios/api/reference/Enums/UMPConsentStatus
 */
var CONSENT_STATUS = {
  'UNKNOWN': 0,
  'NOT_REQUIRED': 1,
  'REQUIRED': 2,
  'OBTAINED': 3,
}

/**
 * Consent type values.
 * Android: https://developers.google.com/admob/ump/android/api/reference/com/google/android/ump/ConsentInformation.ConsentType
 * iOS: https://developers.google.com/admob/ump/ios/api/reference/Enums/UMPConsentType
 */
var CONSENT_TYPE = {
  'UNKNOWN': 0,
  'NON_PERSONALIZED': 1,
  'PERSONALIZED': 2
}

/**
 * Error codes used when loading and showing forms.
 * Android: https://developers.google.com/admob/ump/android/api/reference/com/google/android/ump/FormError.ErrorCode
 * iOS: https://developers.google.com/admob/ump/ios/api/reference/Enums/UMPFormErrorCode
 */
var FORM_ERROR = {
  'INTERNAL_ERROR': 1,
  'INTERNET_ERROR': 2,
  'INVALID_OPERATION': 3,
  'TIME_OUT': 4
}

const plugin = {
  consentStatus: CONSENT_STATUS.UNKNOWN,
  consentType: CONSENT_TYPE.UNKNOWN,
  form: {
    isLoaded: false,
    load: function () {
      return new Promise(
        function (resolve, reject) {
          exec(
            function (response) {
              plugin._update(response)

              resolve(response)
            },
            reject,
            'UMPConsentPlugin',
            'actionLoadForm',
            []
          )
        }
      )
    },
    present: function () {
      return new Promise(
        function (resolve, reject) {
          exec(
            function (response) {
              plugin._update(response)

              resolve(response)
            },
            reject,
            'UMPConsentPlugin',
            'actionPresentForm',
            []
          )
        }
      )
    },
  },
  request: function () {
    return new Promise(
      function (resolve, reject) {
        exec(
          function (response) {
            plugin._update(response)

            resolve(response)
          },
          reject,
          'UMPConsentPlugin',
          'actionRequest',
          []
        )
      }
    )
  },
  reset: function () {
    return new Promise(
      function (resolve, reject) {
        exec(
          function (response) {
            plugin._update(response)

            resolve(response)
          },
          reject,
          'UMPConsentPlugin',
          'actionReset',
          []
        )
      }
    )
  },
  _update: function (response) {
    plugin.consentStatus = response.consentStatus || CONSENT_STATUS.UNKNOWN
    plugin.consentType = response.consentType || CONSENT_TYPE.UNKNOWN
    plugin.form.isLoaded = response.formLoaded || false
  },
  CONSENT_STATUS: CONSENT_STATUS,
  CONSENT_TYPE: CONSENT_TYPE,
  FORM_ERROR: FORM_ERROR
}

module.exports = plugin
