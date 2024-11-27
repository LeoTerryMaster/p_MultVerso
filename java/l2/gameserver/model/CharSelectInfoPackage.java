package l2.gameserver.model;

import l2.gameserver.dao.ItemsDAO;
import l2.gameserver.model.items.ItemInstance;

import java.util.Collection;

public class CharSelectInfoPackage
{
	private final ItemInstance[] _paperdoll;
	private String _name;
	private int _objectId;
	private int _charId = 199546;
	private long _exp;
	private int _sp;
	private int _clanId;
	private int _race;
	private int _classId;
	private int _baseClassId;
	private int _deleteTimer;
	private long _lastAccess;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private int _sex;
	private int _level = 1;
	private int _karma;
	private int _pk;
	private int _pvp;
	private int _maxHp;
	private double _currentHp;
	private int _maxMp;
	private double _currentMp;
	private int _accesslevel;
	private int _x;
	private int _y;
	private int _z;
	private int _vitalityPoints = 20000;
	
	public CharSelectInfoPackage(int objectId, String name)
	{
		setObjectId(objectId);
		_name = name;
		Collection<ItemInstance> items = ItemsDAO.getInstance().loadItemsByOwnerIdAndLoc(objectId, ItemInstance.ItemLocation.PAPERDOLL);
		_paperdoll = new ItemInstance[17];
		for(ItemInstance item : items)
		{
			if(item.getEquipSlot() >= 17)
				continue;
			_paperdoll[item.getEquipSlot()] = item;
		}
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getCharId()
	{
		return _charId;
	}
	
	public void setCharId(int charId)
	{
		_charId = charId;
	}
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	public int getClassId()
	{
		return _classId;
	}
	
	public void setClassId(int classId)
	{
		_classId = classId;
	}
	
	public int getBaseClassId()
	{
		return _baseClassId;
	}
	
	public void setBaseClassId(int baseClassId)
	{
		_baseClassId = baseClassId;
	}
	
	public double getCurrentHp()
	{
		return _currentHp;
	}
	
	public void setCurrentHp(double currentHp)
	{
		_currentHp = currentHp;
	}
	
	public double getCurrentMp()
	{
		return _currentMp;
	}
	
	public void setCurrentMp(double currentMp)
	{
		_currentMp = currentMp;
	}
	
	public int getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	public void setDeleteTimer(int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	public void setLastAccess(long lastAccess)
	{
		_lastAccess = lastAccess;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long exp)
	{
		_exp = exp;
	}
	
	public int getFace()
	{
		return _face;
	}
	
	public void setFace(int face)
	{
		_face = face;
	}
	
	public int getHairColor()
	{
		return _hairColor;
	}
	
	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}
	
	public int getHairStyle()
	{
		return _hairStyle;
	}
	
	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}
	
	public int getPaperdollObjectId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null)
		{
			return item.getObjectId();
		}
		return 0;
	}
	
	public int getPaperdollAugmentationId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null && item.isAugmented())
		{
			return item.getVariationStat1() & 65535 | item.getVariationStat2() << 16;
		}
		return 0;
	}
	
	public int getPaperdollItemId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null)
		{
			return item.getItemId();
		}
		return 0;
	}
	
	public int getPaperdollEnchantEffect(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if(item != null)
		{
			return item.getEnchantLevel();
		}
		return 0;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public int getMaxHp()
	{
		return _maxHp;
	}
	
	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}
	
	public int getMaxMp()
	{
		return _maxMp;
	}
	
	public void setMaxMp(int maxMp)
	{
		_maxMp = maxMp;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public int getRace()
	{
		return _race;
	}
	
	public void setRace(int race)
	{
		_race = race;
	}
	
	public int getSex()
	{
		return _sex;
	}
	
	public void setSex(int sex)
	{
		_sex = sex;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int sp)
	{
		_sp = sp;
	}
	
	public int getKarma()
	{
		return _karma;
	}
	
	public void setKarma(int karma)
	{
		_karma = karma;
	}
	
	public int getAccessLevel()
	{
		return _accesslevel;
	}
	
	public void setAccessLevel(int accesslevel)
	{
		_accesslevel = accesslevel;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public void setX(int x)
	{
		_x = x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public void setY(int y)
	{
		_y = y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public void setZ(int z)
	{
		_z = z;
	}
	
	public int getPk()
	{
		return _pk;
	}
	
	public void setPk(int pk)
	{
		_pk = pk;
	}
	
	public int getPvP()
	{
		return _pvp;
	}
	
	public void setPvP(int pvp)
	{
		_pvp = pvp;
	}
	
	public int getVitalityPoints()
	{
		return _vitalityPoints;
	}
	
	public void setVitalityPoints(int points)
	{
		_vitalityPoints = points;
	}
}