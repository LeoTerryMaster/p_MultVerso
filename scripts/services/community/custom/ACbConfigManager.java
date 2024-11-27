package services.community.custom;

import l2.commons.configuration.ExProperties;
import l2.gameserver.Config;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.Player;
import l2.gameserver.scripts.ScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ACbConfigManager implements ScriptFile, ICommunityBoardHandler
{
	public static final String PVPCB_FILE = "config/pvpcommunityboard.properties";
	private static final Logger _log = LoggerFactory.getLogger(ACbConfigManager.class);
	public static int FIRST_CLASS_ID;
	public static int FIRST_CLASS_PRICE;
	public static int SECOND_CLASS_ID;
	public static int SECOND_CLASS_PRICE;
	public static int THRID_CLASS_ID;
	public static int THRID_CLASS_PRICE;
	public static boolean ALLOW_PVPCB_ABNORMAL;
	public static boolean ALLOW_PVPCB_SHOP;
	public static boolean ALLOW_PVPCB_ECHANT;
	public static boolean ALLOW_PVPCB_CLASSMASTER;
	public static boolean ALLOW_PVPCB_SUBMANAGER;
	public static boolean ALLOW_PVPCB_SUBMANAGER_PIACE;
	public static boolean ALLOW_PVPCB_TELEPORT;
	public static boolean ALLOW_PVPCB_SELL;
	public static boolean ALLOW_BBS_NEWS;
	public static int PVPCB_NEWS_PER_PAGE;
	public static ArrayList<Integer> ALLOW_PVPCB_MULTISELL_LIST;
	public static StringTokenizer st;
	public static int ALT_CB_TELE_POINT_PRICE;
	public static int ALT_CB_TELE_POINT_MAX_COUNT;
	public static int ALT_CB_DELVLV_ITEM_ID;
	public static long ALT_CB_DELVL_ITEM_COUNT;
	public static int ALT_CB_NOBLES_ITEM_ID;
	public static long ALT_CB_NOBLES_ITEM_COUNT;
	public static int ALT_CB_CHANGESEX_ITEM_ID;
	public static long ALT_CB_CHANGESEX_ITEM_COUNT;
	public static int ALT_CB_CHANGENAME_ITEM_ID;
	public static long ALT_CB_CHANGENAME_ITEM_COUNT;
	public static int ALT_CB_CLANUP_ITEM_ID;
	public static long ALT_CB_CLANUP_ITEM_COUNT;
	public static int ALT_CB_CLAN_PENALTY_ITEM_ID;
	public static long ALT_CB_CLAN_PENALTY_ITEM_COUNT;
	
	static
	{
		ALLOW_PVPCB_MULTISELL_LIST = new ArrayList();
	}
	
	public static void loadPvPCBSettings()
	{
		ExProperties communityboardpvpSettings = Config.load("config/pvpcommunityboard.properties");
		ALLOW_PVPCB_ABNORMAL = communityboardpvpSettings.getProperty("AllowBBSAbnormal", false);
		ALLOW_PVPCB_SHOP = communityboardpvpSettings.getProperty("AllowBBSShop", true);
		ALLOW_PVPCB_ECHANT = communityboardpvpSettings.getProperty("AllowBBSEnchant", true);
		ALLOW_PVPCB_CLASSMASTER = communityboardpvpSettings.getProperty("AllowBBSClassMaster", true);
		ALLOW_PVPCB_SUBMANAGER = communityboardpvpSettings.getProperty("AllowBBSSubManager", true);
		ALLOW_PVPCB_SUBMANAGER_PIACE = communityboardpvpSettings.getProperty("AllowBBSSubManagerPiace", true);
		ALLOW_PVPCB_TELEPORT = communityboardpvpSettings.getProperty("AllowBBSTeleport", true);
		ALLOW_BBS_NEWS = communityboardpvpSettings.getProperty("AllowBBSNews", false);
		PVPCB_NEWS_PER_PAGE = communityboardpvpSettings.getProperty("AltBBSNewsNewsPerPage", 6);
		ALLOW_PVPCB_SELL = communityboardpvpSettings.getProperty("AllowBBSSell", true);
		for(int id : communityboardpvpSettings.getProperty("AllowMultisellList", new int[0]))
		{
			ALLOW_PVPCB_MULTISELL_LIST.add(id);
		}
		FIRST_CLASS_ID = communityboardpvpSettings.getProperty("FirstProffesionId", 57);
		FIRST_CLASS_PRICE = communityboardpvpSettings.getProperty("FirstProffesionCount", 10000000);
		SECOND_CLASS_ID = communityboardpvpSettings.getProperty("SecondProffesionId", 57);
		SECOND_CLASS_PRICE = communityboardpvpSettings.getProperty("SecondProffesionCount", 20000000);
		THRID_CLASS_ID = communityboardpvpSettings.getProperty("ThridProffesionId", 57);
		THRID_CLASS_PRICE = communityboardpvpSettings.getProperty("ThridProffesionCount", 30000000);
		ALT_CB_TELE_POINT_PRICE = communityboardpvpSettings.getProperty("CommunityTeleporterPointPrice", 100);
		ALT_CB_TELE_POINT_MAX_COUNT = communityboardpvpSettings.getProperty("CommunityTeleporterPointCount", 10);
		ALT_CB_DELVLV_ITEM_ID = communityboardpvpSettings.getProperty("CommunityDeLevelItemId", 57);
		ALT_CB_DELVL_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityDeLevelItemCount", 100);
		ALT_CB_NOBLES_ITEM_ID = communityboardpvpSettings.getProperty("CommunityNobleItemId", 57);
		ALT_CB_NOBLES_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityNobleItemCount", 100);
		ALT_CB_CHANGESEX_ITEM_ID = communityboardpvpSettings.getProperty("CommunityChangeSexItemId", 57);
		ALT_CB_CHANGESEX_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityChangeSexItemCount", 100);
		ALT_CB_CHANGENAME_ITEM_ID = communityboardpvpSettings.getProperty("CommunityChangeNameItemId", 57);
		ALT_CB_CHANGENAME_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityChangeNameItemCount", 100);
		ALT_CB_CLANUP_ITEM_ID = communityboardpvpSettings.getProperty("CommunityClanupNameItemId", 57);
		ALT_CB_CLANUP_ITEM_COUNT = communityboardpvpSettings.getProperty("CommunityClanupItemCount", 100000000);
		ALT_CB_CLAN_PENALTY_ITEM_ID = communityboardpvpSettings.getProperty("ClanPenaltyClearItem", 4037);
		ALT_CB_CLAN_PENALTY_ITEM_COUNT = communityboardpvpSettings.getProperty("ClanPenaltyClearCount", 1);
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return null;
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: Custom Community Config loaded.");
			loadPvPCBSettings();
		}
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
}