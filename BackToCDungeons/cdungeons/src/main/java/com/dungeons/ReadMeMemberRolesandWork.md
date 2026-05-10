Marin Mjeshtri ( Mr Balls)
com.dungeons.screens — Screen Loaders
com.dungeons.Controllers — UI Controllers
com.dungeons — Entry Point & Packaging
com.dungeons.systems.items – item loaders
/resources/screens – Stored FXML screens
/resources/sprites/characters – inGame characters
/resources/sprites/DialougeSprites – Dialouge specific sprites
/resources/sprites/items – Item Sprites
/resources/sprites/lmfao – legacy joke Sprites
/resources/sprites/uiDecor & style.css – FXML styling
/resources/tiles – used on Tiled by KlediAllamani, our chosen tileset by https://itch.io/profile/marceles
/resources/OpenType-TT – Font Storage
/resources/items – Temporary item additions

________________________________________
Ui Screens
The scaffolding is built using SceneBuilder for JavaFX, the rest of the code is manually edited through IntelliJ
areYouSureScreen.fxml — Confirmation overlay displayed when the player attempts to exit the game. Contains a prompt and two buttons — YES to confirm exit and NO to dismiss the overlay and return to the previous screen.
attackScreen.fxml — Sub-menu panel displayed when the player selects ATTACK during combat. Shows up to four ability buttons wired dynamically to the player's move list, and a GO BACK button to return to the main action menu.
battleScreen.fxml — The main combat UI screen. Contains the boss and player sprite areas, HP bars for both combatants with a red background bar and a green overlay bar, a turn log text area, a turn number label, and the main action menu with ATTACK, GUARD, ITEMS and TALK buttons. Each action opens its corresponding sub-menu panel.
creditsScreen.fxml — Displays the credits for the game. Accessible from the starting screen via the CREDITS button.
DialogueScreen.fxml — The NPC dialogue overlay displayed during story encounters and character interactions. Shows the speaker's dialogue sprite alongside a text box that advances line by line on Enter, Space or mouse click.
gameoverScreen.fxml — Displayed when the player is defeated in combat. Shows a game over message and options to retry or return to the main menu.
itempickupScreen.fxml — Chest interaction overlay shown when the player steps on a chest tile. Displays the item's name, image, stats and description in a styled panel over the game world.
itemScreen.fxml — Item management screen for viewing and managing the player's current inventory and held items.
pauseScreen.fxml — Pause overlay displayed when the player presses ESC during gameplay. Contains RESUME, OPTIONS and EXIT buttons. Rendered as a full 800x600 semi-transparent panel layered on top of the game canvas so the game world remains visible behind it.
shopScreen.fxml — Shop overlay displayed when the player steps on a shop tile. Shows three item cards each with a name, image and price label, and a PURCHASE button at the bottom. Styled with the dark navy pixel art theme matching the rest of the game UI.
startingScreen.fxml — The main menu screen shown on launch. Displays the game title and three buttons — PLAY to start the game, CREDITS to view the credits screen, and EXIT to close the application. Background image loaded dynamically via the controller.
________________________________________
Screen Loaders
THE LOADERS FOLLOW THE SAME LOGIC!
startingScreen — Loads startingScreen.fxml and exposes the root and loader for controller access.
pauseScreen — Loads pauseScreen.fxml, injects GameScreen and Stage references into PauseController after loading.
combatScreen — Loads battleScreen.fxml and exposes the loader for CombatController access.
shopScreen — Loads shopScreen.fxml, injects GameScreen and Stage into shopController.
itemPickupScreen — Loads itempickupScreen.fxml, injects GameScreen and Stage into chestController.
DialoguesScreen — Loads DialogueScreen.fxml and exposes the loader for DialogueBoxController access.
creditsScreen — Loads creditsScreen.fxml and exposes the root.
areYouSureScreen – Loads confirmation page for exit
gameoverScreen – Loads after death in combat, allows you to exit the game or start over.
________________________________________
Controllers
OptionsNStartingController — Handles the starting screen buttons. PLAY switches the scene to GameScreen, CREDITS opens the credits screen, EXIT closes the application. Holds a Stage reference for scene switching.
PauseController — Handles the pause overlay. RESUME calls GameScreen.togglePause(), EXIT calls System.exit(0), OPTIONS opens the are-you-sure overlay. Holds references to GameScreen and Stage.
shopController — Handles the shop UI. Manages item display, selection highlighting on click, and the PURCHASE button. Holds a Stage reference for closing the overlay.
chestController — Handles the chest/item pickup UI. Displays item name, image, stats and description. Holds a Stage reference for closing the overlay.
QuitController — Handles the exit confirmation overlay. YES exits the application, NO removes the overlay from the parent pane.
________________________________________
GameScreen — UI and Screen Wiring Contributions
Fields:
shopScreen — Reference to the active shop overlay
itemPickupScreen — Reference to the active chest overlay
pauseScreen — Reference to the pause overlay
gameRoot — Stored root pane used to restore the game scene after combat
interactionLocked — Prevents interact events firing during screen transitions
Methods:
getRoot() — Initialises the pause overlay, shop overlay, chest overlay and dialogue overlay. Wires keyboard input including ESC for pause. Stores gameRoot for later restoration.
returnFromCombat() — Marks the fight tile as done, clears player input, restores gameRoot as the scene root and restarts the game loop.
returnFromCombatWithMap(name) — Same as above but loads a new map first.
togglePause() — Shows or hides the pause overlay and stops or starts the game loop accordingly.
________________________________________
Visual Assets & Styling
-	Sourced and integrated all tilesets used across the game's maps.
-	Found and loaded the custom Reano pixel art typeface globally via Font.loadFont() in the main entry point.
-	Collected and organised all dialogue sprites used in NPC interaction screens, with multiple different versions for each character
-	Created multiple other sprites, including Item Sprites, character sprites, UI decoration sprites, etc.
-	Wrote the global CSS stylesheet applied across all scenes defining the dark navy colour palette, glowing border effects, button hover states, and consistent font application via the * selector.
________________________________________
Entry Point & Packaging
marinMainTesting — Main entry point used throughout development. Loads the starting screen, passes the Stage to OptionsNStartingController, and applies the global CSS stylesheet to the opening scene.
Packaged the final game into a distributable Windows application using jpackage --type app-image, producing a self-contained executable folder that bundles the JavaFX runtime and all game resources, requiring no Java installation on the target machine.
________________________________________
com.dungeons.systems.items
itemData — Data class representing a single item. Stores the item's name, description, stats/buffs and sprite filename. Loaded from the items JSON file and passed to the chest and shop UI screens for display.
itemPicker — Handles item selection logic. Picks which item is offered to the player when they open a chest or enter a shop. Selects from the available item pool and returns an itemData object to be displayed in the UI.
Items — Loads and stores all item definitions from /resources/items/. Acts as the item registry for the game — other systems request items from here rather than loading them directly.
 

 

