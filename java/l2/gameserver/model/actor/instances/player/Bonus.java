package l2.gameserver.model.actor.instances.player;

public class Bonus
{
	public static final float DEFAULT_RATE_XP = 1.0f;
	public static final float DEFAULT_RATE_SP = 1.0f;
	public static final float DEFAULT_QUEST_REWARD_RATE = 1.0f;
	public static final float DEFAULT_QUEST_DROP_RATE = 1.0f;
	public static final float DEFAULT_DROP_ADENA = 1.0f;
	public static final float DEFAULT_DROP_ITEMS = 1.0f;
	public static final float DEFAULT_DROP_RAID_ITEMS = 1.0f;
	public static final float DEFAULT_DROP_SPOIL = 1.0f;
	public static final float DEFAULT_ENCHANT_ITEM = 1.0f;
	private float rateXp = 1.0f;
	private float rateSp = 1.0f;
	private float questRewardRate = 1.0f;
	private float questDropRate = 1.0f;
	private float dropAdena = 1.0f;
	private float dropItems = 1.0f;
	private float dropRaidItems = 1.0f;
	private float dropSpoil = 1.0f;
	private float enchantItem = 1.0f;
	private long bonusExpire;
	
	public void reset()
	{
		setRateXp(1.0f);
		setRateSp(1.0f);
		setQuestRewardRate(1.0f);
		setQuestDropRate(1.0f);
		setDropAdena(1.0f);
		setDropItems(1.0f);
		setDropRaidItems(1.0f);
		setDropSpoil(1.0f);
		bonusExpire = 0;
	}
	
	public float getRateXp()
	{
		return rateXp;
	}
	
	public void setRateXp(float rateXp)
	{
		this.rateXp = rateXp;
	}
	
	public float getRateSp()
	{
		return rateSp;
	}
	
	public void setRateSp(float rateSp)
	{
		this.rateSp = rateSp;
	}
	
	public float getQuestRewardRate()
	{
		return questRewardRate;
	}
	
	public void setQuestRewardRate(float questRewardRate)
	{
		this.questRewardRate = questRewardRate;
	}
	
	public float getQuestDropRate()
	{
		return questDropRate;
	}
	
	public void setQuestDropRate(float questDropRate)
	{
		this.questDropRate = questDropRate;
	}
	
	public float getDropAdena()
	{
		return dropAdena;
	}
	
	public void setDropAdena(float dropAdena)
	{
		this.dropAdena = dropAdena;
	}
	
	public float getDropItems()
	{
		return dropItems;
	}
	
	public void setDropItems(float dropItems)
	{
		this.dropItems = dropItems;
	}
	
	public float getDropRaidItems()
	{
		return dropRaidItems;
	}
	
	public void setDropRaidItems(float dropRaidItems)
	{
		this.dropRaidItems = dropRaidItems;
	}
	
	public float getDropSpoil()
	{
		return dropSpoil;
	}
	
	public void setDropSpoil(float dropSpoil)
	{
		this.dropSpoil = dropSpoil;
	}
	
	public float getEnchantItemMul()
	{
		return enchantItem;
	}
	
	public void setEnchantItem(float enchantItem)
	{
		this.enchantItem = enchantItem;
	}
	
	public long getBonusExpire()
	{
		return bonusExpire;
	}
	
	public void setBonusExpire(long bonusExpire)
	{
		this.bonusExpire = bonusExpire;
	}
}