package org.brlnsreb.utils.abstraction;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.form.window.Form;

public abstract class MenuAbstract {

    private static final Map<Integer, Form<?>> id2FormMap = new ConcurrentHashMap<>();
    private static final Map<Integer, UUID> id2PlayerUUIDMap = new ConcurrentHashMap<>();
    private static final BitSet idSet = new BitSet();
    protected static final Map<UUID, Long> openingCooldown = new ConcurrentHashMap<>();

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

    protected static int sendForm(Player player, Form<?> form) {
        int id = idSet.nextClearBit(0);
        idSet.set(id);

        id2FormMap.put(id, form);
        id2PlayerUUIDMap.put(id, player.getUniqueId());

        form.send(player, id);
        return id;
    }

    public static void removeForm(int id) {
        id2FormMap.remove(id);
        idSet.clear(id);
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
