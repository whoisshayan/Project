# Save/Load System Documentation

## Overview
The game now includes a comprehensive save/load system with automatic crash recovery.

## Features

### 1. **Automatic Saving**
- Game state is saved automatically when switching between levels
- Saves are stored in the `saves/default/` directory
- Each level has its own save file (e.g., `level-1.json`, `level-2.json`)

### 2. **Crash Recovery (Autosave)**
- Game state is automatically saved every 3 seconds during gameplay
- Autosaves are stored in `saves/tmp/default/` directory
- If the game crashes, you'll be prompted to resume from the last autosave when you restart

### 3. **What is Saved**
- Game state (paused, game over, started, current time, coins)
- Wire budget (remaining wire length)
- Score (packet loss)
- All node positions (circles, rectangles)
- All connections between nodes
- Wire views with bend points

### 4. **Save File Format**
Save files are stored in JSON format with the following structure:
- `schemaVersion`: Save format version
- `profileId`: Player profile (currently "default")
- `levelId`: Level identifier
- `savedAtEpochMillis`: Timestamp when saved
- `levelData`: All level-specific data

## Usage

### Starting the Game
1. Run the game normally
2. If a previous save exists for a level, it will be loaded automatically
3. If a crash autosave exists, you'll see a prompt asking if you want to resume

### Manual Save Management
- Saves are created automatically when you exit a level
- To delete saves, remove files from the `saves/` directory

## Directory Structure
```
saves/
├── default/                 # Normal saves
│   ├── level-1.json
│   ├── level-1.json.bak    # Backup of previous save
│   ├── level-2.json
│   └── ...
└── tmp/                    # Crash autosaves
    └── default/
        ├── level-1.json
        └── ...
```

## Building the Project

### Using Maven
```bash
mvn clean compile
mvn javafx:run
```

### Using the existing scripts
The original `run.cmd` and `run.ps1` scripts should still work, but you may need to add the Gson library to the classpath.

## Dependencies
- JavaFX 17.0.2
- Gson 2.10.1 (for JSON serialization)
