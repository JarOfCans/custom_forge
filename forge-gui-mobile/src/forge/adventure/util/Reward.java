package forge.adventure.util;

import forge.adventure.data.ItemData;
import forge.card.CardRarity;
import forge.deck.Deck;
import forge.item.PaperCard;

/**
 * Reward class that may contain gold,cards or items
 */
public class Reward implements Comparable<Reward> {
    public enum Type {
        Card,
        Gold,
        Item,
        Life,
        Shards,
        CardPack
    }

    Type type;
    PaperCard card;
    ItemData item;
    Deck deck;
    boolean isNoSell, isAutoSell;
    private final int count;

    public Reward(ItemData item) {
        type = Type.Item;
        this.item = item;
        count = 1;
    }

    public Reward(int count) {
        type = Type.Gold;
        this.count = count;
    }

    public Reward(PaperCard card) {
        this(card, false);
    }

    public Reward(PaperCard card, boolean isNoSell) {
        type = Type.Card;
        this.card = card;
        count = 0;
        this.isNoSell = isNoSell;
        if(isNoSell)
            this.card = card.getNoSellVersion();
    }

    public Reward(Type type, int count) {
        this.type = type;
        this.count = count;
    }

    public Reward(Deck deck) {
        this(deck, false);
    }

    public Reward(Deck deck, boolean isNoSell) {
        type = Type.CardPack;
        this.deck = deck;
        count = 0;
        this.isNoSell = isNoSell;
        if(isNoSell)
            deck.getTags().add("noSell");
        //Could go through the deck and replace everything in it with the noSellValue version but the tag should
        //handle that later.
    }

    public PaperCard getCard() { return card;  }
    public ItemData getItem()  { return item;  }
    public Deck getDeck()      { return deck;  }
    public Type getType()      { return type;  }
    public int getCount()      { return count; }
    public boolean isNoSell()      { return isNoSell; }
	@Override
	public int compareTo(Reward o) {
		//System.out.println(o.getCard().getName() + o.getNumValue() + " vs " + this.getCard().getName() + this.getNumValue());
		return o.getNumValue() - this.getNumValue();
	}
	public float getDelay() {
		switch (type) {
		case Card:
			switch (card.getRarity()) {
			case Common:
				return 0.6f;
			case Uncommon:
				return 0.85f;
			case Rare:
				return 1.15f;
			case MythicRare:
				return 1.5f;
			case Special:
				return 0.85f;
			case BasicLand:
				return 0.35f;
			default:
				return 0.5f;
			}
		case Gold:
			return 0.3f;
		case Shards:
			return 0.3f;
		case Life:
			return 2f;
		case CardPack:
		case Item:
			return 0.4f;
		}
		return 9;
	}
	
	private int getNumValue() {
		switch (type) {
		case Card:
			switch (card.getRarity()) {
			case Common:
				return 1;
			case Uncommon:
				return 2;
			case Rare:
				return 3;
			case MythicRare:
				return 4;
			case Special:
				return 8;
			case BasicLand:
				return 10;
			default:
				return 10;
			}
		case Gold:
			return 5;
		case Shards:
			return 6;
		case Life:
			return 7;
		case CardPack:
		case Item:
			return 9;
		}
		return 9;
	}

    public boolean isAutoSell() {
        return isAutoSell;
    }

    public void setAutoSell(boolean val) {
        isAutoSell = val;
    }
}
