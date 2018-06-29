
// Empty constructor
function GSTracker() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
GSTracker.prototype.run = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'GSTracker', 'run', []);
}
GSTracker.prototype.exit = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'GSTracker', 'exit', []);
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