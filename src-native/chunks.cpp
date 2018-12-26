#include "shared.cpp"
#include "world.cpp"
#include <emscripten/bind.h>
#include <emscripten/val.h>
#include <map>
#include <utility>

std::map<std::pair<int, int>, World::Chunk *> chunks;

emscripten::val make_chunk(Vec3 position) {
  std::pair<int, int> xz(position.x, position.z);

  if (chunks.count(xz) == 0) {
    World::Chunk *chunk = World::make_chunk(position.x, position.z);
    chunks.emplace(xz, chunk);
  }

  return chunks.at(xz)->makeMesh();
}

emscripten::val change_block(Vec3 position, Material type) {
  emscripten::val result = JsArray.new_();

  int i = 0;
  for (auto const &x : chunks) {
    World::Chunk *chunk = x.second;
    bool updated = chunk->setBlock(position.x, position.y, position.z, type);

    if (updated) {
      result.set(i, chunk->makeMesh());
      i += 1;
    }
  }

  return result;
}

emscripten::val add_block(Vec3 position, Material type) {
  return change_block(position, type);
}

emscripten::val remove_block(Vec3 position) {
  return change_block(position, Material::empty);
}

EMSCRIPTEN_BINDINGS(my_module) {
  emscripten::value_array<Vec3>("Vec3")
      .element(&Vec3::x)
      .element(&Vec3::y)
      .element(&Vec3::z);

  emscripten::enum_<Material>("Material")
    .value("grass", Material::grass)
    .value("stone", Material::stone)
    .value("dirt", Material::dirt);

  emscripten::function("make_chunk", &make_chunk);
  emscripten::function("add_block", &add_block);
  emscripten::function("remove_block", &remove_block);
}
