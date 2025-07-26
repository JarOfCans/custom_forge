package forge.adventure.character;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import forge.Forge;
import forge.adventure.data.ShopData;
import forge.adventure.pointofintrest.PointOfInterestChanges;
import forge.adventure.scene.RewardScene;
import forge.adventure.stage.MapStage;
import forge.adventure.util.Reward;


/**
 * Map actor that will open the Shop on collision
 */
public class ShopActor extends MapActor {
    private final MapStage stage;
    private ShopData shopData;
    List<Reward> rewardData;

    public ShopActor(MapStage stage, int id, ArrayList<Reward> ret, ShopData data) {
        super(id);
        this.stage = stage;
        this.shopData = data;
        this.rewardData = ret;
    }

    public float getPriceModifier() {
        PointOfInterestChanges changes = stage.getChanges();
        float townPricemodifier = changes == null ? 1f : changes.getTownPriceModifier();
        float shopPriceModifier = changes == null ? 1f : changes.getShopPriceModifier(objectId);
        return shopPriceModifier * townPricemodifier;
    }

    public MapStage getMapStage() {
        return stage;
    }

    @Override
    public void onPlayerCollide() {
        stage.getPlayerSprite().stop();
        RewardScene.instance().loadRewards(rewardData, RewardScene.Type.Shop, this);
        Forge.switchScene(RewardScene.instance());
    }


    public boolean isUnlimited() {
        return shopData.unlimited;
    }

    @Override
    public String getName() {
        return shopData.name;
    }

    public String getDescription() {
        return shopData.description;
    }

    public int getRestockPrice() {
        return shopData.restockPrice;
    }

    public boolean canRestock() {
        return getRestockPrice() > 0;
    }

    public ShopData getShopData() {
        return shopData;
    }

    public void setRewardData(List<Reward> ret) {
        rewardData = ret;
    }

    public List<Reward> getRewardData() {
        return rewardData;
    }
}
