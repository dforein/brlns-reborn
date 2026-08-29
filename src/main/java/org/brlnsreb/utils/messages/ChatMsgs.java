package org.brlnsreb.utils.messages;

public class ChatMsgs {

    public static final String BROKENLENS = "§l§eBroken§6Lens§r";
    public static final String BROKENLENS_GAMES = "§l§eBroken§6Lens §9Games";
    public static final String BROKENLENS_PFX = "§l§eBroken§6Lens§r §d";

    public static final String SUCCESS_PFX = "§l§aSUCCESS§r §a";
    public static final String ERROR_PFX = "§l§cERROR§r §c";
    public static final String INFO_PFX = "§l§eINFO§r §a";

    public static final String SPEC_PFX = "§l§fSPEC§r §7";

    public static final String BAR = "§3»§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3«";

    //average pixels (considering both noto sans and mojangles)
    private static final double BAR_PIXELS = 566.0;
    private static final double SPACE_PIXELS = 13.0;
    private static final double CHAR_PIXELS = 14.0;
    private static final double CHAR_BOLD_PIXELS = 16.6;

    public enum Alignment { 
        LEFT, 
        CENTER 
    };

    public static String buildBlockContent(Alignment alignment, String... lines) {
        StringBuilder strBuilder = new StringBuilder();

        for (String line : lines) {
            strBuilder.append("§2-§r");

            switch (alignment) {
                case CENTER -> {
                    int i;

                    double linePxs = 0.0;
                    boolean bold = false;
                    for (i = 0; i < line.length(); i++) {
                        if (line.charAt(i) == '§' && line.length() > i + 1) {     //if the first condition is true, i can evaluate directly the next char
                            char code = line.charAt(i + 1);
                            switch (code) {
                                case 'l' -> bold = true;
                                case 'r' -> bold = false;
                            }
                            i++;
                        } else {
                            linePxs += bold ? CHAR_BOLD_PIXELS : CHAR_PIXELS;
                        }
                    }

                    double spacesPxs = (BAR_PIXELS - linePxs) / 2;
                    int spaces = (int) Math.round(spacesPxs / SPACE_PIXELS);

                    if (spaces > 0) strBuilder.append("§l");
                    for (i = 0; i < spaces; i++) strBuilder.append(" ");
                    if (spaces > 0) strBuilder.append("§r");
                }

                case LEFT -> strBuilder.append("§l §r");
            }

            strBuilder.append(line);
            strBuilder.append("§r\n");
        }

        return strBuilder.toString();
    }

}
