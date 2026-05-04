**Kledi Allamani** 



com.dungeons.world



====================





**InteractZone**



Perfaqeson nje tile te vetme interaktive ne harte.

Ruan koordinatat e tiles (x, y), llojin e nderveprimit (p.sh. "fight", "shop",

"dialogue:cassie\_encounter"), dhe nje flamur triggered per te parandaluar riaktivizimin.



**TransitionZone**



Perfaqeson nje tile qe kalon lojtarin ne nje harte tjeter.

Ruan koordinatat e tiles (x, y), emrin e hartes target, dhe opsionalisht

koordinatat e spawn ne harten target. spawnX/spawnY me vlere -1 do te thote

qe perdoret spawnpoint default i hartes target.



**Map**



Ngarkon dhe ruan te gjitha te dhenat per nje harte nga nje file JSON i eksportuar nga Tiled.



Fields:

width, height - Dimensionet e hartes ne tile

layers - Map i renditur i emrit te layer -> array GID

collisionLayers - Nenbashkesi e layer-ve qe perdoren per kontrollin e perplasjes

tilesetRanges - Mapon firstgid -> celes tileset per zgjidhjen e GID

transitions - Te gjitha objektet TransitionZone te ngarkuara nga object layers

interactZones - Te gjitha objektet InteractZone te ngarkuara nga object layers

spawnX, spawnY - Koordinatat default te spawn te lojtarit ne tile

currentMapName - Emri i hartes aktuale te ngarkuar



Methods:

load(mapName) - Ngarkon JSON e hartes nga /maps/{mapName}.json

isSolid(x, y) - Kthen true nese tile eshte jashte kufijve ose ka

vlere jo-zero ne ndonje collision layer

resolveTile(rawGid) - Heq flamujt e flip nga nje GID dhe kthen

\[tilesetKey, localIndex]

clearLayer(layerName) - Vendos te gjitha tiles ne nje layer ne 0



Object layer names recognised:

"Spawnpoint" - Vendos koordinatat e spawn te hartes

"Transition" - Kalon ne harten tjeter ne MAP\_TRANSITION\_CHAIN

"TransitionShopRoom" - Kalon ne ShopRoom

"TransitionChestRoom" - Kalon ne ChestRoom

"TransitionBackFromShop" - Kthen ne harten e meparshme te koordinatat e shop

"TransitionBackFromChest" - Kthen ne harten e meparshme te koordinatat e chest

"Fight" - Krijon zona fight interaktive

"Shop" - Krijon zona shop interaktive

"Chest" - Krijon zona chest interaktive

"cassie\_encounter" - Krijon zona dialogue:cassie\_encounter

"freki\_encounter" - Krijon zona dialogue:freki\_encounter

"merchant\_enter" - Krijon zona dialogue:merchant\_enter

"johnmkati\_lab\_reveal" - Krijon zona dialogue:johnmkati\_lab\_reveal



**TilesetManager**



Ngarkon imazhet e tileset dhe i ndan ato ne imazhe individuale 16x16 tile.



Methods:

loadAll() - Ngarkon te gjitha tileset e perdorura nga loja

get(tileset, localId) - Kthen imazhin per nje tileset dhe indeks lokal te dhene,

ose null nese eshte jashte kufijve



Tilesets loaded:

(emrat mbeten te njejte)



**MapRenderer**



Vizaton te gjitha layer-at e tiles te hartes aktuale ne nje JavaFX GraphicsContext.

Layer-at vizatohen ne rendin qe shfaqen ne harte (LinkedHashMap ruan rendin e futjes nga Tiled).

Cdo tile vizatohet me madhesi TILE\_SIZE \* SCALE (16 \* 2 = 32px per tile).



Methods:

render(gc) - Vizaton te gjitha layer-at

drawLayer(gc, layer) - Vizaton nje layer te vetem, duke kthyer cdo GID ne nje

imazh me ane te Map.resolveTile() dhe TilesetManager.get()



**MapManager**



Koordinon ngarkimin e hartave, tranzicionet dhe aktivizimin e zonave interaktive.

