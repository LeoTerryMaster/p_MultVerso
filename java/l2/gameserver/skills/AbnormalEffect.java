package l2.gameserver.skills;

import java.util.NoSuchElementException;

public enum AbnormalEffect
{
	NULL("null", 0),
	BLEEDING("bleeding", 1),
	POISON("poison", 2),
	REDCIRCLE("redcircle", 4),
	ICE("ice", 8),
	AFFRAID("affraid", 16),
	CONFUSED("confused", 32),
	STUN("stun", 64),
	SLEEP("sleep", 128),
	MUTED("muted", 256),
	ROOT("root", 512),
	HOLD_1("hold1", 1024),
	HOLD_2("hold2", 2048),
	UNKNOWN_13("unk13", 4096),
	BIG_HEAD("bighead", 8192),
	FLAME("flame", 16384),
	UNKNOWN_16("unk16", 32768),
	GROW("grow", 65536),
	FLOATING_ROOT("floatroot", 131072),
	DANCE_STUNNED("dancestun", 262144),
	FIREROOT_STUN("firerootstun", 524288),
	STEALTH("shadow", 1048576),
	IMPRISIONING_1("imprison1", 2097152),
	IMPRISIONING_2("imprison2", 4194304),
	MAGIC_CIRCLE("magiccircle", 8388608),
	ICE2("ice2", 16777216),
	EARTHQUAKE("earthquake", 33554432),
	UNKNOWN_27("unk27", 67108864),
	INVULNERABLE("invul1", 134217728),
	VITALITY("vitality", 268435456),
	REAL_TARGET("realtarget", 536870912),
	DEATH_MARK("deathmark", 1073741824),
	SOUL_SHOCK("soulshock", Integer.MIN_VALUE),
	S_INVULNERABLE("invul2", 1, true),
	S_AIR_STUN("redglow", 2, true),
	S_AIR_ROOT("redglow2", 4, true),
	S_BAGUETTE_SWORD("baguettesword", 8, true),
	S_YELLOW_AFFRO("yellowafro", 16, true),
	S_PINK_AFFRO("pinkafro", 32, true),
	S_BLACK_AFFRO("blackafro", 64, true),
	S_UNKNOWN8("sunk8", 128, true),
	S_STIGMA("stigma", 256, true),
	S_UNKNOWN10("sunk10", 512, true),
	FROZEN_PILLAR("frozenpillar", 1024, true),
	S_VESPER1("vesper1", 2048, true),
	S_VESPER2("vesper2", 4096, true),
	S_VESPER3("vesper3", 8192, true),
	S_SOA_RESP("soa_respawn", 16384, true),
	S_ARCANE_SHIELD("arcane_invul", 32768, true),
	S_UNKNOWN17("sunk17", 65536, true),
	S_UNKNOWN18("sunk18", 131072, true),
	S_UNKNOWN19("sunk19", 262144, true),
	S_NAVIT("nevitSystem", 524288, true),
	S_UNKNOWN21("sunk21", 1048576, true),
	S_LETTER("letter_collection_buff", 2097152, true),
	S_UNKNOWN23("sunk23", 4194304, true),
	S_UNKNOWN24("sunk24", 8388608, true),
	S_UNKNOWN25("sunk25", 16777216, true),
	S_UNKNOWN26("sunk26", 33554432, true),
	S_UNKNOWN27("sunk27", 67108864, true),
	S_UNKNOWN28("sunk28", 134217728, true),
	S_UNKNOWN29("sunk29", 268435456, true),
	S_UNKNOWN30("sunk30", 536870912, true),
	S_UNKNOWN31("sunk31", 1073741824, true),
	S_UNKNOWN32("sunk32", Integer.MIN_VALUE, true),
	E_AFRO_1("afrobaguette1", 1, false, true),
	E_AFRO_2("afrobaguette2", 2, false, true),
	E_AFRO_3("afrobaguette3", 4, false, true),
	E_EVASWRATH("evaswrath", 8, false, true),
	E_HEADPHONE("headphone", 16, false, true),
	E_VESPER_1("vesper1", 32, false, true),
	E_VESPER_2("vesper2", 64, false, true),
	E_VESPER_3("vesper3", 128, false, true);
	
	private final int _mask;
	private final String _name;
	private final boolean _special;
	private final boolean _event;
	
	AbnormalEffect(String name, int mask)
	{
		_name = name;
		_mask = mask;
		_special = false;
		_event = false;
	}
	
	AbnormalEffect(String name, int mask, boolean special)
	{
		_name = name;
		_mask = mask;
		_special = special;
		_event = false;
	}
	
	AbnormalEffect(String name, int mask, boolean special, boolean event)
	{
		_name = name;
		_mask = mask;
		_special = special;
		_event = event;
	}
	
	public static AbnormalEffect getByName(String name)
	{
		for(AbnormalEffect eff : AbnormalEffect.values())
		{
			if(!eff.getName().equals(name))
				continue;
			return eff;
		}
		throw new NoSuchElementException("AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
	}
	
	public final int getMask()
	{
		return _mask;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final boolean isSpecial()
	{
		return _special;
	}
	
	public final boolean isEvent()
	{
		return _event;
	}
}