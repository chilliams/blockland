#!/bin/sh

mkdir -p public/js/

emcc -O3 \
     -s ALLOW_MEMORY_GROWTH=1 \
     --memory-init-file 0 \
     --bind \
     --post-js src-native/chunkworker.js \
     -o public/js/chunkworker.js \
     src-native/chunks.cpp
