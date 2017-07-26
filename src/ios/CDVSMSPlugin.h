#import <Cordova/CDV.h>

@interface CDVSMSPlugin : CDVPlugin {
    NSMutableDictionary *pool;
}

-(void) sendVerificationCode: (CDVInvokedUrlCommand *) command;
-(void) commitVerificationCode: (CDVInvokedUrlCommand *) command;

@end
