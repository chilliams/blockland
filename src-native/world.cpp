#pragma once
#include "mesh.cpp"
#include "noise.hpp"
#include "shared.cpp"
#include <climits>
#include <cmath>
#include <emscripten/val.h>

namespace World {

const int CHUNK_HEIGHT = 32, CHUNK_WIDTH = 32;
const double N_SCALE = 0.15;
const double ISO_VAL = 0.34;
const double HN_SCALE = 0.028;
const double HEIGHT_SCALE = 0.09;
const int HEIGHT_SHIFT = 16;

class Block {
public:
  Material type = Material::empty;
};

class Chunk {
private:
  int id_x, id_z, xMin, xMax, zMin, zMax;
  Block ***blocks;

  // REFACTOR: name this better
  bool shouldRender(int x, int y, int z) {
    if (x < 0 || y < 0 || z < 0 || x >= CHUNK_WIDTH || y >= CHUNK_HEIGHT ||
        z >= CHUNK_WIDTH) {
      return false;
    }
    return blocks[x][y][z].type == Material::empty;
  }

public:
  Chunk(int idx, int idz, int x, int z) {
    id_x = idx;
    id_z = idz;
    xMin = x;
    xMax = x + CHUNK_WIDTH;
    zMin = z;
    zMax = z + CHUNK_WIDTH;
    blocks = new Block **[CHUNK_WIDTH];
    for (int i = 0; i < CHUNK_WIDTH; i++) {
      blocks[i] = new Block *[CHUNK_HEIGHT];
      for (int j = 0; j < CHUNK_HEIGHT; j++) {
        blocks[i][j] = new Block[CHUNK_WIDTH];
      }
    }
  }

  ~Chunk() {
    for (int i = 0; i < CHUNK_WIDTH; ++i) {
      for (int j = 0; j < CHUNK_HEIGHT; ++j) {
        delete[] blocks[i][j];
      }
      delete[] blocks[i];
    }
    delete[] blocks;
  }

  // returns true if the chunk changed, false if not
  bool setBlock(int x, int y, int z, Material type) {
    int chunkX = x - xMin;
    int chunkZ = z - zMin;
    bool xFits = chunkX >= 0 && chunkX < CHUNK_WIDTH;
    bool yFits = y >= 0 && y < CHUNK_HEIGHT;
    bool zFits = chunkZ >= 0 && chunkZ < CHUNK_WIDTH;
    bool change = xFits && yFits && zFits;

    if (change) {
      blocks[chunkX][y][chunkZ].type = type;
    }

    return change;
  }

