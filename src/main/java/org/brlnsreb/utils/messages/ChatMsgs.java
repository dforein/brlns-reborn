package org.brlnsreb.utils.messages;

public class ChatMsgs {

    public static final String BROKENLENS = "§l§eBroken§6Lens§r";
    public static final String BROKENLENS_GAMES = "§l§eBroken§6Lens §9Games";
    public static final String BROKENLENS_REBORN = "§l§eBroken§6Lens §9Reborn";
    public static final String BROKENLENS_PFX = "§l§eBroken§6Lens§r §d";

    public static final String SUCCESS_PFX = "§l§aSUCCESS§r §a";
    public static final String ERROR_PFX = "§l§cERROR§r §c";
    public static final String INFO_PFX = "§l§eINFO§r §a";

    public static final String SPEC_PFX = "§l§fSPEC§r §7";

    public static final String BAR = "§3»§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3«";

    //average pixels (considering only mojangles)
    private static final double BAR_PIXELS = 540.0;
    private static final double SPACE_PIXELS = 12.5;
    private static final double CHAR_PIXELS = 15.5;
    private static final double CHAR_BOLD_PIXELS = 18.5;
    private static final double CHAR_SHORT = 3.0;     //e.g. '.', '!', ':', etc

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
                    double linePxs = 0.0;
                    boolean bold = false;
                    
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);

                        if (c == '§' && line.length() > i + 1) {     //if the first condition is true, I can evaluate directly the next char
                            char code = line.charAt(i + 1);
                            switch (code) {
                                case 'l' -> bold = true;
                                case 'r' -> bold = false;
                            }
                            i++;
                        } else {
                            linePxs += switch(c) {
                                case '.', ':', '!', '|', ',' -> CHAR_SHORT;
                                default -> bold ? CHAR_BOLD_PIXELS : CHAR_PIXELS;
                            };                            
                        }
                    }

                    double spacesPxs = (BAR_PIXELS - linePxs) / 2;
                    int spaces = (int) Math.round(spacesPxs / SPACE_PIXELS);

                    if (spaces > 0) strBuilder.append("§r");
                    for (int i = 0; i < spaces; i++) strBuilder.append(" ");
                }

                case LEFT -> strBuilder.append("§l §r");
            }

            strBuilder.append(line);
            strBuilder.append("§r\n");
        }

        return strBuilder.toString();
    }

}
