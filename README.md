# GolemMultiplayer

## Purpose

This repository contains the shared codebase for the multiplayer Java3D game. All teammates run the same source code and choose runtime role (Host or Join) from the same application entry point.

## Why the .vscode folder exists

The .vscode folder is used to keep a predictable local development setup for everyone using VS Code, especially when the project depends on external Java3D and H2 libraries.

Without .vscode project settings, teammates can run into these common issues:

- Main class launches with the wrong JDK.
- Native Java3D library path is missing when launching from VS Code.
- Java extension cannot resolve referenced jars consistently across machines.

The .vscode setup is intended to reduce these environment mismatches.

## launch.json: why it is needed

launch.json defines how VS Code starts the game.

It should specify:

- mainClass so everyone launches the same entry point.
- vmArgs required by Java3D (for example java.library.path and any needed module export flags).
- a stable launch profile name so teammates can use the same run target.

In this project, launch.json is important because Java3D needs runtime arguments that are easy to forget when launching manually.

## settings.json: why it is needed

settings.json tells the VS Code Java extension how to resolve project dependencies.

For this repository, it should include library resolution from lib/\*_/_.jar so every teammate can run with the same local dependency folder.

This avoids machine-specific hardcoded jar paths and makes the workspace portable between:

- Windows
- Linux

## Team setup approach

Recommended team workflow:

1. Keep required jars in the repository lib folder (or in a shared setup process that places them there).
2. Keep .vscode config minimal and portable.
3. Avoid absolute local paths in committed workspace settings when possible.

This lets all teammates run the exact same code with fewer local configuration differences.

## Notes about gitignore and local IDE folders

This repository ignores local IDE/editor folders to keep version control clean:

- .idea/
- .vscode/
- out/production/

These files are often machine-specific and can create noise in pull requests.

If any of those files were already tracked before adding ignore rules, they must be removed from git index once (without deleting local files) using a cached remove operation.