Kledi Allamani (Freaki Relah)
com.dungeons.world — Map System
com.dungeons.systems — Player
com.dungeons.Controllers — Dialogue Controller
com.dungeons.screens — GameScreen (Map & Game Loop)
/resources/maps – Stores the maps created on tiled
/resources/tiles – Stores extra specific sprites created personally
________________________________________
com.dungeons.world
InteractZone — Represents a single interactive tile on the map. Stores the tile coordinates (x, y), the interaction type (e.g. "fight", "shop", "dialogue:cassie_encounter"), and a triggered flag to prevent reactivation.
TransitionZone — Represents a tile that moves the player to another map. Stores the tile coordinates (x, y), the target map name, and optionally the spawn coordinates on the target map. spawnX/spawnY of -1 means the target map's default spawnpoint is used.
________________________________________
Map — Loads and stores all data for a map from a JSON file exported from Tiled.
Fields:
width, height — Map dimensions in tiles
layers — Ordered map of layer name → GID array
collisionLayers — Subset of layers used for collision checking
tilesetRanges — Maps firstgid → tileset key for GID resolution
transitions — All TransitionZone objects loaded from object layers
interactZones — All InteractZone objects loaded from object layers
spawnX, spawnY — Default player spawn coordinates in tiles
currentMapName — Name of the currently loaded map
Methods:
load(mapName) — Loads the map JSON from /maps/{mapName}.json
isSolid(x, y) — Returns true if the tile is out of bounds or has a non-zero value in any collision layer
resolveTile(rawGid) — Strips flip flags from a GID and returns [tilesetKey, localIndex]
clearLayer(layerName) — Sets all tiles in a layer to 0
Object layer names recognised:
"Spawnpoint" — Sets the map's spawn coordinates
"Transition" — Transitions to the next map in MAP_TRANSITION_CHAIN
"TransitionShopRoom" — Transitions to ShopRoom
"TransitionChestRoom" — Transitions to ChestRoom
"TransitionBackFromShop" — Returns to the previous map at shop coordinates
"TransitionBackFromChest" — Returns to the previous map at chest coordinates
"Fight" — Creates fight interact zones
"Shop" — Creates shop interact zones
"Chest" — Creates chest interact zones
"cassie_encounter" — Creates dialogue:cassie_encounter zones
"freki_encounter" — Creates dialogue:freki_encounter zones
"merchant_enter" — Creates dialogue:merchant_enter zones
"johnmkati_lab_reveal" — Creates dialogue:johnmkati_lab_reveal zones
________________________________________
TilesetManager — Loads tileset images and splits them into individual 16x16 tile images.
Methods:
loadAll() — Loads all tilesets used by the game
get(tileset, localId) — Returns the image for a given tileset and local index, or null if out of bounds
________________________________________
MapRenderer — Draws all tile layers of the current map onto a JavaFX GraphicsContext. Layers are drawn in the order they appear in the map (LinkedHashMap preserves insertion order from Tiled). Each tile is drawn at size TILE_SIZE * SCALE (16 * 2 = 32px per tile).
Methods:
render(gc) — Draws all layers
drawLayer(gc, layer) — Draws a single layer, resolving each GID to an image via Map.resolveTile() and TilesetManager.get()
________________________________________
MapManager — Coordinates map loading, transitions and interact zone activation. Called every frame from GameScreen with the player's current tile position.
Fields:
currentMap — The currently active Map object
transitionCooldown — Prevents interact zones from firing on the first frame after a transition
Methods:
loadMap(mapName) — Creates and loads a new map
getCurrentMap() — Returns the active map
checkInteractions(x, y) — Checks transitions then interact zones
markFightDone(x, y) — Marks all fight zones at the given tile as triggered
markDialogueDone(x, y) — Marks all dialogue zones at the given tile as triggered
Interfaces:
MapChangeListener.onMapChanged(newMap, spawnX, spawnY) — Called when the player steps on a transition zone
InteractListener.onInteract(type, tileX, tileY) — Called when the player steps on an interact zone
Special behaviour:
If a fight zone and a dialogue zone share the same tile, the dialogue activates first. The fight activates on the player's next step onto that tile.
________________________________________
com.dungeons.systems
Player — Manages player movement, collision checking and rendering. Position is stored in scaled pixel coordinates (tile * 16 * 2).
Constants:
SPEED — 2.0 pixels per frame
TILE_SIZE — 16 (unscaled tile size)
SCALE — 2 (must match MapRenderer and GameScreen)
SIZE — 14 (hitbox size in unscaled pixels)
Methods:
setMap(map) — Sets the map used for collision checking
setPosition(x, y) — Teleports the player to scaled pixel coordinates
keyPressed(key) — Activates movement for WASD or arrow keys
keyReleased(key) — Deactivates movement
clearInput() — Clears all movement inputs (used after combat)
update() — Applies movement and collision every frame
render(gc) — Draws the player at the current position
getTileX(), getTileY() — Returns tile coordinates based on center position for interaction checks
getX(), getY() — Returns raw pixel position
________________________________________
com.dungeons.Controllers
DialogueBoxController — FXML controller for the dialogue box UI. Line advancement is triggered by Enter, Space, or left mouse click.
Methods:
setDialogueManager(dm) — Sets the DialogueManager
setOnFinished(callback) — Sets the function called when the dialogue ends
startDialogue(id) — Loads a dialogue by ID and displays the first line
nextLine() — Advances to the next line or calls onFinished
________________________________________
com.dungeons.screens
GameScreen — Manages the game loop, map rendering, player, camera and interactions.
Map-related fields:
tilesetManager — Loads and stores all tile images
mapManager — Manages transitions and interact zones
mapRenderer — Renders the current map every frame
interactionLocked — Prevents interactions during combat/dialogue
fightTileX/Y — Tile coordinates of the last fight zone triggered
lastDialogueTileX/Y — Tile coordinates of the last dialogue zone triggered
Map-related methods:
getRoot() — Initialises the map system, loads MobRoom1, sets spawn position
returnFromCombat() — Called when a fight ends. Marks the fight as done, clears the mob layer and resumes the game
returnFromCombatWithMap(name) — Same as above but loads a new map first
update() — Calls checkInteractions every frame when not locked
updateCamera() — Clamps the camera within the map and keeps it centred on the player

 
 

