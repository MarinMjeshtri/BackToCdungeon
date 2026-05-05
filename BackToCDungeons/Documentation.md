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



**Jon Toska**



====================


dialogue.json

Nje objekt JSON me nje key te vetëm dialogues, i cili permban ID-ne e dialogut (p.sh. "merchant_enter", "cassie_defeat").
Me pas ndahet ne 3 keys te tjera:

character - string me emrin e karakterit
sprite - string me emrin e skedarit te sprites
lines - array stringjesh, ku çdo element është nje rresht dialogu ne rend


characters.json

Nje objekt JSON me nje key te vetëm characters, i cili permban emrat e karaktereve (p.sh. "Player").
Me pas ndahet ne:

stats - objekt me fushat atk, def, hp si numra te plotë
sprites - objekt me fushat neutral, angry, defeated si strings te skedarëve
abilities - array objektesh me emrat, efektet dhe damage.
(Jane bere ndryshime te tjera me vone per te lehtesuar punen e Combat)


Dialogue.java

Nje klase e thjesht qe pefaqeson te dhenat qe do ruhen nga cdo dialog te cilat jane:


character - Emri i karakterit folës
sprite - Emri i skedarit te sprites
lines - Array i rreshtave te dialogut ne rend



DialogueData.java

(Nevojitet per te perdorur Gson)

Vendi ku ruhen te dhenat qe behen parse nga JSON ne Java Objects nepermjet Gson.
Gson lexon JSON dhe mbush DialogueData e cila sherben si nje wrapper class.

DialogueManager e merr data.dialogues dhe e ruan tek dialogues 



DialogueManager.java


Ngarkon dhe menaxhon te gjitha te dhenat e dialogut nga skedari JSON. Mban gjendjen e dialogut aktual gjate nje bisede aktive.

Perdor Hashmaps (p.sh Map<String, Dialogue> dialogues) per te ruajtur dialogjet.
Ajo i vendos te dhenat ne cifte key-value ku String esht ID-ja e dialogut dhe Dialogue eshte i gjith dialogu i asaj ID.
Perdoret per arsye optimizimi pasi kompleksiteti i kohes per te kerkuar te dhenat ne nje Map eshte O(1).


currentLines - Array i rreshtave per dialogun aktual ne ekzekutim
currentIndex - Treguesi i rreshtit aktual; rritet me cdo thirrje getNextLine()
currentDialogue - Objekti Dialogue aktualisht aktiv


load() - Ngarkon (/Dialogues/dialogue.json) si InputStream dhe e ben parse ne DialogueData me librarin Gson

startDialogue(id) - Merr dialogun nga harta me ID-ne e dhene, vendos currentLines dhe rinis currentIndex ne 0

getCurrentCharacter() - Kthen character te currentDialogue

getSprite() - Kthen sprite te currentDialogue

getNextLine() - Kthen rreshtin ne currentIndex dhe e rrit ate me 1

isFinished() - Kthen true kur currentIndex ka arritur fundin e currentLines




DialogueBoxController.java


Controller FXML per UI-n e kutis se dialogut. Lidh Dialogue Manager me elementet vizual.

dialogueText - Label FXML qe shfaq rreshtin aktual te dialogut
characterName - Label FXML qe shfaq emrin e karakterit
character1 - ImageView FXML qe shfaq sprites te karakterit
dialogueManager - Instanca e DialogueManager per marrjen e te dhenave te dialogut
onFinished - Callback Runnable qe thirret kur dialogu mbaron


initialize() - Vendos listeners per tastieren (Enter, Space) dhe mausin (klik i majte) per te thirrur nextLine()

setDialogueManager(dm) - Vendos DialogueManager

setOnFinished(callback) - Vendos funksionin qe thirret kur dialogu mbaron

startDialogue(id) - Fillon dialogun me ID-ne e dhene; vendos emrin e karakterit, rreshtin e pare dhe sprites

nextLine() - Kalon ne rreshtin tjeter; nese dialogu ka mbaruar thirr onFinished

setSprite(view, spriteName) - Ngarkon imazhin e sprites nga /sprites/DialougeSprites/ dhe e vendos ne ImageView






GameScreen.java - Pjeset e lidhura me dialogun.



activeDialogue - Mban controller-in e dialogut aktual aktiv; null nese nuk ka dialogue ne ekzekutim
activeDialogueNode - Mban node vizuale te dialogut qe shtohen ne gameRoot
interactionLocked - Flamur qe bllokon te gjitha interaksionet gjate dialogut (dhe combat-it); parandalon retriggering


Kur MapManager zbulon nje zone me tip "dialogue:", InteractListener thirret me tipin dhe koordinatat e tiles. 
GameScreen kontrollon nese tipi fillon me "dialogue:" dhe:

- Ruan koordinatat e tiles ne lastDialogueTileX/Y
- Vendos interactionLocked = true dhe ndalon loop-in — ky eshte flamuri qe parandalon retriggering
- Nxjerr ID-ne e dialogut nga tipi (p.sh. "dialogue:cassie_encounter" → "cassie_encounter")
- Krijon DialoguesScreen dhe merr DialogueBoxController nga loader-i
- I jep controller-it DialogueManager dhe callbackun onFinished
- Thërret startDialogue(dialogueId) dhe shton node ne gameRoot




DialogueBox.fxml

Percakton paraqitjen vizuale te kutise se dialogut. Shtrihet mbi te gjithe ekranin (800x600) me sfond transparent per te mos bllokuar lojën prapa.
Perbehet nga dy AnchorPane te pozicionuara ne fund te ekranit:
Paneli i djathte — shfaq sprites e karakterit si ImageView (fx:id="character1") me sfond te zbardhur.
Paneli i majte — permban dy pjese:

Nje shirit te errët ne krye me Label-in characterName qe shfaq emrin e karakterit
Label-in dialogueText me tekst te mbështjellë qe shfaq rreshtat e dialogut
