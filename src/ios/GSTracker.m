/* GSTracker.m */

#import "GSTracker.h"
#import <Cordova/CDV.h>
#import "GLManager.h"
#import "MainViewController.h"

@implementation GSTracker

-(void) statusConnection: (CDVInvokedUrlCommand*) command {
    CDVPluginResult* pluginResult = nil;
    
    NSString *status = [[NSUserDefaults standardUserDefaults] valueForKey:IS_CONNECTION_OK];
    
    if( status != nil ){
        if( [status isEqualToString:@"NOTHING"] || [status isEqualToString:@"AVAILABLE"] ) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"OK"];
        } else if( [status isEqualToString:@"UNAVAILABLE"] ) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ILLEGAL_ACCESS_EXCEPTION messageAsString:@"Illegal access"];
            
            // [[GLManager sharedManager] clearTripDB];
            
            //            NSUserDefaults * defs = [NSUserDefaults standardUserDefaults];
            //            NSDictionary * dict = [defs dictionaryRepresentation];
            //            for (id key in dict) {
            //                [defs removeObjectForKey:key];
            //            }
            //            [defs synchronize];
        }
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void) run: (CDVInvokedUrlCommand*) command {
    [[NSUserDefaults standardUserDefaults] setBool:[GLManager sharedManager].trackingEnabled forKey:GLTrackingStateDefaultsName];
    
    [[GLManager sharedManager] saveNewAPIEndpoint:@"http://driver-ws.golsat.com.br:6001/driver-positioning"];
    
    // Custom configuration
    [GLManager sharedManager].significantLocationMode = kGLSignificantLocationEnabled;
    [GLManager sharedManager].activityType = CLActivityTypeAutomotiveNavigation;
    [GLManager sharedManager].desiredAccuracy = kCLLocationAccuracyBest;
    [GLManager sharedManager].pointsPerBatch = 200; // pacote com 200 pontos
    [GLManager sharedManager].pausesAutomatically = NO;
    [[GLManager sharedManager] setSendingInterval:[NSNumber numberWithInteger:60]]; // enviar a cada 1 minuto
    [[GLManager sharedManager] setDefersLocationUpdates:100];
    
    [[GLManager sharedManager] startAllUpdates];
    [[GLManager sharedManager] startTrip];
    
    MainViewController *mainView = (MainViewController *)[self viewController];
    
    NSDictionary *dic = [[command arguments] objectAtIndex:0];
    
    [mainView run:dic];
    
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

-(void) exit:(CDVInvokedUrlCommand*) command {
    if( [[GLManager sharedManager] trackingEnabled] ) {
        [[GLManager sharedManager] stopAllUpdates];
        [[GLManager sharedManager] endTrip];
    }
    
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)echo:(CDVInvokedUrlCommand*) command
{
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

@end