Sindi Leka (Thin De. Moni)
com.dungeons.MusicAndSoundsCode — Audio System
/resources/MusicForTheGame — Music
/resources/Soungs – Sound Effects
/resources/Dialouges – Game story script and character dialogue
/resources/sprites/characters – inGame enemy sprites
________________________________________
Audio System Overview
AudioManager.java — Low-level audio engine. Handles all technical audio operations — loading files, controlling playback, and managing volume. Uses JavaFX MediaPlayer for MP3 music tracks and javax.sound.sampled for WAV/OGG sound effects. Never called directly by game code — all calls go through GameMusicManager.
GameMusicManager.java — High-level audio controller called by the rest of the team. Knows what music should play in each game state. Provides simple named methods that teammates call at specific moments. Tracks what is currently playing so the same track never restarts unnecessarily.
________________________________________
Files Changed
AudioManager.java — Fully rewritten. Uncommented and rebuilt with JavaFX MediaPlayer for MP3 support.
GameMusicManager.java — Fully rewritten. Uncommented and connected to all screens and controllers.
pom.xml — Added javafx-media dependency (version 13), required to play MP3 files.
module-info.java — Added requires javafx.media, required by the Java module system.
module-info-music.java — Deleted. Duplicate file causing compilation error.
pom-music.xml — Deleted. Duplicate file not needed.
________________________________________
Music Flow
Music changes automatically based on game state. The system tracks what is currently playing so the same track never restarts unnecessarily.
Walkingthroughthegame.mp3 — Plays on the title screen and during dungeon exploration. Loops continuously.
FightingtheBosses.mp3 — Plays during regular boss fights. Loops continuously.
Fightingthefinalboss.mp3 — Plays during the final boss fight against SuperCoolSigma. Loops continuously.
Emotionalmomentattheend.mp3 — Plays on the credits and ending screen. Plays once.
Music stops — When the player is defeated or the game over screen is shown.
________________________________________
Sound Effects
Sound effects fire at specific moments only — they do not play continuously.
WalkingSound.wav — Every 20 frames while the player is moving.
HittingSound.wav — Every time damage is dealt to any character.
SwordFight.wav — Default attack sound for physical moves.
LightningStrikeSound.wav — When a lightning-type move is used.
MagicSpellSound.wav — When a spell or magic move is used.
CloneSound.wav — When a clone move is used.
SpawnWallSound.wav — When a wall move is used.
SpawnTurret.wav — When a turret move is used.
PickupItemSound.ogg — When an item is used in combat or a chest is opened.
GameOverSound.wav — When the player is defeated.
LevelUp.ogg — When the player levels up.
________________________________________
Team Integration
Teammates only call GameMusicManager — never AudioManager directly. Each integration requires one line of code at the right moment.
Main.java — GameMusicManager.playOpening() before stage.show()
OptionsNStartingController.java — GameMusicManager.playGameplay() in handleButton1()
CombatController.java — GameMusicManager.playCombat() or playFinalBoss() in initialize()
CombatController.java — GameMusicManager.playGameplay() on PLAYER_WIN
CombatController.java — GameMusicManager.stopMusic() + playGameOverSound() on PLAYER_LOSE
creditsScreen.java — GameMusicManager.playEnding() in constructor
PauseController.java — GameMusicManager.pauseMusic() / resumeMusic()
systems/Player.java — GameMusicManager.tickWalkSound(moving) in update()
CombatEngine.java — GameMusicManager.playHitSound() after takeDamage()
CombatController.java — GameMusicManager.playMoveSound(move.getName()) after processTurn()
________________________________________
Architecture
The system follows a two-layer architecture separating technical concerns from game logic.
Layer 1 — AudioManager.java — Handles file loading, playback control and volume management. Uses JavaFX MediaPlayer for MP3 and javax.sound.sampled for WAV/OGG. Never called directly by game code.
Layer 2 — GameMusicManager.java — Handles game state logic. Knows what music belongs in each state and provides named methods that teammates call. Prevents the same track from restarting unnecessarily.
________________________________________
Branch & Git History
All changes developed on the Sindi branch. Pushed and verified working before merging. Merged into main via Pull Request on GitHub with no conflicts. Both AudioManager.java and GameMusicManager.java are live on the main branch.

 

