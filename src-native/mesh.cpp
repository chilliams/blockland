#pragma once
#include <vector>
#include "shared.cpp"

namespace Mesh {

enum Side : int {
  RIGHT = 0,
  LEFT = 1,
  TOP = 2,
  BOTTOM = 3,
  FRONT = 4,
  BACK = 5
};

std::vector<int> getFace(Vec3 p, Side side) {
  // refactor?

  // [[(1 0 1) (1 0 0) (1 1 0) (1 1 1)]
  //  [(0 0 0) (0 0 1) (0 1 1) (0 1 0)]
  //  [(0 1 1) (1 1 1) (1 1 0) (0 1 0)]
  //  [(0 0 0) (1 0 0) (1 0 1) (0 0 1)]
  //  [(0 0 1) (1 0 1) (1 1 1) (0 1 1)]
  //  [(1 0 0) (0 0 0) (0 1 0) (1 1 0)]]

  switch (side) {
  case RIGHT:
    return {1 + p.x, 0 + p.y, 1 + p.z, 1 + p.x, 0 + p.y, 0 + p.z,
            1 + p.x, 1 + p.y, 0 + p.z, 1 + p.x, 1 + p.y, 1 + p.z};
  case LEFT:
    return {0 + p.x, 0 + p.y, 0 + p.z, 0 + p.x, 0 + p.y, 1 + p.z,
            0 + p.x, 1 + p.y, 1 + p.z, 0 + p.x, 1 + p.y, 0 + p.z};
  case TOP:
    return {0 + p.x, 1 + p.y, 1 + p.z, 1 + p.x, 1 + p.y, 1 + p.z,
            1 + p.x, 1 + p.y, 0 + p.z, 0 + p.x, 1 + p.y, 0 + p.z};
  case BOTTOM:
    return {0 + p.x, 0 + p.y, 0 + p.z, 1 + p.x, 0 + p.y, 0 + p.z,
            1 + p.x, 0 + p.y, 1 + p.z, 0 + p.x, 0 + p.y, 1 + p.z};
  case FRONT:
    return {0 + p.x, 0 + p.y, 1 + p.z, 1 + p.x, 0 + p.y, 1 + p.z,
            1 + p.x, 1 + p.y, 1 + p.z, 0 + p.x, 1 + p.y, 1 + p.z};
  default:
    return {1 + p.x, 0 + p.y, 0 + p.z, 0 + p.x, 0 + p.y, 0 + p.z,
            0 + p.x, 1 + p.y, 0 + p.z, 1 + p.x, 1 + p.y, 0 + p.z};
  }
}

std::vector<std::vector<int>> blockNormal = {
    {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

std::vector<float> grassTop = {0.124, 0.124, 0.01,  0.124,
                               0.01,  0.01,  0.124, 0.01};

std::vector<float> grassSide = {0.499, 0.124, 0.376, 0.124,
                                0.376, 0.01,  0.499, 0.01};

std::vector<float> dirtSide = {0.374, 0.124, 0.251, 0.124,
                               0.251, 0.01,  0.374, 0.01};

std::vector<float> stoneSide = {0.249, 0.124, 0.126, 0.124,
                                0.126, 0.01,  0.249, 0.01};

std::vector<std::vector<float>> grassTex = {grassSide, grassSide, grassTop,
                                            dirtSide,  grassSide, grassSide};

std::vector<std::vector<float>> stoneTex = {stoneSide, stoneSide, stoneSide,
                                            stoneSide, stoneSide, stoneSide};

std::vector<std::vector<float>> dirtTex = {dirtSide, dirtSide, dirtSide,
                                           dirtSide, dirtSide, dirtSide};

std::vector<float> getTexture(Material type, Side side) {
  if (type == Material::grass) {
    return grassTex[side];
  }
  if (type == Material::dirt) {
    return dirtTex[side];
  }
  if (type == Material::stone) {
    return stoneTex[side];
  }
  return grassTex[side];
}
}
