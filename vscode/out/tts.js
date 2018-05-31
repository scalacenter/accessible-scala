const spawn = require('child_process').spawn;

var processes = new Map();

function cancel(){
  processes.forEach(function (v) {
    v.kill()
  });
};

exports.cancel = cancel;

exports.speak = function(utterance){
  cancel();
  var child = spawn("espeak", [utterance], {detached: true});
  child.on('exit', function (code, signal) { 
    processes.delete(child.pid); 
  });
  processes.set(child.pid, child);
};