#pragma once
#include <emscripten/val.h>

emscripten::val JsObject = emscripten::val::global("Object");
emscripten::val JsArray = emscripten::val::global("Array");
emscripten::val Float32Array = emscripten::val::global("Float32Array");
emscripten::val Uint16Array = emscripten::val::global("Uint16Array");

enum Material { empty, grass, dirt, stone };

struct Vec3 {
  int x, y, z;
};
