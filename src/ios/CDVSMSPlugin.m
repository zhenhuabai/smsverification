#import <Cordova/CDV.h>
#import "CDVSMSPlugin.h"
#import <SMS_SDK/SMSSDK.h>

@implementation CDVSMSPlugin: CDVPlugin

- (void)sendVerificationCode:(CDVInvokedUrlCommand*)command {
    // validating parameters
    if ([command.arguments count] < 2) {

        // triggering parameter error
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing arguments when calling 'sendVerificationCode' action."];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];

    } else {
        // running in background to avoid thread locks
        [self.commandDelegate runInBackground:^{

            __block CDVPluginResult* result= nil;
            NSString* phoneNumber = nil;
            NSString* zone = nil;

			// preparing parameters
			phoneNumber = [command.arguments objectAtIndex:0];
			zone = [command.arguments objectAtIndex:1];
			if (zone == nil || [zone length] <= 0) {
				zone = [NSString stringWithFormat:@"86"];
				NSLog(@"- zone not provided, default to 86!");
			}
			if (phoneNumber != nil && [phoneNumber length] > 0) {
				[SMSSDK getVerificationCodeByMethod:SMSGetCodeMethodSMS phoneNumber:phoneNumber zone:zone result:^(NSError *error) {
					if (!error)
					{
						NSLog(@"- Succeeded sending code to Phone:%@ by zone:%@ ", phoneNumber, zone);
						result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Code sent OK"];
					}
					else
					{
                        NSString *es = [NSString stringWithFormat:@"Error(%@)",[error localizedDescription]];
						result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:es];
						NSLog(@"- Error(%@) sending code to Phone:%@ by zone:%@ ",[error localizedDescription], phoneNumber, zone);
					}
				}];
			} else {
				//wrong phone number
				result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid phoneNumber"];
				NSLog(@"- Phone number is not valid!");
			}
            //returning callback resolution
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
    }
}


- (void)commitVerificationCode:(CDVInvokedUrlCommand*)command {
    // validating parameters
    if ([command.arguments count] < 3) {

        // triggering parameter error
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing arguments when calling 'sendVerificationCode' action."];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];

    } else {
        // running in background to avoid thread locks
        [self.commandDelegate runInBackground:^{

            __block CDVPluginResult* result= nil;
            NSString* code = nil;
            NSString* phoneNumber = nil;
            NSString* zone = nil;

			// preparing parameters
			phoneNumber = [command.arguments objectAtIndex:0];
			zone = [command.arguments objectAtIndex:1];
			code = [command.arguments objectAtIndex:2];
			if (zone == nil || [zone length] <= 0) {
				zone = [NSString stringWithFormat:@"86"];
				NSLog(@"- zone not provided, default to 86!");
			}
			if (code != nil && [code length] > 0) {
				if (phoneNumber != nil && [phoneNumber length] > 0) {
					[SMSSDK commitVerificationCode: code phoneNumber:phoneNumber zone:zone result:^(NSError *error) {
						if (!error)
						{
							NSLog(@"- Succeeded verifying code %@ with Phone:%@ by zone:%@ ", code, phoneNumber, zone);
							result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Code verified OK"];
						}
						else
						{
                            NSString *es = [NSString stringWithFormat:@"Error(%@)",[error localizedDescription]];
                            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:es];
							NSLog(@"- Error(%@) code(%@) not verified to Phone:%@ by zone:%@ ",[error localizedDescription], code, phoneNumber, zone);
						}
					}];
				} else {
					//wrong phone number
					result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid phoneNumber"];
					NSLog(@"- Phone number is not valid!");
				}
			} else {
				result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid code"];
				NSLog(@"- code is not valid!");
			}
            //returning callback resolution
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
    }
}

@end
