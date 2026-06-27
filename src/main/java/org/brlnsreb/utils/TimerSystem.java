package org.brlnsreb.utils;

import org.brlnsreb.BrlnsReb;

import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;

public class TimerSystem {
    
    private int secondsRemaining;
    private Task task;
    private Runnable onTick;
    
    public TimerSystem(int duration) {
        this.secondsRemaining = duration;
    }

    public void startCountdown(int seconds, Runnable onComplete, Runnable onTick) {
        this.secondsRemaining = seconds;
        this.onTick = onTick;
        start(onComplete);
    }
    
    public void startGame(int seconds, Runnable onComplete) {
        this.secondsRemaining = seconds;
        start(onComplete);
    }

    private void start(Runnable onComplete) {
        if (task != null) { task.cancel(); }

        if (onTick != null) {
            onTick.run();
        }

        task = new Task() {
            @Override
            public void onRun(int currentTick) {
                secondsRemaining--;
                
                if (secondsRemaining <= 0) {
                    cancel();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    if (onTick != null) {
                        onTick.run();
                    }
                }
            }
        };
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