Thirret cdo frame nga GameScreen me pozicionin aktual te lojtarit ne tile.



Fields:

currentMap - Objekti Map aktual aktiv

transitionCooldown - Parandalon aktivizimin e zonave interaktive ne frame-in e pare pas tranzicionit



Methods:

loadMap(mapName) - Krijon dhe ngarkon nje harte te re

getCurrentMap() - Kthen hartën aktive

checkInteractions(x, y) - Kontrollon tranzicionet dhe pastaj zonat interaktive

markFightDone(x, y) - Shënon te gjitha zonat fight si te perfunduara

markDialogueDone(x, y) - Shënon te gjitha zonat dialogue si te perfunduara



Interfaces:

MapChangeListener.onMapChanged(newMap, spawnX, spawnY)

\- Thirret kur lojtari shkel ne nje zone tranzicioni

InteractListener.onInteract(type, tileX, tileY)

\- Thirret kur lojtari shkel ne nje zone interaktive



Special behaviour:

Nese nje zone fight dhe nje zone dialogue ndajne te njejtin tile,

dialogue aktivizohet i pari. Fight aktivizohet ne hapin tjeter te lojtarit ne ate tile.



com.dungeons.systems



====================



**Player**



Menaxhon levizjen e lojtarit, kontrollin e perplasjes dhe vizatimin.

Pozicioni ruhet ne koordinata pixel te shkallezuara (tile \* 16 \* 2).



Constants:

SPEED - 2.0 pixel per frame

TILE\_SIZE - 16 (madhesia e tile pa shkallezim)

SCALE - 2 (duhet te perputhet me MapRenderer dhe GameScreen)

SIZE - 14 (madhesia e hitbox ne pixel pa shkallezim)



Methods:

setMap(map) - Vendos harten per kontrollin e perplasjes

setPosition(x, y) - Teleporton lojtarin ne koordinata pixel te shkallezuara

keyPressed(key) - Aktivizon levizjen per WASD ose shigjeta

keyReleased(key) - Caktivizon levizjen

clearInput() - Pastron te gjitha input-et e levizjes (perdoret pas combat)

update() - Aplikon levizjen dhe perplasjen cdo frame

render(gc) - Vizaton lojtarin ne pozicionin aktual

getTileX(), getTileY()- Kthen koordinatat e tile bazuar ne qender per kontrolle interaksioni

getX(), getY() - Kthen pozicionin raw ne pixel



com.dungeons.Controllers



========================



**DialogueBoxController**



Controller FXML per UI e kutise se dialogut.

Kalimi midis rreshtave behet me Enter, Space, ose klik te majte te mausit.



Methods:

setDialogueManager(dm) - Vendos DialogueManager

setOnFinished(callback) - Vendos funksionin qe thirret kur dialogu mbaron

startDialogue(id) - Ngarkon nje dialog sipas ID dhe shfaq rreshtin e pare

nextLine() - Kalon ne rreshtin tjeter ose thirr onFinished



com.dungeons.screens



====================



**GameScreen** (pjesa e Kledi Allamani)



Menaxhon game loop, vizatimin e hartes, lojtarin, kameran dhe interaksionet.



Map-related fields:

tilesetManager - Ngarkon dhe ruan te gjitha imazhet e tiles

mapManager - Menaxhon tranzicionet dhe zonat interaktive

mapRenderer - Vizaton harten aktuale cdo frame

interactionLocked - Parandalon interaksionet gjate combat/dialogue

fightTileX/Y - Koordinatat e tile per fight-in e fundit

lastDialogueTileX/Y - Koordinatat e tile per dialogun e fundit



Map-related methods:

getRoot() - Inicializon sistemin e hartes, ngarkon MobRoom1, vendos spawn

returnFromCombat() - Thirret kur mbaron fight; shenon fight si te perfunduar,

pastron layer-in e mob dhe vazhdon lojen

returnFromCombatWithMap(name) - Si me siper por ngarkon nje harte te re fillimisht

update() - Therrit checkInteractions cdo frame kur nuk eshte locked

updateCamera() - Kufizon kameran brenda hartes dhe e mban te qendruar te lojtari

