# BrokenLens Reborn Minigames
Plugin based on PowerNukkitX that recreates from scratch, with high fidelty, the mechanics of MurderMystery minigame (mm) on BrokenLens, a server closed in 2022. 
The plugin supports multiple minigames, which could be developed later. 
This project is not affiliated with official BrokenLens.

## First setup
- Follow the steps listed under the **Install** section, to install PowerNukkitX, at https://github.com/PowerNukkitX/PowerNukkitX
- Start the server for the first time, in order to create the necessary folders
- Download the plugin and put it in the plugins folder
- Download the Resource Pack and extract the zip content in the resource_packs folder
- Set up the plugin | next chapter

## config.yml settings and adding maps
After starting the plugin for the first time, the plugin will generate a **config.yml** file, where you can edit the **settings** of the plugin and **add valid maps**.
You can use ```/mm map add [etc...]``` to add new maps, ```/mm map edit [etc...]``` to edit certain settings, ```/mm map newspawn <mapId>``` to add new map spawns.
You have to manually delete maps.
Here an example of how to set up a mm map inside config.yml:

```
world:
  lobby: "lobby"                        # folder name of the lobby world 
  default-world: "world"                # folder name of the default map world
  enabled-maps:                         # which maps are enabled ingame for vote/selection
    - "testworld"
    - "museum"
  arena-regions:
    testworld:                          # map ID (better -but not necessary- to use the same name of the folder of the world) (it has to be lowercase)
      name: "Test Map"                  # whatever name you want to show ingame
      world: "testworld"                # folder name of the map world
      min: "-20 0 21"                   # min-max coordinates (x y z) to select the total volume of the map
      max: "323 84 152"
      night-vision: true                # true/false = on/off
      weather: "Clear"                  # "Clear"/"Rain"/"Storm"
      builders:
        - "Lesz"                        # List of the map builders' names
        - "@BrokenLensTeam"

      spawns:                           # List of the possible players' spawns when starting a match
        - "50 5 23"
        - "11 5 42"
        - "64 3 80"
    museum:
      # ...
```

## Commands and usage
### Overview
```/mm <join|joinall|leave|start|stop|map|setrules|debug>```

```/mm join``` - Join a game: after ```{minPlayers}``` players joined, the countdown will start and you will be able to vote for a map and the time of the day  
```/mm joinall``` - Make all online players join a game  
```/mm leave``` - Leave a game  
```/mm start``` - Force the game to start for all joined players, skipping the countdowns  
```/mm stop``` - Force the game to stop for all players  
```/mm map <subcommand> <args>``` - Subcommands for gold mapper (check next paragraph and next section for more details)  
```/mm setrules``` - Set all the gamerules instantly in a optimal way for a mm game inside the world/level you are in (e.g. no pvp, no fall damage, no mobspawing, etc.)  
```/mm debug``` - Command class for developers in order to test features  

```/ping``` - Returns ping  
```/ping <player>``` - Returns a player's ping  

```/reloadconfig``` - Reload config.yml after external edits  

### Mapper subcommands
```/mm map add <mapId> <minCoords> <maxCoords> <worldFolder> <mapName>``` - Add a new map to config.yml  
```/mm map edit <mapId> <field> <args>``` - Edit a map settings in config.yml (editable fields: name, world, night-vision, weather, builders; for other fields you have to edit them manually in config.yml)  
```/mm map enable <mapId>``` - Enable a map in config.yml, in order to be able to play the game in it  
```/mm map disable <mapId>``` - Disable a map in config.yml  
```/mm map newspawn <mapId>``` - Save your current position as a new spawn, in the spawns list of the map, in config.yml  
```/mm map scan <mapId> <useBarriersWhitelist>``` - Scan map for valid gold spawn blocks and save locally, from min to max coords written in config.yml; add 'true' to treat saved whitelisted barriers as valid spawn blocks  
```/mm map scanforbarriers <mapId>``` - Scan and save locally barriers blocks, in order to whitelist them when scanning, if ```<useBarriersWhitelist>``` is true
```/mm map countbarriers <mapId>``` - Count barriers present in the map (no save)  
```/mm map addvolume <mapId> <from> <to>``` - Add a volume of blocks from coordinates1 to coordinates2 (same as scan: checking for valid blocks, but in a restricted volume)  
```/mm map addvolume <mapId> savecurrentpos``` - Save your position in pos1/pos2 variables in order to use them later (first time: pos1 = yourPosition, pos2 = null; second time: pos2 = yourPosition; third time -> first time)  
```/mm map addvolume <mapId> usesavedpos``` - Add a volume of blocks from pos1 to pos2 saved in variables (won't do anything if one of the variables is null)  
```/mm map removevolume <mapId> <from> <to>``` - Remove a volume of blocks from coordinates1 to coordinates2 (same as scan: checking for valid blocks, but in a restricted volume)  
```/mm map removevolume <mapId> savecurrentpos``` - Save your position in pos1/pos2 variables in order to use them later (first time: pos1 = yourPosition, pos2 = null; second time: pos2 = yourPosition; usesavedpos; third time = first time; ...)  
```/mm map removevolume <mapId> usesavedpos``` - Remove a volume of blocks from pos1 to pos2 saved in variables (won't do anything if one of the variables is null)  
```/mm map reload <mapId>``` - Reload the saved scan in cache  
```/mm map reloadbarriers <mapId>``` - Reload the saved barriers scan in cache  
```/mm map listmaps``` - Returns a list of the available maps from the gold spawn mapper  
```/mm map info <mapId>``` - Returns info about a certain map from the gold spawn mapper  

*Note*: ```<mapId>``` is the main internal name (not shown), ```<worldFolder>``` is the world folder name, ```<mapName>``` is the name displayed to players. It's better (but not necessary) to set a new mapId the same as the world folder name.  

## Making gold spawn in maps
In order to make mm gold system work, you need to use the ```/mm map``` commands to **scan each map** you are going to use, so the plugin understands where it's possible to spawn golds and saves locally the results to use them later during the games.

However, the mapper scans **every block** of the volume, starting from the **min** to the **max** coords you specified in the config.yml, so it could find more "valid" spawn blocks than necessary (for example outside the map itself, but still inside the mix-max coords).

Therefore, to **exclude** these blocks, you have two options: 1) place **barriers blocks** on top of them (only one per block is enough), or alternatively 2) use ```/mm map remove [etc..]``` to **remove a volume** of blocks from the gold spawn blocks list; or both methods together of course.
Don't worry if the volume you selected contains already excluded blocks, these will be ignored.

If you want to **include** certain **barriers blocks** as valid spawn blocks, use first ```/mm map scanforbarriers <mapId>``` to save them before the mapper's scanning, then ```/mm map scan <mapId> true``` to include those barriers scanned before as well.
