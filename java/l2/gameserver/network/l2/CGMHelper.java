package l2.gameserver.network.l2;

import l2.gameserver.Config;
import l2.gameserver.network.l2.c2s.L2GameClientPacket;

public abstract class CGMHelper
{
	private static final CGMHelper INSTANCE = init();
	
	private static CGMHelper init()
	{
		CGMHelper inst = null;
		try
		{
			String instClassName = Config.ALT_CG_MODULE;
			for(CGMType cgmType : CGMType.values())
			{
				if(!cgmType.name().equals(Config.ALT_CG_MODULE))
					continue;
				instClassName = cgmType.getImplClassName();
			}
			if(instClassName != null)
			{
				inst = (CGMHelper) Class.forName(instClassName).newInstance();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return inst;
	}
	
	public static CGMHelper getInstance()
	{
		return INSTANCE;
	}
	
	public static boolean isActive()
	{
		return getInstance() != null;
	}
	
	public abstract L2GameClientPacket handle(GameClient client, int opcode);
	
	public abstract GameCrypt createCrypt();
	
	public abstract byte[] getRandomKey();
	
	public abstract void addHWIDBan(String hwid, String ip, String account, String comment);
	
	public enum CGMType
	{
		NONE(null),
		LAMEGUARD("l2.gameserver.network.l2.cgm.LameGuardHelperImpl"),
		SMARTGUARD("l2.gameserver.network.l2.cgm.SmartGuardHelperImpl"),
		STRIXGUARD("l2.gameserver.network.l2.cgm.StrixGuardHelperImpl");
		
		private final String _implClassName;
		
		CGMType(String implClassName)
		{
			_implClassName = implClassName;
		}
		
		public String getImplClassName()
		{
			return _implClassName;
		}
		
		public boolean isActive()
		{
			return _implClassName != null;
		}
	}
}