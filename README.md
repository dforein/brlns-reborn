# BrokenLens Reborn Minigames
Plugin based on PowerNukkitX that recreates from scratch, with high fidelty, the mechanics of MurderMystery minigame (mm) on BrokenLens, a server closed in 2022. 
The plugin supports multiple minigames, which could be developed later. 
This project is not affiliated with official BrokenLens.

## config.yml settings and adding maps
After starting the plugin for the first time, the plugin will generate a config.yml file, where you can edit the settings of the plugin and add valid maps.
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
        - "ItzDS35"                     # List of the map builders' names
      spawns:                           # List of the possible players' spawns when starting a match
        - [50, 80, 50]
        - [20, 80, 20]
        - [80, 80, 80]
    map2:
      # ...
```

## Making gold spawn in maps 
In order to make mm gold system work, you'd need to use the ```/mm map``` commands to scan the maps you are going to use,
so the plugin understands where it's possible to spawn golds. ps: ```<mapName>``` is the main name (not the one shown ingame) e.g. ```map1```, not ```Test Map```.

However it scans from the min to the max coords you specified in the config.yml, so it could find more valid spawn blocks
than necessary (for example outside the map itself, but still inside the mix-max coords).

Therefore, you need to place barriers on top of blocks to exclude these (only one per block is enough), or use ```/mm map remove [etc..]```
(use the command with no other arguments to see the usage) to remove from the gold spawn blocks list a volume of blocks. 
Don't worry if the volume you selected contains already excluded blocks.

If you want to include certain barriers as valid spawn blocks, use first ```/mm map scanforbarriers <mapName>```, 
then ```/mm map scan <mapName> true``` to include those barriers scanned before as well.