Jon Toska (John M. Kati)
com.dungeons.dialogueManager — Dialogue System
com.dungeons.Controllers — DialogueBoxController
com.dungeons.screens — GameScreen (Dialogue Integration)
com.dungeons.characters – Alternative character stat loader
com.dungeons.dialougeManager
/resources/Dialogues — Dialogue Data
/resources/CharacterStats – Enemy & Player  Json data management
/resources/Dialouges – Dialouge Json management
________________________________________
Data Files
dialogue.json — A JSON object with a single dialogues key containing all dialogue entries. Each entry is identified by a dialogue ID (e.g. "merchant_enter", "cassie_defeat") and splits into three keys:
character — String with the character's name
sprite — String with the sprite filename
lines — Array of strings where each element is one line of dialogue in order
characters.json — A JSON object with a single characters key containing character entries identified by name (e.g. "Player"). Each entry splits into:
stats — Object with atk, def, hp as integers
sprites — Object with neutral, angry, defeated as filename strings
abilities — Array of objects with names, effects and damage values. Further changes were made later to ease integration with the combat system.
________________________________________
com.dungeons.dialogueManager
Dialogue.java — A simple data class representing the stored data for a single dialogue entry.
Fields:
character — Name of the speaking character
sprite — Sprite filename for the character
lines — Ordered array of dialogue lines
________________________________________
DialogueData.java — Required for Gson parsing. Stores the data parsed from JSON into Java objects via Gson. Serves as a wrapper class that Gson fills when reading the JSON file. DialogueManager extracts data.dialogues and stores it in the dialogues map.
________________________________________
DialogueManager.java — Loads and manages all dialogue data from the JSON file. Maintains the state of the active dialogue during an ongoing conversation.
Fields:
dialogues — Map<String, Dialogue> storing all dialogues as key-value pairs where the String is the dialogue ID and Dialogue is the full dialogue object. Uses a HashMap for O(1) lookup time complexity.
currentLines — Array of lines for the currently running dialogue
currentIndex — Pointer to the current line. Increments with each getNextLine() call
currentDialogue — The currently active Dialogue object
Methods:
load() — Loads /Dialogues/dialogue.json as an InputStream and parses it into DialogueData using the Gson library
startDialogue(id) — Retrieves the dialogue from the map by ID, sets currentLines and resets currentIndex to 0
getCurrentCharacter() — Returns the character field of currentDialogue
getSprite() — Returns the sprite field of currentDialogue
getNextLine() — Returns the line at currentIndex and increments it by 1
isFinished() — Returns true when currentIndex has reached the end of currentLines
________________________________________
com.dungeons.Controllers
DialogueBoxController.java — FXML controller for the dialogue box UI. Connects DialogueManager to the visual elements.
Fields:
dialogueText — FXML Label displaying the current dialogue line
characterName — FXML Label displaying the character's name
character1 — FXML ImageView displaying the character's sprite
dialogueManager — Instance of DialogueManager for retrieving dialogue data
onFinished — Runnable callback called when the dialogue ends
Methods:
initialize() — Sets up keyboard listeners (Enter, Space) and mouse listener (left click) to call nextLine()
setDialogueManager(dm) — Sets the DialogueManager instance
setOnFinished(callback) — Sets the function called when the dialogue ends
startDialogue(id) — Starts the dialogue with the given ID. Sets the character name, first line and sprite
nextLine() — Advances to the next line. Calls onFinished when the dialogue is finished
setSprite(view, spriteName) — Loads the sprite image from /sprites/DialougeSprites/ and sets it on the ImageView
________________________________________
com.dungeons.screens
GameScreen — Dialogue-related fields and methods.
Fields:
activeDialogue — Holds the currently active dialogue controller. Null if no dialogue is running
activeDialogueNode — Holds the visual node of the dialogue added to gameRoot
interactionLocked — Flag that blocks all interactions during dialogue and combat. Prevents retriggering
Dialogue trigger flow — When MapManager detects a zone with type "dialogue:", InteractListener is called with the type and tile coordinates. GameScreen checks if the type starts with "dialogue:" and:
•	Stores the tile coordinates in lastDialogueTileX/Y
•	Sets interactionLocked = true and stops the loop — this is the flag that prevents retriggering
•	Extracts the dialogue ID from the type (e.g. "dialogue:cassie_encounter" → "cassie_encounter")
•	Creates DialoguesScreen and retrieves DialogueBoxController from the loader
•	Passes the DialogueManager and onFinished callback to the controller
•	Calls startDialogue(dialogueId) and adds the node to gameRoot
________________________________________
com.dungeons.screens — FXML
DialogueBox.fxml — Defines the visual layout of the dialogue box. Spans the full screen (800x600) with a transparent background so the game remains visible behind it. Composed of two AnchorPanes positioned at the bottom of the screen:
Right panel — Displays the character sprite as an ImageView (fx:id="character1") with a lightened background.
Left panel — Contains two sections:
A dark strip at the top with the characterName Label displaying the speaker's name
The dialogueText Label with wrapped text displaying the dialogue lines

 

