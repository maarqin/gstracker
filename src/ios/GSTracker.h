/* GSTracker.h */

#import <Cordova/CDV.h>
#import "MainViewController.h"

@interface GSTracker : CDVPlugin

- (void) echo: (CDVInvokedUrlCommand*) command;
- (void) statusConnection: (CDVInvokedUrlCommand*) command;
- (void) run: (CDVInvokedUrlCommand*) command;
- (void) exit: (CDVInvokedUrlCommand*) command;

#define AVAILABLE @"AVAILABLE"
#define UNAVAILABLE @"UNAVAILABLE"
#define NOTHING @"NOTHING"

#define IS_CONNECTION_OK @"IS_CONNECTION_OK"
#define USER_ID @"USER_ID"
#define USER_EMAIL @"USER_EMAIL"
#define USER_DEVICE_ID @"USER_DEVICE_ID"

@end
