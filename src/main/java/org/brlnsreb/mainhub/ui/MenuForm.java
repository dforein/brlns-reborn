package org.brlnsreb.mainhub.ui;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.database.FriendsManager;
import org.brlnsreb.utils.abstraction.FormAbstract;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.form.window.SimpleForm;

public class MenuForm extends FormAbstract {

    private static final String PATH = "lobby.items.menu.";

    public static void openForm(CustomPlayer player) {
        if (!checkCooldown(player)) return;

        SimpleForm form = new SimpleForm(getStr("title"));
        form.addButton("Manage Friends", p -> {
            CustomPlayer plr = (CustomPlayer) p;
            if (!plr.data.isLogged()) {
                plr.sendMessage(ChatMsgs.ERROR_PFX + "You are not logged in!");
                return;
            }
            manageFriends(plr);
        });
        
        form.send(player);
    }

    private static void manageFriends(CustomPlayer player) {
        SimpleForm form = new SimpleForm("Manage Friends");
        form.addButton("Add New Friend", p -> addNewFriend((CustomPlayer) p));
        //form.addButton("Search Friends", p -> searchFriends((CustomPlayer) p));
        form.addButton("Friend List", p -> friendList((CustomPlayer) p));
        form.addButton("Friend Invites", p -> friendInvites((CustomPlayer) p));
        
        form.send(player);
    }

    private static void addNewFriend(CustomPlayer player) {
        CustomForm form = new CustomForm("Add New Friend");
        form.addInput("Insert the name of the player you want to add:");

        form.send(player);
        form.onSubmit((p, response) -> {
            String senderName = ((CustomPlayer) p).data.name;
            String receiverName = response.getInputResponse(0);
            FriendsManager.sendRequest(senderName, receiverName).thenAccept(outcome -> {
                FriendsManager.sendRequestMessages(outcome, senderName, receiverName);
            });
        });
    }

    private static void friendList(CustomPlayer player) {
        //SimpleForm form = new SimpleForm("Friend List");
        //form.content("Friend List / Page 1");
        player.sendMessage(ChatMsgs.INFO_PFX + "Feature not implemented yet!");
    }

    private static void friendInvites(CustomPlayer player) {
        //SimpleForm form = new SimpleForm("Friend Invites");
        player.sendMessage(ChatMsgs.INFO_PFX + "Feature not implemented yet!");
    }

    private static String getStr(String path) {
        return YamlUtil.getStr(PATH + path, Configs.getGlobalConfig());
    }
    
}
