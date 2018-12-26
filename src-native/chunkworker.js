var q = [];
var initialized = false;

Module['onRuntimeInitialized'] = function() {
  initialized = true;
  while (q.length > 0) {
    var chunk = q.shift();
    makeChunk(chunk);
  }
};

var makeChunk = function(chunk) {
  var blocks = new Module.VectorBlock();
  for (var i = 0; i < chunk.length; i++) {
    blocks.push_back(chunk[i]);
  }
  var res = Module.chunkify(blocks);
  postMessage(res);
};

onmessage = function(msg) {
  var chunk = msg.data;
  if (initialized) {
    makeChunk(chunk);
  } else {
    q.push(chunk);
  }
};
