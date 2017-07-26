/* global module, require */
'use strict';

var exec = require('cordova/exec');

// secure copy definition
function SMSVerification(){
    this.pluginRef = 'SMSVerification';                              // *** Plugin reference for Cordova.exec calls
}

// send verification code
SMSVerification.prototype.sendVerificationCode = function (phoneNumber, zone, successCallback, errorCallback) {
                   exec(successCallback, errorCallback, this.pluginRef, 'sendVerificationCode', [phoneNumber, zone]);
};

// commit verificaiton code
SMSVerification.prototype.commitVerificationCode = function (connectionId, successCallback, errorCallback) {
	exec(successCallback, errorCallback, this.pluginRef, 'commitVerificationCode', [phoneNumber, zone, code]);
};

module.exports = new SMSVerification();
