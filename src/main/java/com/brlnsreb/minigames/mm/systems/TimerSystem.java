package com.brlnsreb.minigames.mm.systems;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.scheduler.Task;

public class TimerSystem {
    
    private final MinigameCore plugin;
    private int secondsRemaining;
    private boolean firstSecond;
    private Task task;
    private Runnable onTick;
    
    public TimerSystem(MinigameCore plugin, int duration) {
        this.plugin = plugin;
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

        firstSecond = true;

        task = new Task() {
            @Override
            public void onRun(int currentTick) {
                if (firstSecond) {
                    firstSecond = false;
                } else {
                    secondsRemaining--;
                }
                
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
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, task, 20);
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