  emscripten::val makeMesh() {
    int yMin = 0;
    int yMax = CHUNK_HEIGHT;
    int xWidth = xMax - xMin;
    int zWidth = zMax - zMin;

    std::vector<std::vector<int>> faces;
    std::vector<std::vector<int>> normals;
    std::vector<std::vector<float>> texture;
    emscripten::val collidable = JsArray.new_();
    int ci = 0;

    // Here we go
    for (int x = 1; x < CHUNK_WIDTH - 1; x++) {
      for (int y = 0; y < CHUNK_HEIGHT; y++) {
        for (int z = 1; z < CHUNK_WIDTH - 1; z++) {
          Material type = blocks[x][y][z].type;
          if (type == Material::empty) {
            continue;
          }

          bool viable = false;
          Vec3 pos;
          pos.x = x + xMin;
          pos.y = y;
          pos.z = z + zMin;

          if (shouldRender(x + 1, y, z)) {
            faces.push_back(Mesh::getFace(pos, Mesh::RIGHT));
            normals.push_back(Mesh::blockNormal[Mesh::RIGHT]);
            texture.push_back(Mesh::getTexture(type, Mesh::RIGHT));
            viable = true;
          }
          if (shouldRender(x - 1, y, z)) {
            faces.push_back(Mesh::getFace(pos, Mesh::LEFT));
            normals.push_back(Mesh::blockNormal[Mesh::LEFT]);
            texture.push_back(Mesh::getTexture(type, Mesh::LEFT));
            viable = true;
          }
          if (shouldRender(x, y + 1, z)) {
            faces.push_back(Mesh::getFace(pos, Mesh::TOP));
            normals.push_back(Mesh::blockNormal[Mesh::TOP]);
            texture.push_back(Mesh::getTexture(type, Mesh::TOP));
            viable = true;
          }
          if (shouldRender(x, y - 1, z)) {
            faces.push_back(Mesh::getFace(pos, Mesh::BOTTOM));
            normals.push_back(Mesh::blockNormal[Mesh::BOTTOM]);
            texture.push_back(Mesh::getTexture(type, Mesh::BOTTOM));
            viable = true;
          }
          if (shouldRender(x, y, z + 1)) {
            faces.push_back(Mesh::getFace(pos, Mesh::FRONT));
            normals.push_back(Mesh::blockNormal[Mesh::FRONT]);
            texture.push_back(Mesh::getTexture(type, Mesh::FRONT));
            viable = true;
          }
          if (shouldRender(x, y, z - 1)) {
            faces.push_back(Mesh::getFace(pos, Mesh::BACK));
            normals.push_back(Mesh::blockNormal[Mesh::BACK]);
            texture.push_back(Mesh::getTexture(type, Mesh::BACK));
            viable = true;
          }

          if (viable) {
            collidable.set(ci, pos);
            ci++;
          }
        }
      }
    }

    int resultCount = faces.size();
    emscripten::val result = JsObject.new_();

    emscripten::val positionData = Float32Array.new_(resultCount * 4 * 3);
    emscripten::val normalData = Float32Array.new_(resultCount * 4 * 3);
    emscripten::val uvData = Float32Array.new_(resultCount * 4 * 2);
    int p = 0;
    int n = 0;
    int u = 0;
    for (int i = 0; i < faces.size(); i++) {
      for (int x : faces[i]) {
        positionData.set(p, x);
        p++;
      }
      for (int j = 0; j < 4; j++) {
        for (int x : normals[i]) {
          normalData.set(n, x);
          n++;
        }
      }
      for (float f : texture[i]) {
        uvData.set(u, f);
        u++;
      }
    }

    emscripten::val indices = Uint16Array.new_(resultCount * 6);
    for (int i = 0; i < n; i++) {
      int m = i * 4;
      indices.set(i * 6, 0 + m);
      indices.set(i * 6 + 1, 1 + m);
      indices.set(i * 6 + 2, 2 + m);
      indices.set(i * 6 + 3, 0 + m);
      indices.set(i * 6 + 4, 2 + m);
      indices.set(i * 6 + 5, 3 + m);
    }

    result.set("count", resultCount);
    result.set("position", positionData);
    result.set("normal", normalData);
    result.set("uv", uvData);
    result.set("indices", indices);
    result.set("collidable", collidable);

    emscripten::val id = JsArray.new_();
    id.set(0, id_x);
    id.set(1, id_z);
    result.set("id", id);

    return result;
  }
};

Chunk *make_chunk(int x, int z) {
  int width = CHUNK_WIDTH - 2;
  int xMin = x * width - 1;
  int xMax = x * width + width + 1;
  int yMin = 0;
  int yMax = CHUNK_HEIGHT;
  int zMin = z * width - 1;
  int zMax = z * width + width + 1;

  Chunk *chunk = new Chunk(x, z, xMin, zMin);
  siv::PerlinNoise perlin;

  for (int x = xMin; x < xMax; x++) {
    for (int z = zMin; z < zMax; z++) {
      double maxHeight =
          std::min((perlin.noise(HN_SCALE * x, HN_SCALE * z) / HEIGHT_SCALE) +
                       HEIGHT_SHIFT,
                   (double)yMax);
      for (int y = yMin; y < maxHeight; y++) {
        if (ISO_VAL <= perlin.noise(x * N_SCALE, y * N_SCALE, z * N_SCALE) &&
            y != yMin) {
          continue;
        }
        Material type;
        if (1 > maxHeight - y) {
          type = Material::grass;
        } else if (y >= maxHeight - 3 && maxHeight != yMax) {
          type = Material::dirt;
        } else if (y == yMin) {
          type = Material::stone; // TODO: obsidian
        } else {
          type = Material::stone;
        }
        chunk->setBlock(x, y, z, type);
      }
    }
  }

  return chunk;
};
}
