Combat System ---- How It Works


Written so whoever picks this up knows where everything is and why it was done this way. (Im here too twin, i got no idea what i made)


Where to start

The combat system lives in two places. The logic is in systems/CombatSystem/ and the visual side is in Controllers/CombatController.java. If you want to change how damage is calculated, go to CombatEngine.java. If you want to change how something looks or animates on screen, go to CombatController.java. These two should never know about each other more than necessary — the controller calls the engine and reads the result, that is it.

Everything that defines a character — their HP, attacks, damage values, sprites — comes from Stats.json. This file is in src/main/resources/CharacterStats/Stats.json. 


How a fight starts

When the player walks around the map, GameScreen.java is running a loop that checks the player position every frame. MapManager.java compares that position against interact zones loaded from the map file. When the player steps on a zone marked as "fight", GameScreen stops the game loop and loads the combat screen. The combat screen is battleScreen.fxml and its controller is CombatController.java.

CombatController has a method called startCombat that takes a boss ID string. That string must match a character name in Stats.json exactly — spelling and capitals matter. Right now the boss ID is hardcoded in the initialize method as CassieYarn for testing. In a real fight triggered from the map, whoever calls combatScreen should call startCombat with the correct boss ID before the screen is shown. 


How a turn works

The player picks something from the UI. If they pick an attack, handlePlayerAttack is called with the move index (0 through 3). This locks all the buttons so nothing can be clicked during the animation. Then it calls processTurnByIndex on CombatEngine, which runs the full round in one go — player acts, boss acts, effects tick — and returns a TurnLog.

The TurnLog is just a snapshot of what happened. It holds the move names, damage dealt on both sides, HP values after the round, and the result. CombatController reads this and plays the animations based on what the log says.

After all animations finish, finishTurnUpdate is called. This unlocks the buttons if the fight is still going, updates the turn log text area, and checks if someone died. If the fight is over it calls onCombatEnd.


The engine in detail

CombatEngine.java is where the math happens(Thanks to claude cuz I had no idea how to do ts). When processTurnByIndex is called it runs in this order. First it ticks DOT on the player if they have one active. Then it ticks cooldowns. Then it checks if the player is stunned — if yes, their turn is skipped. If not, it runs the move. After the player acts it checks if the boss is dead. If not, it ticks DOT on the boss, then runs the boss AI to pick a move, then applies that move to the player. Then it checks if the player is dead.

Damage is calculated as move damage plus the attacker's attack stat, then passed to takeDamage on the target which subtracts the target's defense and floors at 1. Multi-hit moves divide the total raw damage by the number of hits and deal each hit separately.

Status effects are applied after the move via tryApplyEffect. It rolls a random number against the move's chance value. If the roll succeeds it creates a StatusEffect object and applies it to the target. The three types are DOT which deals 12 damage at the start of each turn, skip which causes the affected party to lose their next turn, and halfDmg which halves all damage they deal for the duration/round.

Guard and counter are activated before the player attacks by clicking the guard or counter button. They set flags in the engine. When the boss attacks that turn, the engine checks those flags and rolls against fixed odds. 55 percent for guard to block completely, 30 percent for counter. After resolving, the flags are cleared and guard goes on a 3 turn cooldown. Both guard and counter can only be used once per turn.

Talk and insult work similarly. They set a talkModifier value on the engine. When the boss attacks, that modifier is applied to the damage. Talk has a 50 percent chance of halving boss damage or adding 20 percent. Insult has a 35 percent chance of reducing to 30 percent or a 65 percent chance of doubling it. Once used, talk or insult cannot be used again that turn.

The fourth player move has a cooldown field in Stats.json. After it is used, move4CooldownLeft is set to cooldown plus one. It counts down each turn. While it is above zero the button is grayed out.


Stats.json explained

Every character in the game is an entry in this file. The structure is the same for all of them. Each has a stats block with hp, atk, and def. Each has an abilities array where every move has a name, desc, effects block with damage and optionally hits, a statusEffect string which can be DOT skip or halfDmg or null, a duration, a chance between 0 and 1, an abilitySprite path, a hitStyle string, and a cooldown number.

The hitStyle field tells the animation system how to display the attack. Single means one big hit all at once. Rapid means one floating damage number per hit fired quickly one after another. Heal means the boss is healing, no damage shown. Clone means the clone mechanic which doubles boss HP percentage.

Bosses also have a sprites block with paths for neutral, angry, thinking, defeated. Players have neutral, angry, and defeated not set as I didnt have a character. All paths start with a forward slash and point inside the resources folder. For example /Sprties_CombatUI/Cassie/Neutral.jpeg. Im lazy to fix the typo, it was too late when i found out the typo.

StatsLoader.java reads this file manually with a custom parser. There is no JSON library used for the game stats. If the parser fails to find a field it returns 0 or empty string by default. If a character name is not found it throws an exception. So if a fight crashes on startup, the first thing to check is whether the boss ID passed to startCombat matches a key in Stats.json.


Boss AI

Each boss has its own move selection logic inside BossLoader.java in the chooseMove method. It checks the boss ID and calls the matching AI method. Thanks to cluade to this one too, im not smart to do 50 ifs.

