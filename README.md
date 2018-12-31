# Blockland

A Minecraft clone in the browser!

![screenshot](media/screenshot.png)

## Overview

Blockland is a browser-based voxel engine made with ClojureScript, WebGL, WebAssembly, and a Web Worker.

Blockland is a partial rewrite of [Voxelminer](https://github.com/chilliams/voxelminer), my last attempt at a voxel engine in the browser.

## Differences from Voxelminer

#### Blockland uses a physics engine.

In Voxelminer, I attempted to write all the physics from scratch. This turned out to be super tricky and very time-consuming for someone trying to build a game.

I picked Bullet because it allows for objects of arbitrary shapes (like chunks in a voxel engine) with good performance. Bullet is supported in the browser thanks to the wonderful [ammo.js](https://github.com/kripken/ammo.js) project.

If you want to really learn physics and learn to simulate it, write the physics yourself. If you want to make a game, I'd recommend picking a physics engine off the shelf.

I will admit that implementing the voxel ray tracing algorithm from [this paper](http://www.cse.yorku.ca/~amana/research/grid.pdf) was fun though.

#### Blockland uses Entity-Component-System.

Using the ECS pattern gives Blockland more structure and makes it easier to manage the code. I'm not sure if what I ended up with is strictly ECS as normally implemented, but it worked out pretty well.

The architecture I implemented is pretty similar to how [play-clj](https://github.com/oakes/play-clj) does things.

#### Blockland uses [three.js](https://github.com/mrdoob/three.js).

three.js has great documentation.

## Development

You will need both [Emscripten](https://github.com/kripken/emscripten) and NPM.

To get an interactive development environment run:

    sh compile.sh
    npx shadow-cljs watch game

and open your browser at [localhost:8080](http://localhost:8080/).

## TODO

- Production build
- Host on GitHub Pages
- Fix a bug where the player can place blocks they end up inside of
- Block selection

## Credits

Textures are from [https://github.com/deathcap/ProgrammerArt](https://github.com/deathcap/ProgrammerArt)

## License

Copyright © 2018 Christopher Williams

Distributed under the Eclipse Public License version 1.0
