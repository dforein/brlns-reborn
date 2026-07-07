package org.brlnsreb.utils.abstraction;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.window.Form;

public abstract class MenuAbstract {

    private static final Map<Integer, Form<?>> id2FormMap = new ConcurrentHashMap<>();
    private static final Map<Integer, UUID> id2PlayerUUIDMap = new ConcurrentHashMap<>();
    protected static final Map<UUID, Long> openingCooldown = new ConcurrentHashMap<>();
    protected static int id = 0;

    protected static boolean checkCooldown(Player player) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (openingCooldown.containsKey(uuid)
            && now - openingCooldown.get(uuid) < 500) {
            return true;
        }

        openingCooldown.put(uuid, now);
        return false;
    }

    protected static void sendForm(Player player, Form<?> form, String type) {
        id2FormMap.put(++id, form);
        id2PlayerUUIDMap.put(id, player.getUniqueId());

        form.putMeta("type", "games");
        form.send(player, id);
    }

    public static void removeForm(int id) {
        id2FormMap.remove(id);
    }

    public static void checkDeadForms() {
        Server server = Server.getInstance();
        Iterator<Integer> iterator = id2PlayerUUIDMap.keySet().iterator();
    
        while (iterator.hasNext()) {
            Integer formId = iterator.next();
            
            if (server.getPlayer(id2PlayerUUIDMap.get(formId)).isEmpty()) {
                id2FormMap.remove(formId);
                iterator.remove();
            }
        }
    }

}
