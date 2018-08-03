/* GSTracker.m */

#import "GSTracker.h"
#import <Cordova/CDV.h>

@implementation GSTracker

- (void)echo:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* action = [command.arguments objectAtIndex:0];

    NSArray *items = @[@"run", @"exit", @"statusConnection"];

    int item = [items indexOfObject:action];
    switch (item) {
        case 0: // run
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            break;
        case 1: // exit
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            break;
        case 2: // statusConnection
            default :
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            break;
    }

    if (msg == nil || [msg length] == 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    } else {
        // /* http://stackoverflow.com/questions/18680891/displaying-a-message-in-ios-which-has-the-same-functionality-as-toast-in-android */
        // UIAlertView *toast = [
        //     [UIAlertView alloc] initWithTitle:@""
        //     message:msg
        //     delegate:nil
        //     cancelButtonTitle:nil
        //     otherButtonTitles:nil, nil];

        // [toast show];
        
        // dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 3 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        //     [toast dismissWithClickedButtonIndex:0 animated:YES];
        // });
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
