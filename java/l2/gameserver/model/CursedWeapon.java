package l2.gameserver.model;

import l2.commons.util.Rnd;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.Earthquake;
import l2.gameserver.network.l2.s2c.ExRedSky;
import l2.gameserver.network.l2.s2c.SkillList;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collection;

public class CursedWeapon
{
	private final String _name;
	private final int _itemId;
	private final int _skillMaxLevel;
	private final int _skillId;
	private String _transformationName;
	private int _dropRate;
	private int _disapearChance;
	private int _durationMin;
	private int _durationMax;
	private int _durationLost;
	private int _transformationId;
	private int _transformationTemplateId;
	private int _stageKills;
	private int _nbKills;
	private int _playerKarma;
	private int _playerPkKills;
	private CursedWeaponState _state = CursedWeaponState.NONE;
	private Location _loc;
	private long _endTime;
	private long _owner;
	private ItemInstance _item;
	
	public CursedWeapon(int itemId, int skillId, String name)
	{
		_name = name;
		_itemId = itemId;
		_skillId = skillId;
		_skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId);
	}
	
	public void initWeapon()
	{
		zeroOwner();
		setState(CursedWeaponState.NONE);
		_endTime = 0;
		_item = null;
		_nbKills = 0;
	}
	
	public void create(NpcInstance attackable, Player killer)
	{
		_item = ItemFunctions.createItem(_itemId);
		if(_item != null)
		{
			zeroOwner();
			setState(CursedWeaponState.DROPPED);
			if(_endTime == 0)
			{
				_endTime = System.currentTimeMillis() + (long) (getRndDuration() * 60000);
			}
			_item.dropToTheGround(attackable, Location.findPointToStay(attackable, 100));
			_loc = _item.getLoc();
			_item.setDropTime(0);
			ExRedSky redSky = new ExRedSky(10);
			Earthquake eq = new Earthquake(killer.getLoc(), 30, 12);
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				player.sendPacket(redSky, eq);
			}
		}
	}
	
	public boolean dropIt(NpcInstance attackable, Player killer, Player owner)
	{
		if(Rnd.chance(_disapearChance))
		{
			return false;
		}
		Player player = getOnlineOwner();
		if(player == null)
		{
			if(owner == null)
			{
				return false;
			}
			player = owner;
		}
		ItemInstance oldItem;
		if((oldItem = player.getInventory().removeItemByItemId(_itemId, 1)) == null)
		{
			return false;
		}
		player.setKarma(_playerKarma);
		player.setPkKills(_playerPkKills);
		player.setCursedWeaponEquippedId(0);
		player.setTransformation(0);
		player.setTransformationName(null);
		player.validateLocation(0);
		Skill skill = SkillTable.getInstance().getInfo(_skillId, player.getSkillLevel(_skillId));
		if(skill != null)
		{
			for(Skill.AddedSkill s : skill.getAddedSkills())
			{
				player.removeSkillById(s.id);
			}
		}
		player.removeSkillById(_skillId);
		player.abortAttack(true, false);
		zeroOwner();
		setState(CursedWeaponState.DROPPED);
		oldItem.dropToTheGround(player, Location.findPointToStay(player, 100));
		_loc = oldItem.getLoc();
		oldItem.setDropTime(0);
		_item = oldItem;
		player.sendPacket(new SystemMessage(298).addItemName(oldItem.getItemId()));
		player.broadcastUserInfo(true);
		player.broadcastPacket(new Earthquake(player.getLoc(), 30, 12));
		return true;
	}
	
	private void giveSkill(Player player)
	{
		for(Skill s : getSkills(player))
		{
			player.addSkill(s, false);
			player._transformationSkills.put(s.getId(), s);
		}
		player.sendPacket(new SkillList(player));
	}
	
	private Collection<Skill> getSkills(Player player)
	{
		int level = 1 + _nbKills / _stageKills;
		if(level > _skillMaxLevel)
		{
			level = _skillMaxLevel;
		}
		Skill skill = SkillTable.getInstance().getInfo(_skillId, level);
		ArrayList<Skill> ret = new ArrayList<>();
		ret.add(skill);
		for(Skill.AddedSkill s : skill.getAddedSkills())
		{
			ret.add(SkillTable.getInstance().getInfo(s.id, s.level));
		}
		return ret;
	}
	
	public boolean reActivate()
	{
		if(getTimeLeft() <= 0)
		{
			if(getPlayerId() != 0)
			{
				setState(CursedWeaponState.ACTIVATED);
			}
			return false;
		}
		if(getPlayerId() == 0)
		{
			if(_loc == null || (_item = ItemFunctions.createItem(_itemId)) == null)
			{
				return false;
			}
			_item.dropMe(null, _loc);
			_item.setDropTime(0);
			setState(CursedWeaponState.DROPPED);
		}
		else
		{
			setState(CursedWeaponState.ACTIVATED);
		}
		return true;
	}
	
	public void activate(Player player, ItemInstance item)
	{
		if(isDropped() || getPlayerId() != player.getObjectId())
		{
			_playerKarma = player.getKarma();
			_playerPkKills = player.getPkKills();
		}
		setPlayer(player);
		setState(CursedWeaponState.ACTIVATED);
		player.leaveParty();
		if(player.isMounted())
		{
			player.setMount(0, 0, 0);
		}
		_item = item;
		player.getInventory().setPaperdollItem(8, null);
		player.getInventory().setPaperdollItem(7, null);
		player.getInventory().setPaperdollItem(7, _item);
		player.sendPacket(new SystemMessage(49).addItemName(_item.getItemId()));
		player.setTransformation(0);
		player.setCursedWeaponEquippedId(_itemId);
		player.setTransformation(_transformationId);
		player.setTransformationName(_transformationName);
		player.setTransformationTemplate(_transformationTemplateId);
		player.setKarma(9999999);
		player.setPkKills(_nbKills);
		if(_endTime == 0)
		{
			_endTime = System.currentTimeMillis() + (long) (getRndDuration() * 60000);
		}
		giveSkill(player);
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}
	
	public void increaseKills()
	{
		Player player = getOnlineOwner();
		if(player == null)
		{
			return;
		}
		++_nbKills;
		player.setPkKills(_nbKills);
		player.updateStats();
		if(_nbKills % _stageKills == 0 && _nbKills <= _stageKills * (_skillMaxLevel - 1))
		{
			giveSkill(player);
		}
		_endTime -= (long) (_durationLost * 60000);
	}
	
	public void setDisapearChance(int disapearChance)
	{
		_disapearChance = disapearChance;
	}
	
	public void setDurationMin(int duration)
	{
		_durationMin = duration;
	}
	
	public void setDurationMax(int duration)
	{
		_durationMax = duration;
	}
	
	public void setDurationLost(int durationLost)
	{
		_durationLost = durationLost;
	}
	
	public int getTransformationId()
	{
		return _transformationId;
	}
	
	public void setTransformationId(int transformationId)
	{
		_transformationId = transformationId;
	}
	
	public void setTransformationTemplateId(int transformationTemplateId)
	{
		_transformationTemplateId = transformationTemplateId;
	}
	
	public void setTransformationName(String name)
	{
		_transformationName = name;
	}
	
	private void zeroOwner()
	{
		_owner = 0;
		_playerKarma = 0;
		_playerPkKills = 0;
	}
	
	public CursedWeaponState getState()
	{
		return _state;
	}
	
	public void setState(CursedWeaponState state)
	{
		_state = state;
	}
	
	public boolean isActivated()
	{
		return getState() == CursedWeaponState.ACTIVATED;
	}
	
	public boolean isDropped()
	{
		return getState() == CursedWeaponState.DROPPED;
	}
	
	public long getEndTime()
	{
		return _endTime;
	}
	
	public void setEndTime(long endTime)
	{
		_endTime = endTime;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public ItemInstance getItem()
	{
		return _item;
	}
	
	public void setItem(ItemInstance item)
	{
		_item = item;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getDropRate()
	{
		return _dropRate;
	}
	
	public void setDropRate(int dropRate)
	{
		_dropRate = dropRate;
	}
	
	public int getPlayerId()
	{
		return _owner == 0 ? 0 : GameObjectsStorage.getStoredObjectId(_owner);
	}
	
	public void setPlayerId(int playerId)
	{
		_owner = playerId == 0 ? 0 : GameObjectsStorage.objIdNoStore(playerId);
	}
	
	public Player getPlayer()
	{
		return _owner == 0 ? null : GameObjectsStorage.getAsPlayer(_owner);
	}
	
	public void setPlayer(Player player)
	{
		if(player != null)
		{
			_owner = player.getStoredId();
		}
		else if(_owner != 0)
		{
			setPlayerId(getPlayerId());
		}
	}
	
	public int getPlayerKarma()
	{
		return _playerKarma;
	}
	
	public void setPlayerKarma(int playerKarma)
	{
		_playerKarma = playerKarma;
	}
	
	public int getPlayerPkKills()
	{
		return _playerPkKills;
	}
	
	public void setPlayerPkKills(int playerPkKills)
	{
		_playerPkKills = playerPkKills;
	}
	
	public int getNbKills()
	{
		return _nbKills;
	}
	
	public void setNbKills(int nbKills)
	{
		_nbKills = nbKills;
	}
	
	public int getStageKills()
	{
		return _stageKills;
	}
	
	public void setStageKills(int stageKills)
	{
		_stageKills = stageKills;
	}
	
	public Location getLoc()
	{
		return _loc;
	}
	
	public void setLoc(Location loc)
	{
		_loc = loc;
	}
	
	public int getRndDuration()
	{
		if(_durationMin > _durationMax)
		{
			_durationMax = 2 * _durationMin;
		}
		return Rnd.get(_durationMin, _durationMax);
	}
	
	public boolean isActive()
	{
		return isActivated() || isDropped();
	}
	
	public int getLevel()
	{
		return Math.min(1 + _nbKills / _stageKills, _skillMaxLevel);
	}
	
	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}
	
	public Location getWorldPosition()
	{
		if(isActivated())
		{
			Player player = getOnlineOwner();
			if(player != null)
			{
				return player.getLoc();
			}
		}
		else if(isDropped() && _item != null)
		{
			return _item.getLoc();
		}
		return null;
	}
	
	public Player getOnlineOwner()
	{
		Player player = getPlayer();
		return player != null && player.isOnline() ? player : null;
	}
	
	public boolean isOwned()
	{
		return _owner != 0;
	}
	
	public enum CursedWeaponState
	{
		NONE,
		ACTIVATED,
		DROPPED;
	}
}