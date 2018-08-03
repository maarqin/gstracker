function replaceAppDelegate(acc, line) {
  return `
  //gstrack modified
  /*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  */
 
 #import "AppDelegate.h"
 #import "MainViewController.h"
 #import "AppDelegate.h"
 #import "Plugins/cordova-plugin-gstracker/GLManager.h"
 
 @implementation AppDelegate
 
 - (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
 {
     self.viewController = [[MainViewController alloc] init];
     [GLManager sharedManager];
    
     if([launchOptions objectForKey:UIApplicationLaunchOptionsLocationKey]) {
         [[GLManager sharedManager] logAction:@"application_launched_with_location"];
     }

     return [super application:application didFinishLaunchingWithOptions:launchOptions];
 }


- (void)applicationDidEnterBackground:(UIApplication *)application {
  NSLog(@"Application is entering the background");
  [[GLManager sharedManager] applicationDidEnterBackground];
}

- (void)applicationWillTerminate:(UIApplication *)application {
  NSLog(@"Application is terminating");
  [[GLManager sharedManager] applicationWillTerminate];
}

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
  if([[url host] isEqualToString:@"setup"]) {
      NSString *endpoint = [[url query] stringByRemovingPercentEncoding];
      NSLog(@"Saving new API Endpoint: %@", endpoint);
      [[NSUserDefaults standardUserDefaults] setObject:endpoint forKey:GLAPIEndpointDefaultsName];
      [[NSUserDefaults standardUserDefaults] synchronize];
  }
  
  return YES;
}
 
 @end`;
}


module.exports = ctx => {
  if (ctx.opts.platforms.indexOf('ios') < 0)
    return;
	const fs = ctx.requireCordovaModule('fs');
	const path = ctx.requireCordovaModule('path');
	const deferral = ctx.requireCordovaModule('q').defer();
  const platformRoot = path.join(ctx.opts.projectRoot, 'platforms/ios');
  
  const mainActivityPath = path.join(platformRoot, 'Golfleet Driver/Classes/AppDelegate.m');
  fs.readFile(mainActivityPath, 'utf-8', (err, data) => {
    if (err)
      deferral.resolve();
    else if (data.includes('//gstrack modified'))
      deferral.resolve();
    else {
      const fileLines = data.split('\n');
      const editedFile = fileLines.reduce((acc, line, index, arr) => {
        return replaceAppDelegate(acc, line);
      }, '');
      console.log(editedFile);
      fs.writeFile(mainActivityPath, editedFile, err => {
        if(err)
          deferral.resolve();
        deferral.resolve();
      });
    }
  });

  return deferral.promise;
};