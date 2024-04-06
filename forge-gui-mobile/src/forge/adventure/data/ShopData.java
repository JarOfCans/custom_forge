package forge.adventure.data;

import java.util.List;

/**
 * Data class that will be used to read Json configuration files
 * SettingData
 * contains data for a Shop on the map
 */
public class ShopData {

    public String name;
    public String description;
    public int restockPrice;
    public String spriteAtlas;
    public String sprite;
    public boolean unlimited;
    public List<RewardData> rewards;
    public String overlaySprite = "";





}
