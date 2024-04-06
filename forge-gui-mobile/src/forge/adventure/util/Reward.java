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
    boolean isNoSell;
    private final int count;

    public Reward(ItemData item) {
        type      = Type.Item;
        this.item = item;
        count     = 1;
    }
    public Reward(int count) {
        type       = Type.Gold;
        this.count = count;
    }
    public Reward(PaperCard card) {
        this(card,false);
    }
    public Reward(PaperCard card, boolean isNoSell) {
        type      = Type.Card;
        this.card = card;
        count     = 0;
        this.isNoSell = isNoSell;
    }
    public Reward(Type type, int count) {
        this.type  = type;
        this.count = count;
    }
    public Reward(Deck deck) {
        this(deck, false);
    }
    public Reward(Deck deck, boolean isNoSell) {
        type      = Type.CardPack;
        this.deck = deck;
        count     = 0;
        this.isNoSell = isNoSell;
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
}
