package org.brlnsreb.commands.maintenance.physics;

import java.util.List;
import java.util.stream.Collectors;

import org.brlnsreb.core.levels.LevelManager;
import org.brlnsreb.utils.abstraction.FormAbstract;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.form.response.CustomResponse;
import org.powernukkitx.form.window.CustomForm;

public class PhysicsForm extends FormAbstract {

    private static final List<String> blockRanges = List.of(
        "All world",
        "1", "2", "3", "4", "5", "6", "7", "10", "20"
    );

    public static void openForm(Player player) {
        if (!checkCooldown(player)) return;

        CustomForm form = new CustomForm("Physics settings");
        List<String> levels = Server.getInstance().getLevels().values().stream()
            .map(level -> level.getName())
            .collect(Collectors.toUnmodifiableList());

        boolean physicsPlayerLevel = LevelManager.arePhysicsEnabledIn(player.level);
        form.addDropdown(
            "Level", 
            levels, 
            levels.indexOf(player.getLevelName())
        );
        form.addToggle("Enable physics", physicsPlayerLevel);
        form.addStepSlider(
            "Block range of working physics from any player", 
            blockRanges, 
            physicsPlayerLevel ? LevelManager.getPhysicsRangeIn(player.level) : 0
        );

        form.send(player);
        form.onSubmit((p, response) -> handleResponse((Player) p, response));
    }

    private static void handleResponse(Player player, CustomResponse response) {
        String levelName = response.getDropdownResponse(0).elementText();
        boolean physicsEnabled = response.getToggleResponse(1);
        String blockRange = response.getStepSliderResponse(2).elementText();

        if (physicsEnabled) {
            LevelManager.enablePhysicsIn(
                levelName, 
                blockRange.equals("All world") ? -1 : Integer.parseInt(blockRange)
            );
        } else {
            LevelManager.disablePhysicsIn(levelName);
        }
    }
    
}
