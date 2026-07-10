package org.brlnsreb.tasks;

import org.brlnsreb.utils.abstraction.MenuAbstract;

import org.powernukkitx.plugin.annotation.ScheduleTask;
import org.powernukkitx.scheduler.Task;

@ScheduleTask(delay = 5 * 60 * 20, period = 5 * 60 * 20)
public class CheckDeadFormsTask extends Task {

    @Override
    public void onRun(int currentTick) {
        MenuAbstract.checkDeadForms(); 
    }

}