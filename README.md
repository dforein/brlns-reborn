# BrokenLens Reborn Minigames
Plugin based on PowerNukkitX that recreates from scratch, with high fidelty, the mechanics of MurderMystery minigame (mm) on BrokenLens, a server closed in 2022. 
The plugin supports multiple minigames, which could be developed later. 
This project is not affiliated with official BrokenLens.

## First setup
- Follow the steps listed under the **Install** section, to install PowerNukkitX, at https://github.com/PowerNukkitX/PowerNukkitX
- Start the server for the first time, in order to create the necessary folders
- Download the plugin and put it in the **plugins** folder
- Download the Resource Pack and put the zip file in the **resourcepacks** folder
- Set up the plugin | next chapter

## config.yml settings and adding maps
After starting the plugin for the first time, the plugin will generate a **config.yml** file, where you can edit the **settings** of the plugin and **add valid maps**.
You can use ```/mmop map add [etc...]``` to add new maps, ```/mmop map edit [etc...]``` to edit certain settings, ```/mmop map newspawn <mapId>``` to add new map spawns.
You have to manually delete maps: open **config.yml** and remove everything starting from "enabled-maps: " to the end. Change ```default-world``` later, when you have a first or favorite map world, to use as a fallback in case the other worlds give some errors.
Set in **pnx.yml** ```loadAllLevels = true```, because the plugin needs all worlds already loaded.
Here an example of how to set up a mm map inside config.yml:

```YML
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
```/mm <join|joinall|leave>```  
```/mmop <start|stop|map|setrules|debug>```  

**MM commands**  
These commands are accessible to every player, like ping (everything else is exclusive to ops)
```/mm join``` - Join a game: after ```{minPlayers}``` players joined, the countdown will start and you will be able to vote for a map and the time of the day  
```/mm joinall``` - Make all online players join a game  
```/mm leave``` - Leave a game  
  
**MM Operator commands**  
```/mmop start``` - Force the game to start for all joined players, skipping the countdowns  
```/mmop stop``` - Force the game to stop for all players  
```/mmop map <subcommand> <args>``` - Subcommands for map management and gold mapper (check next paragraph and next section for more details)  
```/mmop setrules``` - Set all the gamerules instantly in a optimal way for a mm game inside the world/level you are in (e.g. no pvp, no fall damage, no mobspawing, etc.)  
```/mmop debug``` - Command class for developers in order to debug/test features  
  
**General commands**  
```/ping``` - Returns ping  
```/ping <player>``` - Returns a player's ping  
  
```/globalchat``` - Toggle global/local world chat  
  
```/reloadconfig``` - Reload config.yml after external edits  


### Map management subcommands
```/mmop map add <mapId> <minCoords> <maxCoords> <worldFolder> <mapName>``` - Add a new map to config.yml  
```/mmop map edit <mapId> <field> <args>``` - Edit a map settings in config.yml (editable fields: name, world, night-vision, weather, builders; for other fields you have to edit them manually in config.yml)  
```/mmop map enable <mapId>``` - Enable a map in config.yml, in order to be able to play the game in it  
```/mmop map disable <mapId>``` - Disable a map in config.yml  
```/mmop map newspawn <mapId>``` - Save your current position as a new spawn, in the spawns list of the map, in config.yml  
```/mmop map scan <mapId> <useBarriersWhitelist>``` - Scan map for valid gold spawn blocks and save locally, from min to max coords written in config.yml; add 'true' to treat saved whitelisted barriers as valid spawn blocks  
```/mmop map scanforbarriers <mapId>``` - Scan and save locally barriers blocks, in order to whitelist them when scanning, if ```<useBarriersWhitelist>``` is true
```/mmop map countbarriers <mapId>``` - Count barriers present in the map (no save)  
```/mmop map addvolume <mapId> <from> <to>``` - Add a volume of blocks from coordinates1 to coordinates2 (same as scan: checking for valid blocks, but in a restricted volume)  
```/mmop map addvolume <mapId> savecurrentpos``` - Save your position in pos1/pos2 variables in order to use them later (first time: pos1 = yourPosition, pos2 = null; second time: pos2 = yourPosition; third time -> first time)  
```/mmop map addvolume <mapId> usesavedpos``` - Add a volume of blocks from pos1 to pos2 saved in variables (won't do anything if one of the variables is null)  
```/mmop map removevolume <mapId> <from> <to>``` - Remove a volume of blocks from coordinates1 to coordinates2 (same as scan: checking for valid blocks, but in a restricted volume)  
```/mmop map removevolume <mapId> savecurrentpos``` - Save your position in pos1/pos2 variables in order to use them later (first time: pos1 = yourPosition, pos2 = null; second time: pos2 = yourPosition; usesavedpos; third time = first time; ...)  
```/mmop map removevolume <mapId> usesavedpos``` - Remove a volume of blocks from pos1 to pos2 saved in variables (won't do anything if one of the variables is null)  
```/mmop map reload <mapId>``` - Reload the saved scan in cache  
```/mmop map reloadbarriers <mapId>``` - Reload the saved barriers scan in cache  
```/mmop map listmaps``` - Returns a list of the available maps from the gold spawn mapper  
```/mmop map info <mapId>``` - Returns info about a certain map from the gold spawn mapper  

*Note*: ```<mapId>``` is the main internal name (not shown), ```<worldFolder>``` is the world folder name, ```<mapName>``` is the name displayed to players. It's better (but not necessary) to set a new mapId the same as the world folder name.  

## Making gold spawn in maps
In order to make mm gold system work, you need to use the ```/mmop map``` commands to **scan each map** you are going to use, so the plugin understands where it's possible to spawn golds and saves locally the results to use them later during the games.

However, the mapper scans **every block** of the volume, starting from the **min** to the **max** coords you specified in the config.yml, so it could find more "valid" spawn blocks than necessary (for example outside the map itself, but still inside the mix-max coords).

Therefore, to **exclude** these blocks, you have two options: 1) place **barriers blocks** on top of them (only one per block is enough), or alternatively 2) use ```/mmop map remove [etc..]``` to **remove a volume** of blocks from the gold spawn blocks list; or both methods together of course.
Don't worry if the volume you selected contains already excluded blocks, these will be ignored.

If you want to **include** certain **barriers blocks** as valid spawn blocks, use first ```/mmop map scanforbarriers <mapId>``` to save them before the mapper's scanning, then ```/mmop map scan <mapId> true``` to include those barriers scanned before as well.
