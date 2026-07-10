package org.brlnsreb.utils;

import org.brlnsreb.BrlnsReb;

import org.powernukkitx.Server;
import org.powernukkitx.scheduler.Task;

public class TimerSystem {
    
    private int secondsRemaining;
    private Task task;
    private Runnable onTick;
    
    public TimerSystem(int duration) {
        this.secondsRemaining = duration;
    }

    public void start(int seconds, Runnable onTick, Runnable onComplete) {
        this.secondsRemaining = seconds;
        this.onTick = onTick;
        start(onComplete);
    }
    
    public void start(int seconds, Runnable onComplete) {
        this.secondsRemaining = seconds;
        this.onTick = null;
        start(onComplete);
    }

    private void start(Runnable onComplete) {
        if (task != null) task.cancel();

        task = new Task() {
            @Override
            public void onRun(int currentTick) {
                secondsRemaining--;
                if (onTick != null) onTick.run();
                
                if (secondsRemaining <= 0) {
                    cancel();
                    if (onComplete != null) onComplete.run();
                }
            }
        };

        if (onTick != null) onTick.run();   //first run (0 seconds)
        Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(BrlnsReb.getInstance(), task, 20, 20);
    }
    
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
    
    public int getSecondsRemaining() {
        return secondsRemaining;
    }
    
    public String getFormattedTime() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}