Kejvi Spaho (CASSIE YARN)
com.dungeons.systems.CombatSystem — Combat Engine & Turn Logic
com.dungeons.Controllers — CombatController (UI, Input, Animations)
com.dungeons.characters — BossLoader (Boss AI & Move Selection)
/resources/CharacterStats — Stats.json (Characters, Stats, Abilities)
/resources/sprites/characters — Combat Sprites & Ability Sprites
________________________________________
Combat System
The combat system is split into two layers. CombatEngine.java handles all calculations and turn logic, while CombatController.java handles UI, animations, and player input. The controller calls the engine and reads a TurnLog result, and the engine does not depend on UI code. All combat data such as HP, ATK, DEF, abilities, effects, and sprites are loaded from Stats.json.
________________________________________
Combat Flow
GameScreen.java runs a loop checking player position every frame. MapManager.java compares this position against interact zones loaded from the map. When the player enters a zone marked as "fight", the game loop stops and the combat screen is loaded (battleScreen.fxml with CombatController).
CombatController.startCombat(bossID) must be called with a valid character ID from Stats.json (case-sensitive). The boss ID is currently hardcoded as CassieYarn for testing but should be passed dynamically when triggered from the map.
________________________________________
Turn System
The player selects an action from the UI. If an attack is selected, handlePlayerAttack is called with the move index (0–3). The UI locks during execution.
CombatController calls processTurnByIndex in CombatEngine, which processes the full round (player action, boss action, effects) and returns a TurnLog.
TurnLog contains move names, damage values, HP results, and outcome state. CombatController reads this and plays animations accordingly.
After animations finish, finishTurnUpdate unlocks the UI, updates the log, and checks for end conditions. If the fight ends, onCombatEnd is called.
________________________________________
Combat Engine
CombatEngine.java processes turns in this order:
•	Apply DOT on player
•	Reduce cooldowns
•	Check stun (skip turn if active)
•	Execute player move
•	Check boss death
•	Apply DOT on boss
•	Boss AI selects move
•	Execute boss move
•	Check player death
Damage is calculated as base move damage plus attacker ATK, reduced by target DEF (minimum 1). Multi-hit moves split damage across hits.
Status effects are applied after moves using a chance roll. Effects include DOT (12 damage per turn), skip (lose next turn), and halfDmg (reduced damage output).
________________________________________
Special Actions
Guard has a 55% chance to block all damage and has a 3-turn cooldown.
Counter has a 30% chance to reflect the attack.
Talk has a 50% chance to either halve boss damage or increase it by 20%.
Insult has a 35% chance to reduce damage to 30% or a 65% chance to double it.
These actions set modifiers in the engine and are resolved during the boss attack phase. Each can only be used once per turn.
The fourth ability uses a cooldown defined in Stats.json and becomes unavailable while the cooldown is active.
________________________________________
Stats.json
Each character entry contains a stats block (hp, atk, def) and an abilities array.
Each ability includes name, description, damage (and optional hits), statusEffect (DOT, skip, halfDmg or null), duration, chance, abilitySprite path, hitStyle, and cooldown.
Hit styles define animation behavior:
single — one large hit
rapid — multiple fast hits
heal — healing effect
clone — doubles boss HP percentage
Bosses include sprite states (neutral, angry, thinking, defeated). Player sprites include neutral, angry, and defeated. All paths are resource-relative.
StatsLoader.java parses this file manually. Missing fields default to 0 or empty. Invalid character IDs will crash on load.
________________________________________
Boss AI
Boss logic is defined in BossLoader.java.
CassieYarn — selects moves randomly from damage and heal options.
FreakyRelah — adapts based on player HP:
•	Above 60% uses random damage moves
•	Between 40–60% prefers status effects
•	Below 40% uses highest damage move
JohnMKati — checks own HP and player HP:
•	Below 40% uses clone once
•	Above 50% prefers status effects
•	Low player HP favors high stun chance moves
AI receives player HP percentage each turn via setLastKnownPlayerHpPercent.
________________________________________
Sprite System
Two sprite layers are used:
•	Mood sprite (neutral/angry based on HP)
•	Ability sprite (temporary attack visuals)
Ability sprites are stored separately and must be cleared after each animation using clearAbilitySprite.
CombatController always reads mood sprites for idle state.
thinkingRevertTimer resets the boss sprite after delay and must be cancelled before attack animations to avoid overwriting ability sprites.
________________________________________
Map & Scene Transitions
GameScreen.java and MapManager handle fight triggers through interact zones marked as "fight".
After combat, onCombatEnd triggers a delay and calls loadNextArea.
BOSS_NEXT_MAP defines transitions:
CassieYarn → MobRoom3
FreakyRelah → MobRoom5
JohnMKati → pending
returnFromCombatWithMap loads the new map and resumes gameplay.
This requires GameScreen.getInstance() to return the active game instance.

