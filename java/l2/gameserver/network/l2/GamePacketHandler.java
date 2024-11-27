package l2.gameserver.network.l2;

import l2.commons.net.nio.impl.IClientFactory;
import l2.commons.net.nio.impl.IMMOExecutor;
import l2.commons.net.nio.impl.IPacketHandler;
import l2.commons.net.nio.impl.MMOConnection;
import l2.commons.net.nio.impl.ReceivablePacket;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.network.l2.c2s.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public final class GamePacketHandler implements IPacketHandler<GameClient>, IClientFactory<GameClient>, IMMOExecutor<GameClient>
{
	private static final Logger _log = LoggerFactory.getLogger(GamePacketHandler.class);
	
	@Override
	public ReceivablePacket<GameClient> handlePacket(ByteBuffer buf, GameClient client)
	{
		int id = buf.get() & 255;
		L2GameClientPacket msg = null;
		if(CGMHelper.isActive() && (msg = CGMHelper.getInstance().handle(client, id)) != null)
		{
			return msg;
		}
		try
		{
			parada:
			switch(client.getState())
			{
				case CONNECTED:
				{
					switch(id)
					{
						case 0x00:
							msg = new ProtocolVersion();
							break parada;
						case 0x08:
							msg = new AuthLogin();
							break parada;
						case 0xCB:
							msg = new ReplyGameGuardQuery();
							break parada;
						case 0xa8:
							msg = new NetPing();
							break parada;
						default:
							client.onUnknownPacket();
							break parada;
					}
				}
				case AUTHED:
				{
					switch(id)
					{
						case 9:
							msg = new Logout();
							break parada;
						case 11:
							msg = new CharacterCreate();
							break parada;
						case 12:
							msg = new CharacterDelete();
							break parada;
						case 13:
							msg = new CharacterSelected();
							break parada;
						case 14:
							msg = new NewCharacter();
							break parada;
						case 33:
							msg = new RequestBypassToServer();
							break parada;
						case 98:
							msg = new CharacterRestore();
							break parada;
						case 202:
							msg = new ReplyGameGuardQuery();
							break parada;
						case 208:
						{
							int id3 = buf.getShort() & 65535;
							switch(id3)
							{
								case 54:
									msg = new GotoLobby();
									break parada;
								case 147:
									msg = new RequestEx2ndPasswordCheck();
									break parada;
								case 148:
									msg = new RequestEx2ndPasswordVerify();
									break parada;
								case 149:
									msg = new RequestEx2ndPasswordReq();
									break parada;
								default:
									client.onUnknownPacket();
									break parada;
							}
						}
						case 168:
							msg = new NetPing();
							break parada;
						default:
							client.onUnknownPacket();
							break parada;
					}
				}
				case IN_GAME:
				{
					switch(id)
					{
						case 1:
							msg = new MoveBackwardToLocation();
							break parada;
						case 3:
							msg = new EnterWorld();
							break parada;
						case 4:
							msg = new Action();
							break parada;
						case 9:
							msg = new Logout();
							break parada;
						case 10:
							msg = new AttackRequest();
							break parada;
						case 15:
							msg = new RequestItemList();
							break parada;
						case 16:
							break parada;
						case 17:
							msg = new RequestUnEquipItem();
							break parada;
						case 18:
							msg = new RequestDropItem();
							break parada;
						case 20:
							msg = new UseItem();
							break parada;
						case 21:
							msg = new TradeRequest();
							break parada;
						case 22:
							msg = new AddTradeItem();
							break parada;
						case 23:
							msg = new TradeDone();
							break parada;
						case 27:
							msg = new RequestSocialAction();
							break parada;
						case 30:
							msg = new RequestSellItem();
							break parada;
						case 31:
							msg = new RequestBuyItem();
							break parada;
						case 32:
							msg = new RequestLinkHtml();
							break parada;
						case 33:
							msg = new RequestBypassToServer();
							break parada;
						case 34:
							msg = new RequestBBSwrite();
							break parada;
						case 36:
							msg = new RequestJoinPledge();
							break parada;
						case 37:
							msg = new RequestAnswerJoinPledge();
							break parada;
						case 38:
							msg = new RequestWithdrawalPledge();
							break parada;
						case 39:
							msg = new RequestOustPledgeMember();
							break parada;
						case 40:
							break parada;
						case 41:
							msg = new RequestJoinParty();
							break parada;
						case 42:
							msg = new RequestAnswerJoinParty();
							break parada;
						case 43:
							msg = new RequestWithDrawalParty();
							break parada;
						case 44:
							msg = new RequestOustPartyMember();
							break parada;
						case 45:
							break parada;
						case 47:
							msg = new RequestMagicSkillUse();
							break parada;
						case 48:
							msg = new Appearing();
							break parada;
						case 49:
							if(!Config.ALLOW_WAREHOUSE)
								break parada;
							msg = new SendWareHouseDepositList();
							break parada;
						case 50:
							msg = new SendWareHouseWithDrawList();
							break parada;
						case 51:
							msg = new RequestShortCutReg();
							break parada;
						case 53:
							msg = new RequestShortCutDel();
							break parada;
						case 54:
							msg = new CannotMoveAnymore();
							break parada;
						case 55:
							msg = new RequestTargetCanceld();
							break parada;
						case 56:
							msg = new Say2C();
							break parada;
						case 60:
							msg = new RequestPledgeMemberList();
							break parada;
						case 63:
							msg = new RequestSkillList();
							break parada;
						case 65:
							msg = new MoveWithDelta();
							break parada;
						case 66:
							msg = new RequestGetOnVehicle();
							break parada;
						case 67:
							msg = new RequestGetOffVehicle();
							break parada;
						case 68:
							msg = new AnswerTradeRequest();
							break parada;
						case 69:
							msg = new RequestActionUse();
							break parada;
						case 70:
							msg = new RequestRestart();
							break parada;
						case 71:
							msg = new RequestSiegeInfo();
							break parada;
						case 72:
							msg = new ValidatePosition();
							break parada;
						case 73:
							break parada;
						case 74:
							msg = new StartRotatingC();
							break parada;
						case 75:
							msg = new FinishRotatingC();
							break parada;
						case 77:
							msg = new RequestStartPledgeWar();
							break parada;
						case 79:
							msg = new RequestStopPledgeWar();
							break parada;
						case 83:
							msg = new RequestSetPledgeCrest();
							break parada;
						case 85:
							msg = new RequestGiveNickName();
							break parada;
						case 87:
							msg = new RequestShowBoard();
							break parada;
						case 88:
							msg = new RequestEnchantItem();
							break parada;
						case 89:
							msg = new RequestDestroyItem();
							break parada;
						case 91:
							msg = new SendBypassBuildCmd();
							break parada;
						case 92:
							msg = new RequestMoveToLocationInVehicle();
							break parada;
						case 93:
							msg = new CannotMoveAnymoreInVehicle();
							break parada;
						case 94:
							msg = new RequestFriendInvite();
							break parada;
						case 95:
							msg = new RequestFriendAddReply();
							break parada;
						case 96:
							msg = new RequestFriendList();
							break parada;
						case 97:
							msg = new RequestFriendDel();
							break parada;
						case 99:
							msg = new RequestQuestList();
							break parada;
						case 100:
							msg = new RequestQuestAbort();
							break parada;
						case 102:
							msg = new RequestPledgeInfo();
							break parada;
						case 103:
							msg = new RequestPledgeExtendedInfo();
							break parada;
						case 104:
							msg = new RequestPledgeCrest();
							break parada;
						case 106:
							break parada;
						case 107:
							msg = new RequestAquireSkillInfo();
							break parada;
						case 108:
							msg = new RequestAquireSkill();
							break parada;
						case 109:
							msg = new RequestRestartPoint();
							break parada;
						case 110:
							msg = new RequestGMCommand();
							break parada;
						case 111:
							msg = new RequestPartyMatchConfig();
							break parada;
						case 112:
							msg = new RequestPartyMatchList();
							break parada;
						case 113:
							msg = new RequestPartyMatchDetail();
							break parada;
						case 114:
							msg = new RequestCrystallizeItem();
							break parada;
						case 115:
							msg = new RequestPrivateStoreSell();
							break parada;
						case 116:
							msg = new SetPrivateStoreSellList();
							break parada;
						case 117:
							break parada;
						case 118:
							msg = new RequestPrivateStoreQuitSell();
							break parada;
						case 119:
							msg = new SetPrivateStoreMsgSell();
							break parada;
						case 120:
							break parada;
						case 121:
							msg = new RequestPrivateStoreManageBuy();
							break parada;
						case 122:
							break parada;
						case 123:
							msg = new RequestTutorialLinkHtml();
							break parada;
						case 124:
							msg = new RequestTutorialPassCmdToServer();
							break parada;
						case 125:
							msg = new RequestTutorialQuestionMark();
							break parada;
						case 126:
							msg = new RequestTutorialClientEvent();
							break parada;
						case 127:
							msg = new RequestPetition();
							break parada;
						case 128:
							msg = new RequestPetitionCancel();
							break parada;
						case 129:
							msg = new RequestGmList();
							break parada;
						case 130:
							msg = new RequestJoinAlly();
							break parada;
						case 131:
							msg = new RequestAnswerJoinAlly();
							break parada;
						case 132:
							msg = new RequestWithdrawAlly();
							break parada;
						case 133:
							msg = new RequestOustAlly();
							break parada;
						case 134:
							msg = new RequestDismissAlly();
							break parada;
						case 135:
							msg = new RequestSetAllyCrest();
							break parada;
						case 136:
							msg = new RequestAllyCrest();
							break parada;
						case 137:
							msg = new RequestChangePetName();
							break parada;
						case 138:
							msg = new RequestPetUseItem();
							break parada;
						case 139:
							msg = new RequestGiveItemToPet();
							break parada;
						case 140:
							msg = new RequestGetItemFromPet();
							break parada;
						case 142:
							msg = new RequestAllyInfo();
							break parada;
						case 143:
							msg = new RequestPetGetItem();
							break parada;
						case 144:
							msg = new RequestPrivateStoreBuy();
							break parada;
						case 145:
							msg = new SetPrivateStoreBuyList();
							break parada;
						case 146:
							break parada;
						case 147:
							msg = new RequestPrivateStoreQuitBuy();
							break parada;
						case 148:
							msg = new SetPrivateStoreMsgBuy();
							break parada;
						case 149:
							break parada;
						case 150:
							msg = new RequestPrivateStoreBuySellList();
							break parada;
						case 151:
							break parada;
						case 152:
							break parada;
						case 153:
							break parada;
						case 154:
							break parada;
						case 155:
							break parada;
						case 156:
							break parada;
						case 157:
							break parada;
						case 158:
							msg = new RequestPackageSendableItemList();
							break parada;
						case 159:
							msg = new RequestPackageSend();
							break parada;
						case 160:
							msg = new RequestBlock();
							break parada;
						case 161:
							break parada;
						case 162:
							msg = new RequestCastleSiegeAttackerList();
							break parada;
						case 163:
							msg = new RequestCastleSiegeDefenderList();
							break parada;
						case 164:
							msg = new RequestJoinCastleSiege();
							break parada;
						case 165:
							msg = new RequestConfirmCastleSiegeWaitingList();
							break parada;
						case 166:
							msg = new RequestSetCastleSiegeTime();
							break parada;
						case 167:
							msg = new RequestMultiSellChoose();
							break parada;
						case 168:
							msg = new NetPing();
							break parada;
						case 170:
							msg = new BypassUserCmd();
							break parada;
						case 171:
							msg = new SnoopQuit();
							break parada;
						case 172:
							msg = new RequestRecipeBookOpen();
							break parada;
						case 173:
							msg = new RequestRecipeItemDelete();
							break parada;
						case 174:
							msg = new RequestRecipeItemMakeInfo();
							break parada;
						case 175:
							msg = new RequestRecipeItemMakeSelf();
							break parada;
						case 176:
							break parada;
						case 177:
							msg = new RequestRecipeShopMessageSet();
							break parada;
						case 178:
							msg = new RequestRecipeShopListSet();
							break parada;
						case 179:
							msg = new RequestRecipeShopManageQuit();
							break parada;
						case 180:
							msg = new SnoopQuit();
							break parada;
						case 181:
							msg = new RequestRecipeShopMakeInfo();
							break parada;
						case 182:
							msg = new RequestRecipeShopMakeDo();
							break parada;
						case 183:
							msg = new RequestRecipeShopSellList();
							break parada;
						case 184:
							msg = new RequestObserverEnd();
							break parada;
						case 185:
							msg = new RequestVoteNew();
							break parada;
						case 186:
							msg = new RequestHennaList();
							break parada;
						case 187:
							msg = new RequestHennaItemInfo();
							break parada;
						case 188:
							msg = new RequestHennaEquip();
							break parada;
						case 189:
							msg = new RequestHennaUnequipList();
							break parada;
						case 190:
							msg = new RequestHennaUnequipInfo();
							break parada;
						case 191:
							msg = new RequestHennaUnequip();
							break parada;
						case 192:
							msg = new RequestPledgePower();
							break parada;
						case 193:
							msg = new RequestMakeMacro();
							break parada;
						case 194:
							msg = new RequestDeleteMacro();
							break parada;
						case 195:
							msg = new RequestHennaItemInfo();
							break parada;
						case 196:
							msg = new RequestBuySeed();
							break parada;
						case 197:
							msg = new ConfirmDlg();
							break parada;
						case 198:
							msg = new RequestPreviewItem();
							break parada;
						case 199:
							msg = new RequestSSQStatus();
							break parada;
						case 200:
							msg = new PetitionVote();
							break parada;
						case 202:
							msg = new ReplyGameGuardQuery();
							break parada;
						case 204:
							msg = new RequestSendL2FriendSay();
							break parada;
						case 205:
							msg = new RequestShowMiniMap();
							break parada;
						case 206:
							break parada;
						case 207:
							msg = new RequestReload();
							break parada;
						case 208:
						{
							if(buf.remaining() < 2)
							{
								_log.warn("Client: " + client + " sent a 0xd0 without the second opcode.");
								break parada;
							}
							int id2 = buf.getShort() & 65535;
							switch(id2)
							{
								case 1:
									msg = new RequestOustFromPartyRoom();
									break parada;
								case 2:
									msg = new RequestDismissPartyRoom();
									break parada;
								case 3:
									msg = new RequestWithdrawPartyRoom();
									break parada;
								case 4:
									msg = new RequestHandOverPartyMaster();
									break parada;
								case 5:
									msg = new RequestAutoSoulShot();
									break parada;
								case 6:
									msg = new RequestExEnchantSkillInfo();
									break parada;
								case 7:
									msg = new RequestExEnchantSkill();
									break parada;
								case 8:
									msg = new RequestManorList();
									break parada;
								case 9:
									msg = new RequestProcureCropList();
									break parada;
								case 10:
									msg = new RequestSetSeed();
									break parada;
								case 11:
									msg = new RequestSetCrop();
									break parada;
								case 12:
									msg = new RequestWriteHeroWords();
									break parada;
								case 13:
									msg = new RequestExMPCCAskJoin();
									break parada;
								case 14:
									msg = new RequestExMPCCAcceptJoin();
									break parada;
								case 15:
									msg = new RequestExOustFromMPCC();
									break parada;
								case 16:
									msg = new RequestPledgeCrestLarge();
									break parada;
								case 17:
									msg = new RequestSetPledgeCrestLarge();
									break parada;
								case 18:
									msg = new RequestOlympiadObserverEnd();
									break parada;
								case 19:
									msg = new RequestOlympiadMatchList();
									break parada;
								case 20:
									msg = new RequestAskJoinPartyRoom();
									break parada;
								case 21:
									msg = new AnswerJoinPartyRoom();
									break parada;
								case 22:
									msg = new RequestListPartyMatchingWaitingRoom();
									break parada;
								case 23:
									msg = new RequestExitPartyMatchingWaitingRoom();
									break parada;
								case 24:
									msg = new RequestGetBossRecord();
									break parada;
								case 25:
									msg = new RequestPledgeSetAcademyMaster();
									break parada;
								case 26:
									msg = new RequestPledgePowerGradeList();
									break parada;
								case 27:
									msg = new RequestPledgeMemberPowerInfo();
									break parada;
								case 28:
									msg = new RequestPledgeSetMemberPowerGrade();
									break parada;
								case 29:
									msg = new RequestPledgeMemberInfo();
									break parada;
								case 30:
									msg = new RequestPledgeWarList();
									break parada;
								case 31:
									msg = new RequestExFishRanking();
									break parada;
								case 32:
									msg = new RequestPCCafeCouponUse();
									break parada;
								case 34:
									msg = new RequestCursedWeaponList();
									break parada;
								case 35:
									msg = new RequestCursedWeaponLocation();
									break parada;
								case 36:
									msg = new RequestPledgeReorganizeMember();
									break parada;
								case 38:
									msg = new RequestExMPCCShowPartyMembersInfo();
									break parada;
								case 39:
									msg = new RequestDuelStart();
									break parada;
								case 40:
									msg = new RequestDuelAnswerStart();
									break parada;
								case 41:
									msg = new RequestConfirmTargetItem();
									break parada;
								case 42:
									msg = new RequestConfirmRefinerItem();
									break parada;
								case 43:
									msg = new RequestConfirmGemStone();
									break parada;
								case 44:
									msg = new RequestRefine();
									break parada;
								case 45:
									msg = new RequestConfirmCancelItem();
									break parada;
								case 46:
									msg = new RequestRefineCancel();
									break parada;
								case 47:
									msg = new RequestExMagicSkillUseGround();
									break parada;
								case 48:
									msg = new RequestDuelSurrender();
									break parada;
								default:
									client.onUnknownPacket();
									break parada;
							}
						}
						default:
							client.onUnknownPacket();
							break parada;
					}
				}
			}
		}
		catch(BufferUnderflowException e)
		{
			client.onPacketReadFail();
		}
		return msg;
	}
	
	@Override
	public GameClient create(MMOConnection<GameClient> con)
	{
		return new GameClient(con);
	}
	
	@Override
	public void execute(Runnable r)
	{
		ThreadPoolManager.getInstance().execute(r);
	}
}