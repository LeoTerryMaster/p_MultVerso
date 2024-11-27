package l2.gameserver.network.l2.c2s;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Summon;
import l2.gameserver.model.instances.StaticObjectInstance;
import l2.gameserver.model.instances.residences.SiegeFlagInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.network.l2.s2c.PrivateStoreManageListBuy;
import l2.gameserver.network.l2.s2c.PrivateStoreManageListSell;
import l2.gameserver.network.l2.s2c.RecipeShopManageList;
import l2.gameserver.network.l2.s2c.SocialAction;
import l2.gameserver.tables.PetSkillsTable;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.TradeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestActionUse extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestActionUse.class);
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Action action = Action.find(_actionId);
		if(action == null)
		{
			_log.warn("unhandled action type " + _actionId + " by player " + activeChar.getName());
			activeChar.sendActionFailed();
			return;
		}
		boolean usePet;
		boolean bl = usePet = action.type == 1 || action.type == 2;
		if(!(usePet || !activeChar.isOutOfControl() && !activeChar.isActionsDisabled() || activeChar.isFakeDeath() && _actionId == 0))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getTransformation() != 0 && action.transform > 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(action.type == 3)
		{
			if(activeChar.isOutOfControl() || activeChar.getTransformation() != 0 || activeChar.isActionsDisabled() || activeChar.isSitting() || activeChar.getPrivateStoreType() != 0 || activeChar.isProcessingRequest())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.isFishing())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
				return;
			}
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), action.value));
			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600);
				activeChar.startParalyzed();
			}
			return;
		}
		GameObject target = activeChar.getTarget();
		Summon pet = activeChar.getPet();
		if(usePet)
		{
			if(pet == null || pet.isOutOfControl())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(pet.isDepressed())
			{
				activeChar.sendPacket(SystemMsg.YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
				return;
			}
		}
		if(action.type == 2)
		{
			if(action.id == 1000 && target != null && !target.isDoor())
			{
				activeChar.sendActionFailed();
				return;
			}
			if((action.id == 1039 || action.id == 1040) && (target.isDoor() || target instanceof SiegeFlagInstance))
			{
				activeChar.sendActionFailed();
				return;
			}
			UseSkill(action.value);
			return;
		}
		switch(action.id)
		{
			case 0:
			{
				if(activeChar.isMounted())
				{
					activeChar.sendActionFailed();
					break;
				}
				if(activeChar.isFakeDeath())
				{
					activeChar.breakFakeDeath();
					activeChar.updateEffectIcons();
					break;
				}
				if(!activeChar.isSitting())
				{
					if(target != null && target instanceof StaticObjectInstance && ((StaticObjectInstance) target).getType() == 1 && activeChar.getDistance3D(target) <= (double) target.getActingRange())
					{
						activeChar.sitDown((StaticObjectInstance) target);
						break;
					}
					activeChar.sitDown(null);
					break;
				}
				activeChar.standUp();
				break;
			}
			case 1:
			{
				if(activeChar.isRunning())
				{
					activeChar.setWalking();
					break;
				}
				activeChar.setRunning();
				break;
			}
			case 10:
			case 61:
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, _actionId == 61 ? 8 : 1))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreManageListSell(activeChar, _actionId == 61));
				break;
			}
			case 28:
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, 3))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreManageListBuy(activeChar));
				break;
			}
			case 37:
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, 5))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			}
			case 51:
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(0);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, 5))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			}
			case 96:
			{
				_log.info("96 Accessed");
				break;
			}
			case 97:
			{
				_log.info("97 Accessed");
				break;
			}
			case 15:
			case 21:
			{
				if(pet == null)
					break;
				pet.setFollowMode(!pet.isFollowMode());
				break;
			}
			case 16:
			case 22:
			{
				if(target == null || !target.isCreature() || pet == target || pet.isDead())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isOlyParticipant() && !activeChar.isOlyCompetitionStarted())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(pet.getTemplate().getNpcId() == 12564)
				{
					return;
				}
				if(!_ctrlPressed && target.isCreature() && !((Creature) target).isAutoAttackable(pet))
				{
					return;
				}
				if(!target.isAttackable(pet))
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return;
				}
				if(!target.isNpc() && (pet.isInZonePeace() || target.isCreature() && ((Creature) target).isInZonePeace()))
				{
					activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
					return;
				}
				if(activeChar.getLevel() + 20 <= pet.getLevel())
				{
					activeChar.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
					return;
				}
				pet.getAI().Attack(target, _ctrlPressed, _shiftPressed);
				break;
			}
			case 17:
			case 23:
			{
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			}
			case 19:
			{
				if(pet.isDead())
				{
					activeChar.sendPacket(SystemMsg.DEAD_PETS_CANNOT_BE_RETURNED_TO_THEIR_SUMMONING_ITEM, ActionFail.STATIC);
					return;
				}
				if(pet.isInCombat())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE, ActionFail.STATIC);
					break;
				}
				if(pet.isPet() && (double) pet.getCurrentFed() < 0.55 * (double) pet.getMaxFed())
				{
					activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_RESTORE_A_HUNGRY_PET, ActionFail.STATIC);
					break;
				}
				pet.unSummon();
				break;
			}
			case 38:
			{
				if(activeChar.getTransformation() != 0)
				{
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(pet == null || !pet.isMountable())
				{
					if(!activeChar.isMounted())
						break;
					if(activeChar.isFlying() && !activeChar.checkLandingState())
					{
						activeChar.sendPacket(Msg.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION, ActionFail.STATIC);
						return;
					}
					activeChar.setMount(0, 0, 0);
					break;
				}
				if(activeChar.isMounted() || activeChar.isInBoat())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.isDead())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(pet.isDead())
				{
					activeChar.sendPacket(Msg.A_DEAD_PET_CANNOT_BE_RIDDEN);
					break;
				}
				if(activeChar.isInDuel())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.isInCombat() || pet.isInCombat())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.isFishing())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.isSitting())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.getActiveWeaponFlagAttachment() != null)
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.isCastingNow())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				if(activeChar.isParalyzed())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
					break;
				}
				activeChar.setMount(pet.getTemplate().npcId, pet.getObjectId(), pet.getLevel());
				pet.unSummon();
				break;
			}
			case 52:
			{
				if(pet.isInCombat())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE);
					activeChar.sendActionFailed();
					break;
				}
				pet.saveEffects();
				pet.unSummon();
				break;
			}
			case 53:
			case 54:
			{
				if(target == null || pet == target || pet.isMovementDisabled())
					break;
				pet.setFollowMode(false);
				pet.moveToLocation(target.getLoc(), 100, true);
				break;
			}
			case 1001:
			{
				break;
			}
			default:
			{
				_log.warn("unhandled action type " + _actionId + " by player " + activeChar.getName());
			}
		}
		activeChar.sendActionFailed();
	}
	
	private void UseSkill(int skillId)
	{
		Player activeChar = getClient().getActiveChar();
		Summon pet = activeChar.getPet();
		if(pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		int skillLevel = PetSkillsTable.getInstance().getAvailableLevel(pet, skillId);
		if(skillLevel == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if(skill == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getLevel() + 20 <= pet.getLevel())
		{
			activeChar.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return;
		}
		Creature aimingTarget = skill.getAimingTarget(pet, activeChar.getTarget());
		if(skill.checkCondition(pet, aimingTarget, _ctrlPressed, _shiftPressed, true))
		{
			pet.getAI().Cast(skill, aimingTarget, _ctrlPressed, _shiftPressed);
		}
		else
		{
			activeChar.sendActionFailed();
		}
	}
	
	public enum Action
	{
		ACTION0(0, 0, 0, 1),
		ACTION1(1, 0, 0, 0),
		ACTION7(7, 0, 0, 1),
		ACTION10(10, 0, 0, 1),
		ACTION28(28, 0, 0, 1),
		ACTION37(37, 0, 0, 1),
		ACTION38(38, 0, 0, 1),
		ACTION51(51, 0, 0, 1),
		ACTION61(61, 0, 0, 1),
		ACTION96(96, 0, 0, 1),
		ACTION97(97, 0, 0, 1),
		ACTION67(67, 0, 0, 1),
		ACTION68(68, 0, 0, 1),
		ACTION69(69, 0, 0, 1),
		ACTION70(70, 0, 0, 1),
		ACTION15(15, 1, 0, 0),
		ACTION16(16, 1, 0, 0),
		ACTION17(17, 1, 0, 0),
		ACTION19(19, 1, 0, 0),
		ACTION21(21, 1, 0, 0),
		ACTION22(22, 1, 0, 0),
		ACTION23(23, 1, 0, 0),
		ACTION52(52, 1, 0, 0),
		ACTION53(53, 1, 0, 0),
		ACTION54(54, 1, 0, 0),
		ACTION32(32, 2, 4230, 0),
		ACTION36(36, 2, 4259, 0),
		ACTION39(39, 2, 4138, 0),
		ACTION41(41, 2, 4230, 0),
		ACTION42(42, 2, 4378, 0),
		ACTION43(43, 2, 4137, 0),
		ACTION44(44, 2, 4139, 0),
		ACTION45(45, 2, 4025, 0),
		ACTION46(46, 2, 4261, 0),
		ACTION47(47, 2, 4260, 0),
		ACTION48(48, 2, 4068, 0),
		ACTION1000(1000, 2, 4079, 0),
		ACTION1003(1003, 2, 4710, 0),
		ACTION1004(1004, 2, 4711, 0),
		ACTION1005(1005, 2, 4712, 0),
		ACTION1006(1006, 2, 4713, 0),
		ACTION1007(1007, 2, 4699, 0),
		ACTION1008(1008, 2, 4700, 0),
		ACTION1009(1009, 2, 4701, 0),
		ACTION1010(1010, 2, 4702, 0),
		ACTION1011(1011, 2, 4703, 0),
		ACTION1012(1012, 2, 4704, 0),
		ACTION1013(1013, 2, 4705, 0),
		ACTION1014(1014, 2, 4706, 0),
		ACTION1015(1015, 2, 4707, 0),
		ACTION1016(1016, 2, 4709, 0),
		ACTION1017(1017, 2, 4708, 0),
		ACTION1031(1031, 2, 5135, 0),
		ACTION1032(1032, 2, 5136, 0),
		ACTION1033(1033, 2, 5137, 0),
		ACTION1034(1034, 2, 5138, 0),
		ACTION1035(1035, 2, 5139, 0),
		ACTION1036(1036, 2, 5142, 0),
		ACTION1037(1037, 2, 5141, 0),
		ACTION1038(1038, 2, 5140, 0),
		ACTION1039(1039, 2, 5110, 0),
		ACTION1040(1040, 2, 5111, 0),
		ACTION1041(1041, 2, 5442, 0),
		ACTION1042(1042, 2, 5444, 0),
		ACTION1043(1043, 2, 5443, 0),
		ACTION1044(1044, 2, 5445, 0),
		ACTION1045(1045, 2, 5584, 0),
		ACTION1046(1046, 2, 5585, 0),
		ACTION1047(1047, 2, 5580, 0),
		ACTION1048(1048, 2, 5581, 0),
		ACTION1049(1049, 2, 5582, 0),
		ACTION1050(1050, 2, 5583, 0),
		ACTION1051(1051, 2, 5638, 0),
		ACTION1052(1052, 2, 5639, 0),
		ACTION1053(1053, 2, 5640, 0),
		ACTION1054(1054, 2, 5643, 0),
		ACTION1055(1055, 2, 5647, 0),
		ACTION1056(1056, 2, 5648, 0),
		ACTION1057(1057, 2, 5646, 0),
		ACTION1058(1058, 2, 5652, 0),
		ACTION1059(1059, 2, 5653, 0),
		ACTION1060(1060, 2, 5654, 0),
		ACTION1061(1061, 2, 5745, 0),
		ACTION1062(1062, 2, 5746, 0),
		ACTION1063(1063, 2, 5747, 0),
		ACTION1064(1064, 2, 5748, 0),
		ACTION1065(1065, 2, 5753, 0),
		ACTION1066(1066, 2, 5749, 0),
		ACTION1067(1067, 2, 5750, 0),
		ACTION1068(1068, 2, 5751, 0),
		ACTION1069(1069, 2, 5752, 0),
		ACTION1070(1070, 2, 5771, 0),
		ACTION1071(1071, 2, 5761, 0),
		ACTION1072(1072, 2, 6046, 0),
		ACTION1073(1073, 2, 6047, 0),
		ACTION1074(1074, 2, 6048, 0),
		ACTION1075(1075, 2, 6049, 0),
		ACTION1076(1076, 2, 6050, 0),
		ACTION1077(1077, 2, 6051, 0),
		ACTION1078(1078, 2, 6052, 0),
		ACTION1079(1079, 2, 6053, 0),
		ACTION1080(1080, 2, 6041, 0),
		ACTION1081(1081, 2, 6042, 0),
		ACTION1082(1082, 2, 6043, 0),
		ACTION1083(1083, 2, 6044, 0),
		ACTION1084(1084, 2, 6054, 0),
		ACTION1086(1086, 2, 6094, 0),
		ACTION1087(1087, 2, 6095, 0),
		ACTION1088(1088, 2, 6096, 0),
		ACTION1089(1089, 2, 6199, 0),
		ACTION1090(1090, 2, 6205, 0),
		ACTION1091(1091, 2, 6206, 0),
		ACTION1092(1092, 2, 6207, 0),
		ACTION1093(1093, 2, 6618, 0),
		ACTION1094(1094, 2, 6681, 0),
		ACTION1095(1095, 2, 6619, 0),
		ACTION1096(1096, 2, 6682, 0),
		ACTION1097(1097, 2, 6683, 0),
		ACTION1098(1098, 2, 6684, 0),
		ACTION5000(5000, 2, 23155, 0),
		ACTION5001(5001, 2, 23167, 0),
		ACTION5002(5002, 2, 23168, 0),
		ACTION5003(5003, 2, 5749, 0),
		ACTION5004(5004, 2, 5750, 0),
		ACTION5005(5005, 2, 5751, 0),
		ACTION5006(5006, 2, 5771, 0),
		ACTION5007(5007, 2, 6046, 0),
		ACTION5008(5008, 2, 6047, 0),
		ACTION5009(5009, 2, 6048, 0),
		ACTION5010(5010, 2, 6049, 0),
		ACTION5011(5011, 2, 6050, 0),
		ACTION5012(5012, 2, 6051, 0),
		ACTION5013(5013, 2, 6052, 0),
		ACTION5014(5014, 2, 6053, 0),
		ACTION5015(5015, 2, 6054, 0),
		ACTION12(12, 3, 2, 2),
		ACTION13(13, 3, 3, 2),
		ACTION14(14, 3, 4, 2),
		ACTION24(24, 3, 6, 2),
		ACTION25(25, 3, 5, 2),
		ACTION26(26, 3, 7, 2),
		ACTION29(29, 3, 8, 2),
		ACTION30(30, 3, 9, 2),
		ACTION31(31, 3, 10, 2),
		ACTION33(33, 3, 11, 2),
		ACTION34(34, 3, 12, 2),
		ACTION35(35, 3, 13, 2),
		ACTION62(62, 3, 14, 2),
		ACTION66(66, 3, 15, 2);
		
		public static final Action[] VALUES;
		
		static
		{
			VALUES = Action.values();
		}
		
		public int id;
		public int type;
		public int value;
		public int transform;
		
		Action(int id, int type, int value, int transform)
		{
			this.id = id;
			this.type = type;
			this.value = value;
			this.transform = transform;
		}
		
		public static Action find(int id)
		{
			for(Action action : VALUES)
			{
				if(action.id != id)
					continue;
				return action;
			}
			return null;
		}
	}
	
	
	static class SocialTask extends RunnableImpl
	{
		Player _player;
		
		SocialTask(Player player)
		{
			_player = player;
		}
		
		@Override
		public void runImpl() throws Exception
		{
			_player.stopParalyzed();
		}
	}
}