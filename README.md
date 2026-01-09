# BrokenLens Reborn Minigames
Plugin based on PowerNukkitX that recreates from scratch, with high fidelty, the mechanics of MurderMystery minigame (mm) on BrokenLens, a server closed in 2022. 
The plugin supports multiple minigames, which could be developed later. 
This project is not affiliated with official BrokenLens.

## First setup
- Follow the steps listed under the **Install** section, to install PowerNukkitX, at https://github.com/PowerNukkitX/PowerNukkitX
- Start the server for the first time, in order to create the necessary folders
- Download the plugin and put it in the plugins folder
- Download the Resource Pack and put it in the resource_packs folder
- Set up the plugin | next chapter

## config.yml settings and adding maps
After starting the plugin for the first time, the plugin will generate a **config.yml** file, where you can edit the **settings** of the plugin and **add valid maps**.
Here an example of how to set up a mm map inside config.yml:

```
world:
  lobby: "lobby"                        # folder name of the lobby world 
  default-world: "world"                # folder name of the default map world
  enabled-maps:                         # which maps are enabled ingame for vote/selection
    - "map1"
    - "map2"
  arena-regions:
    map1:
      name: "Test Map"                  # whatever name you want to show ingame
      world: "world"                    # folder name of the map world
      min: [0, 80, 0]                   # min-max coordinates to select the total volume of the map
      max: [100, 80, 100]
      night-vision: true                # true/false = on/off
      weather: "Clear"                  # "Clear"/"Rain"/"Storm"
      builders:
        - "Lesz"                        # List of the map builders' names
        - "@BrokenLensTeam"

      spawns:                           # List of the possible players' spawns when starting a match
        - [50, 80, 50]
        - [20, 80, 20]
        - [80, 80, 80]
    map2:
      # ...
```

## Commands and usage
### Overview
```/mm <join|leave|start|stop|listworlds|world|map|setrules>```

```/mm join``` - Join a game: after ```{minPlayers}``` players joined, the countdown will start and you will be able to vote for a map and the time of the day  
```/mm leave``` - Leave a game  
```/mm start``` - Force the game to start for all joined players, skipping the countdowns  
```/mm stop``` - Force the game to stop for all players  
```/mm listworld``` - Returns a list of the worlds (folders)  
```/mm world <worldFolder>``` - Teleports you to into the selected world  
```/mm map <subcommand> <args>``` - Subcommands for gold mapper (check next paragraph and next section for more details)  
```/mm setrules``` - Set all the gamerules instantly in a optimal way for a mm game inside the world/level you are in (e.g. no pvp, no fall damage, no mobspawing, etc.)  

```/ping``` - Returns ping  
```/ping <player>``` - Returns a player's ping  

### Mapper subcommands
```/mm map list``` - Returns a list of the available maps  
```/mm map scan <mapName> [true]``` - Scan map for valid gold spawn blocks and save locally, from min to max coords written in config.yml; add 'true' to treat saved whitelisted barriers as valid spawn blocks  
```/mm map scanforbarriers <mapName>``` - Scan and save locally barriers blocks, in order to whitelist them when scanning, if [true] enabled  
```/mm map countbarriers <mapName>``` - Count barriers present in the map (no save)  
```/mm map savepos1``` - Save current coordinates in internal variable ```position1``` (and resets ```position2``` for safety)  
```/mm map savepos2``` - Save current coordinates in internal variable ```position2```  
```/mm map remove <mapName> <x1> <y1> <z1> <x2> <y2> <z2>``` - Remove a volume of blocks from pos1 to pos2  
```/mm map remove <mapName> sp``` - Use s.aved p.ositions coords in the internal variables (need both)  
```/mm map add <mapName> <x1> <y1> <z1> <x2> <y2> <z2>``` - Add a volume of blocks from pos1 to pos2 (same as scan: checking for valid blocks, but in a restricted volume)  
```/mm map reload <mapName>``` - Reload the saved scan in cache  
```/mm map reloadbarriers <mapName>``` - Reload the saved barriers scan in cache  
```/mm map info <mapName>``` - Returns info about a certain map  

*Note*: ```<mapName>``` is the main internal name (not the one shown ingame) e.g. ```map1```, not ```Test Map```.  

## Making gold spawn in maps
In order to make mm gold system work, you need to use the ```/mm map``` commands to **scan each map** you are going to use, so the plugin understands where it's possible to spawn golds and saves locally the results to use them later during the games.

However, the mapper scans **every block** of the volume, starting from the **min** to the **max** coords you specified in the config.yml, so it could find more valid spawn blocks than necessary (for example outside the map itself, but still inside the mix-max coords).

Therefore, to **exclude** these blocks, you need to 1) place **barriers blocks** on top of them (only one per block is enough), 2) or use ```/mm map remove [etc..]``` to **remove a volume** of blocks from the gold spawn blocks list; or both methods together of course.
Don't worry if the volume you selected contains already excluded blocks, these are ignored.

If you want to **include** certain **barriers blocks** as valid spawn blocks, use first ```/mm map scanforbarriers <mapName>``` to save them before the mapper's scanning, then ```/mm map scan <mapName> true``` to include those barriers scanned before as well.