CassieYarn is fully random. He picks any move that deals damage or heals.

FreakyRelah changes behavior based on the player's HP. When the player is above 60 percent HP she picks randomly from all damaging moves. Between 40 and 60 percent she prefers moves that apply stun or halfDmg. Below 40 percent she goes for the highest total damage move available, which is Capacitor Discharge.

JohnMKati checks his own HP first. If he is below 40 percent and has not cloned yet, he uses Twining to double his current HP. When the player is above 50 percent HP he prefers moves with status effects. When the player is low he goes for Iron Fist which has a high stun chance.

The AI receives the player's current HP percentage via setLastKnownPlayerHpPercent which is called at the start of every boss turn in CombatEngine. Without this call the AI would always make decisions based on stale data.


Sprite system

The boss has two completely separate sprite tracks. The mood sprite is what shows during normal gameplay — neutral when HP is above 40 percent, angry when below. The ability sprite is a temporary image shown only while the boss is performing an attack.

These must never mix. The ability sprite is stored in currentAbilitySprite on BossLoader and is cleared by calling clearAbilitySprite after every animation finishes. The method updateBossSpriteMood in CombatController always reads from getCurrentSprite on BossLoader which only looks at mood paths, never the ability path. If the wrong image keeps showing after an attack, the most likely cause is that clearAbilitySprite was not called or the thinking revert timer fired after the ability sprite loaded.

The thinking revert timer is stored as thinkingRevertTimer in CombatController. It is cancelled at the very start of executeBossTurn. If you add any new animation path that starts before executeBossTurn, make sure the timer is cancelled or the thinking sprite will overwrite the ability sprite.


Map triggers and scene transitions

CombatTrigger.java reads a TMX map file and finds object layers named FightCassie, FightFreki, or FightJohn. It maps these to boss IDs. When the player tile position is inside one of the rectangles drawn in those layers, checkTrigger returns the boss ID string.

Currently the fight trigger in GameScreen goes through the interact zone system using the type "fight" rather than CombatTrigger directly. Both systems exist. CombatTrigger is the newer cleaner approach but GameScreen uses the older interact zone approach. They do the same thing.

After a boss is defeated, onCombatEnd is called in CombatController. It waits 2 seconds then calls loadNextArea. This method looks up the boss ID in a map called BOSS_NEXT_MAP. CassieYarn leads to MobRoom3. FreakyRelah leads to MobRoom5. JohnMKati has a comment placeholder for whoever finishes the end of the game. loadNextArea calls returnFromCombatWithMap on the GameScreen singleton which loads the new map and returns the player to the world.

For this to work GameScreen must be running and its getInstance method must return the current instance. If it returns null the game screen was never started or was restarted after the instance was set.

///As a future project for you if u will keep this flop of a system made.
How to add a new boss

First add the character to Stats.json following the exact same structure as the existing bosses. Give them a unique name, stats, four abilities, and a sprites block with all paths filled in or left empty. The name you use in the JSON is the boss ID.

Then add their AI logic in BossLoader.java inside chooseMove. Add a new case to the switch statement with their ID and write a new private method for their behavior.

Then add them to BOSS_NEXT_MAP in CombatController if they should lead to a specific room after being defeated.

Then add their trigger layer to the Tiled map with the matching name in CombatTrigger.LAYER_TO_BOSS, or add a new entry to that map.

That is the full chain. JSON defines the data, BossLoader defines the behavior, CombatController defines the visuals, and the map defines where the fight starts.


How to add a new move

Open Stats.json and add the move to the character's abilities array. Fill in all fields — name, desc, effects with damage and optionally hits, statusEffect, duration, chance, abilitySprite, hitStyle, and cooldown. If a field does not apply set it to null or 0.

The hitStyle controls the animation. Use single for one hit, rapid for multiple hits shown one at a time, heal for a healing move, and clone for the clone mechanic. If you use a new hitStyle string, add handling for it in executeBossTurn in CombatController or it will silently skip the animation.

The abilitySprite path should point to an image inside resources starting with a forward slash. If the file does not exist the code prints a warning and continues without crashing.


Common bugs and their causes that took me 15 hours+ and 3 claude sessions of free tokens. 

If the boss always uses the same move, check the AI method for that boss in BossLoader. The most common cause is the AI filtering moves by a condition that only one move satisfies.

If the boss sprite gets stuck on an attack image, clearAbilitySprite was not called after the animation finished. Every branch of executeBossTurn must call it before calling finishTurnUpdate.

If HP labels overlap on screen, injectStatusLabels is removing FXML labels it should not touch. The cleanup should only remove previously injected labels by checking the stored references, not by removing all labels from the pane.

If the player can still click buttons during the boss turn, lockAllActions was not called at the start of handlePlayerAttack.

If a fight crashes on startup with a character not found error, the boss ID passed to startCombat does not match any key in Stats.json. Check spelling and capitals.

If the game does not load the next map after a fight, either MapManager.loadMap is not calling the mapChangeListener, or GameScreen.getInstance returned null.  (Yes this was made by claude and edited by me as I make changes to the code and I suck at explaining) (If u got a question idc :thumbsUP) 