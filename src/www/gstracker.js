
// Empty constructor
function GSTracker() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
GSTracker.prototype.run = function(message, duration, successCallback, errorCallback) {
  var options = {};
  options.message = message;
  options.duration = duration;
  cordova.exec(successCallback, errorCallback, 'GSTracker', 'run', [options]);
}

// Installation constructor that binds GSTracker to window
GSTracker.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.gsTracker = new GSTracker();
  return window.plugins.toastyPlugin;
};
cordova.addConstructor(GSTracker.install);