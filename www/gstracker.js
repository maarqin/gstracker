
// Empty constructor
function GSTracker() {}

// The function that passes work along to native shells
GSTracker.prototype.run = function(userId, userEmail, userDeviceId, successCallback, errorCallback) {
  var options = {};
  options.userId = userId;
  options.userEmail = userEmail;
  options.userDeviceId = userDeviceId;

  cordova.exec(successCallback, errorCallback, 'GSTracker', 'run', [options]);
}
GSTracker.prototype.exit = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'GSTracker', 'exit', []);
}
GSTracker.prototype.statusConnection = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'GSTracker', 'statusConnection', []);
}

// Installation constructor that binds GSTracker to window
GSTracker.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.gsTracker = new GSTracker();
  return window.plugins.gsTracker;
};
cordova.addConstructor(GSTracker.install);