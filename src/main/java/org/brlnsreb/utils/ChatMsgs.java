package org.brlnsreb.utils;

public class ChatMsgs {

    public static final String BROKENLENS = "&l&eBroken&4Lens§r";
    public static final String BROKENLENS_PFX = "&l&eBroken&4Lens§r §d";

    public static final String SUCCESS_PFX = "§l§aSUCCESS§r §a";
    public static final String ERROR_PFX = "§l§cERROR§r §c";
    public static final String INFO_PFX = "§l§eINFO§r §a";

    public static final String SPEC_PFX = "§l§fSPEC§r §7";

    public static final String BAR = "§3§o》§r§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3§o《";


    public enum Alignment { 
        RIGHT, 
        CENTER 
    };

    public static String buildString(Alignment alignment, String... lines) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(BAR);

        for (String line : lines) {
            strBuilder.append("§2—§r");
            line.length()
        }
    }

}
