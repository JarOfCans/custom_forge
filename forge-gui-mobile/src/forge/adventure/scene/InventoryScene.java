package forge.adventure.scene;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TextraButton;
import com.github.tommyettinger.textra.TextraLabel;
import forge.Forge;
import forge.adventure.data.ItemData;
import forge.adventure.stage.ConsoleCommandInterpreter;
import forge.adventure.stage.GameHUD;
import forge.adventure.stage.MapStage;
import forge.adventure.util.*;
import forge.deck.Deck;

import java.util.HashMap;
import java.util.Map;

public class InventoryScene extends UIScene {
    TextraButton leave;
    Button equipButton;
    TextraButton useButton;
    TextraLabel itemDescription;
    private final Table inventory;
    private final Array<Button> inventoryButtons = new Array<>();
    private final HashMap<String, Button> equipmentSlots = new HashMap<>();
    HashMap<Button, String> itemLocation = new HashMap<>();
    HashMap<Button, Deck> deckLocation = new HashMap<>();
    Button selected;
    Button deleteButton;
    Texture equipOverlay;
    Dialog useDialog, deleteDialog, openDialog;
    int columns = 0;

    public InventoryScene() {
        super(Forge.isLandscapeMode() ? "ui/inventory.json" : "ui/inventory_portrait.json");
        equipOverlay = Forge.getAssets().getTexture(Config.instance().getFile(Paths.ITEMS_EQUIP));
        ui.onButtonPress("return", this::done);
        leave = ui.findActor("return");
        ui.onButtonPress("delete", this::showConfirm);
        ui.onButtonPress("equip", this::equip);
        ui.onButtonPress("use", this::use);
        equipButton = ui.findActor("equip");
        useButton = ui.findActor("use");
        useButton.setDisabled(true);
        deleteButton = ui.findActor("delete");
        itemDescription = ui.findActor("item_description");
        itemDescription.setAlignment(Align.topLeft);
        itemDescription.setWrap(true);
        ScrollPane pane = new ScrollPane(itemDescription);
        pane.setBounds(itemDescription.getX(), itemDescription.getY(), itemDescription.getWidth() - 5, itemDescription.getHeight() - 8);
        ui.addActor(pane);

        Array<Actor> children = ui.getChildren();
        for (int i = 0, n = children.size; i < n; i++) {

            if (children.get(i).getName() != null && children.get(i).getName().startsWith("Equipment")) {
                String slotName = children.get(i).getName().split("_")[1];
                equipmentSlots.put(slotName, (Button) children.get(i));
                Actor slot = children.get(i);
                slot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Button button = ((Button) actor);
                        if (button.isChecked()) {
                            for (Button otherButton : equipmentSlots.values()) {
                                if (button != otherButton && otherButton.isChecked()) {
                                    otherButton.setChecked(false);
                                }
                            }
                            String item = Current.player().itemInSlot(slotName);
                            if (item != null && !item.isEmpty()) {
                                Button changeButton = null;
                                for (Button invButton : inventoryButtons) {
                                    if (itemLocation.get(invButton) != null && itemLocation.get(invButton).equals(item)) {
                                        changeButton = invButton;
                                        break;
                                    }
                                }
                                if (changeButton != null)
                                    changeButton.setChecked(true);
                            } else {
                                setSelected(null);
                            }
                        }

                    }
                });
            }
        }
        inventory = new Table(Controls.getSkin());
        ScrollPane scrollPane = ui.findActor("inventory");
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setActor(inventory);
        columns = (int) (scrollPane.getWidth() / createInventorySlot().getWidth());
        columns -= 1;
        if (columns <= 0) columns = 1;
        scrollPane.setActor(inventory);
    }

    private void showConfirm() {
        if (deleteDialog == null) {
            deleteDialog = createGenericDialog("", Forge.getLocalizer().getMessage("lblDelete"),
                    Forge.getLocalizer().getMessage("lblYes"),
                    Forge.getLocalizer().getMessage("lblNo"), () -> {
                        this.delete();
                        removeDialog();
                    }, this::removeDialog);
        }
        showDialog(deleteDialog);
    }

    private static InventoryScene object;

    public static InventoryScene instance() {
        if (object == null)
            object = new InventoryScene();
        return object;
    }


    public void done() {
        GameHUD.getInstance().getTouchpad().setVisible(false);
        Forge.switchToLast();
    }

    public void delete() {
        ItemData data = ItemData.getItem(itemLocation.get(selected));
        if (data != null) {
            Current.player().removeItem(data.name);
        }
        updateInventory();

    }

    public void equip() {
        if (selected == null) return;
        ItemData data = ItemData.getItem(itemLocation.get(selected));
        if (data == null) return;
        Current.player().equip(data);
        updateInventory();
    }

    @Override
    public void act(float delta) {
        stage.act(delta);
    }

    private void triggerUse() {
        if (selected == null) return;

        ItemData data = ItemData.getItem(itemLocation.get(selected));
        if (data == null) return;
        Current.player().addShards(-data.shardsNeeded);
        done();
        if (data.commandOnUse != null && !data.commandOnUse.isEmpty())
            ConsoleCommandInterpreter.getInstance().command(data.commandOnUse);
        if (data.dialogOnUse != null && data.dialogOnUse.text != null && !data.dialogOnUse.text.isEmpty()) {
            MapDialog dialog = new MapDialog(data.dialogOnUse, MapStage.getInstance(),0,null);
            MapStage.getInstance().showDialog();
            dialog.activate();
            ChangeListener listen = new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    AdventureQuestController.instance().showQuestDialogs(MapStage.getInstance());
                }
            };
            dialog.addDialogCompleteListener(listen);
        }
        AdventureQuestController.instance().updateItemUsed(data);
    }

    private void openBooster() {
        if (selected == null) return;

        Deck data = (deckLocation.get(selected));
        if (data == null) return;

        //done();
        setSelected(null);
        RewardScene.instance().loadRewards(data, RewardScene.Type.Loot, null, data.getTags().contains("noSell"));
        Forge.switchScene(RewardScene.instance());
        Current.player().getBoostersOwned().removeValue(data, true);
    }

    private void use() {
        if (itemLocation.containsKey(selected)) {
            ItemData data = ItemData.getItem(itemLocation.get(selected));
            if (data == null)
                return;
            if (useDialog == null) {
                useDialog = createGenericDialog("", null, Forge.getLocalizer().getMessage("lblYes"),
                        Forge.getLocalizer().getMessage("lblNo"), () -> {
                            this.triggerUse();
                            removeDialog();
                        }, this::removeDialog);
                useDialog.getContentTable().add(Controls.newTextraLabel("Use " + data.name + "?\n" + data.getDescription()));
            }
            showDialog(useDialog);
        }
        if (deckLocation.containsKey(selected)){
            Deck data = deckLocation.get(selected);
            if (data == null)
                return;
            this.openBooster();
        }
    }

    public void clearItemDescription() {
        itemDescription.setText("");
    }
    private void setSelected(Button actor) {
        selected = actor;
        if (actor == null) {
            clearItemDescription();
            deleteButton.setDisabled(true);
            equipButton.setDisabled(true);
            useButton.setDisabled(true);
            for (Button button : inventoryButtons) {
                button.setChecked(false);
            }
            return;
        }
        if (itemLocation.containsKey(actor)) {
            ItemData data = ItemData.getItem(itemLocation.get(actor));
            if (data == null) return;

            deleteButton.setDisabled(data.questItem);

            boolean isInPoi = MapStage.getInstance().isInMap();
            useButton.setDisabled(!(isInPoi && data.usableInPoi || !isInPoi && data.usableOnWorldMap));
            if (data.shardsNeeded == 0)
                useButton.setText("Use");
            else
                useButton.setText("Use " + data.shardsNeeded + "[+Shards]");
            useButton.layout();
            if (Current.player().getShards() < data.shardsNeeded)
                useButton.setDisabled(true);

            if (data.equipmentSlot == null || data.equipmentSlot.isEmpty() || data.isCracked) {
                equipButton.setDisabled(true);
            } else {
                equipButton.setDisabled(false);
                if (equipButton instanceof TextraButton) {
                    TextraButton button = (TextraButton) equipButton;
                    String item = Current.player().itemInSlot(data.equipmentSlot);
                    if (item != null && item.equals(data.name)) {
                        button.setText("Unequip");
                    } else {
                        button.setText("Equip");
                    }
                    button.layout();
                }
            }
            String status = data.isCracked ? " (" + Forge.getLocalizer().getMessage("lblCracked") + ")" : "";
            itemDescription.setText(data.name + status + "\n[%98]" + data.getDescription());
        }
        else if (deckLocation.containsKey(actor)){
            Deck data = (deckLocation.get(actor));
            if (data == null) return;

            deleteButton.setDisabled(true);
            useButton.setDisabled(false);
            useButton.setText("Open");
            useButton.layout();
            equipButton.setDisabled(true);

            itemDescription.setText(data.getName() + "\n[%98]" + (data.getComment() == null?"":data.getComment()+" - ") + data.getAllCardsInASinglePool().countAll() + " cards");
        }


        for (Button button : inventoryButtons) {
            if (actor != button && button.isChecked()) {
                button.setChecked(false);
            }
        }

        performTouch(scrollPaneOfActor(itemDescription)); //can use mouse wheel if available to scroll after selection
    }

    private void updateInventory() {
        clearSelectable();
        inventoryButtons.clear();
        inventory.clear();

        int itemSlotsUsed = 0;
        // Turn item strings into actual ItemData
        // THen sort but these by slot type and name

        Array<ItemData> items = new Array<>();
        for (int i = 0; i < Current.player().getItems().size; i++) {
            String name = Current.player().getItems().get(i);
            ItemData item = ItemData.getItem(name);
            if (item == null) {
                System.err.print("Can not find item name " + name + "\n");
                continue;
            }
            if (item.sprite() == null) {
                System.err.print("Can not find sprite name " + item.iconName + "\n");
                continue;
            }

            items.add(item);
        }
        items.sort((o1, o2) -> {
            if (o1.equipmentSlot == null && o2.equipmentSlot == null) {
                return o1.name.compareTo(o2.name);
            } else if (o1.equipmentSlot == null) {
                return 1;
            } else if (o2.equipmentSlot == null) {
                return -1;
            } else {
                int slotCompare = o1.equipmentSlot.compareTo(o2.equipmentSlot);
                if (slotCompare != 0) {
                    return slotCompare;
                }
                return o1.name.compareTo(o2.name);
            }
        });


        for (int i = 0; i < items.size; i++) {
            if (i % columns == 0)
                inventory.row();
            Button newActor = createInventorySlot();
            inventory.add(newActor).top().left().space(1);
            addToSelectable(new Selectable(newActor) {
                @Override
                public void onSelect(UIScene scene) {
                    setSelected(newActor);
                    super.onSelect(scene);
                }
            });
            inventoryButtons.add(newActor);

            ItemData item = items.get(i);
            Image img = new Image(item.sprite());
            img.setX((newActor.getWidth() - img.getWidth()) / 2);
            img.setY((newActor.getHeight() - img.getHeight()) / 2);
            newActor.addActor(img);
            itemLocation.put(newActor, item.name);
            if (Current.player().getEquippedItems().contains(item.name)) {
                Image overlay = new Image(equipOverlay);
                overlay.setX((newActor.getWidth() - img.getWidth()) / 2);
                overlay.setY((newActor.getHeight() - img.getHeight()) / 2);
                newActor.addActor(overlay);
            }
            newActor.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (((Button) actor).isChecked()) {
                        setSelected((Button) actor);
                    }
                }
            });
            itemSlotsUsed++;
        }
        Current.player().getBoostersOwned().sort();

        for (int i = 0; i < Current.player().getBoostersOwned().size; i++) {

            if ((i + itemSlotsUsed) % columns == 0)
                inventory.row();
            Button newActor = createInventorySlot();
            inventory.add(newActor).top().left().space(1);
            addToSelectable(new Selectable(newActor) {
                @Override
                public void onSelect(UIScene scene) {
                    setSelected(newActor);
                    super.onSelect(scene);
                }
            });
            inventoryButtons.add(newActor);
            Deck deck = Current.player().getBoostersOwned().get(i);
            if (deck == null | deck.isEmpty()) {
                System.err.print("Can not add null / empty booster " + Current.player().getBoostersOwned().get(i) + "\n");
                continue;
            }
            Sprite deckSprite = Config.instance().getItemSprite("Deck");

            Image img = new Image(deckSprite);
            img.setX((newActor.getWidth() - img.getWidth()) / 2);
            img.setY((newActor.getHeight() - img.getHeight()) / 2);
            newActor.addActor(img);
            deckLocation.put(newActor, Current.player().getBoostersOwned().get(i));
            newActor.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (((Button) actor).isChecked()) {
                        setSelected((Button) actor);
                    }
                }
            });
        }


        for (Map.Entry<String, Button> slot : equipmentSlots.entrySet()) {
            if (slot.getValue().getChildren().size >= 2)
                slot.getValue().removeActorAt(1, false);
            String equippedItem = Current.player().itemInSlot(slot.getKey());
            if (equippedItem == null || equippedItem.isEmpty())
                continue;
            ItemData item = ItemData.getItem(equippedItem);
            if (item != null) {
                Image img = new Image(item.sprite());
                img.setX((slot.getValue().getWidth() - img.getWidth()) / 2);
                img.setY((slot.getValue().getHeight() - img.getHeight()) / 2);
                slot.getValue().addActor(img);
            }
        }
    }

    @Override
    public void enter() {
        updateInventory();
        //inventory.add().expand();
        super.enter();
    }

    public Button createInventorySlot() {
        return new ImageButton(Controls.getSkin(), "item_frame");
    }
}
