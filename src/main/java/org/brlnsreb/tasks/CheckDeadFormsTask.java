package org.brlnsreb.tasks;

import org.brlnsreb.utils.abstraction.MenuAbstract;

import cn.nukkit.plugin.annotation.ScheduleTask;
import cn.nukkit.scheduler.Task;

@ScheduleTask(delay = 5 * 60 * 20, period = 5 * 60 * 20)
public class CheckDeadFormsTask extends Task {

    @Override
    public void onRun(int currentTick) {
        MenuAbstract.checkDeadForms(); 
    }

}