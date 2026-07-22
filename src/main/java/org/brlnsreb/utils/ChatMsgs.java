package org.brlnsreb.utils;

public class ChatMsgs {

    public static final String BROKENLENS = "&l&eBroken&6Lens§r";
    public static final String BROKENLENS_GAMES = "&l&eBroken&6Lens §9Games";
    public static final String BROKENLENS_PFX = "&l&eBroken&6Lens§r §d";

    public static final String SUCCESS_PFX = "§l§aSUCCESS§r §a";
    public static final String ERROR_PFX = "§l§cERROR§r §c";
    public static final String INFO_PFX = "§l§eINFO§r §a";

    public static final String SPEC_PFX = "§l§fSPEC§r §7";

    public static final String BAR = "§3§o》§r§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3《";
    private static final int BAR_CHARACTERS = 40;

    public enum Alignment { 
        LEFT, 
        CENTER 
    };

    public static String buildString(Alignment alignment, String... lines) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(BAR);
        strBuilder.append("§2-§r\n");

        int i, spaces;
        for (String line : lines) {
            strBuilder.append("§2-§r");

            if (alignment == Alignment.CENTER) {
                spaces = (int) (BAR_CHARACTERS - line.length() * 1.1 - 1) / 2;
                for (i = 0; i < spaces; i++) strBuilder.append("§l §r");
            } else if (alignment == Alignment.LEFT) {
                strBuilder.append("§l §r");
            }

            strBuilder.append(line);
            strBuilder.append("§r\n");
        }

        strBuilder.append("§2-§r\n");
        strBuilder.append(BAR);
        return strBuilder.toString();
    }

}
