package parsers;

import l2.commons.data.xml.AbstractDirParser;
import l2.gameserver.Config;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.moveroute.MoveNode;
import l2.gameserver.templates.moveroute.MoveRoute;
import l2.gameserver.templates.moveroute.MoveRouteType;
import org.dom4j.Element;

import java.io.File;

public class MoveRouteParser extends AbstractDirParser<MoveRouteHolder> implements ScriptFile
{
	private static MoveRouteParser INSTANCE;
	
	public MoveRouteParser()
	{
		super(MoveRouteHolder.getInstance());
	}
	
	public static MoveRouteParser getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new MoveRouteParser();
			INSTANCE.load();
		}
		return INSTANCE;
	}
	
	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "./data/superpointinfo");
	}
	
	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}
	
	@Override
	public String getDTDFileName()
	{
		return "superpointinfo.dtd";
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Element e : rootElement.elements())
		{
			String name = e.attributeValue("name");
			MoveRouteType type = MoveRouteType.valueOf(e.attributeValue("type"));
			String running = e.attributeValue("is_running");
			MoveRoute moveRoute = new MoveRoute(name, type, running != null && Boolean.parseBoolean(running));
			for(Element nodeElement : e.elements())
			{
				int x = Integer.parseInt(nodeElement.attributeValue("x"));
				int y = Integer.parseInt(nodeElement.attributeValue("y"));
				int z = Integer.parseInt(nodeElement.attributeValue("z"));
				int socialId = Integer.parseInt(nodeElement.attributeValue("social", "0"));
				long delay = Long.parseLong(nodeElement.attributeValue("delay", "0"));
				String msgAddr = nodeElement.attributeValue("msg_addr");
				ChatType chatType = null;
				if(msgAddr != null)
				{
					chatType = ChatType.valueOf(nodeElement.attributeValue("chat_type"));
				}
				MoveNode node = new MoveNode(x, y, z, msgAddr, socialId, delay, chatType);
				moveRoute.getNodes().add(node);
			}
			getHolder().addRoute(moveRoute);
		}
	}
	
	@Override
	public void onLoad()
	{
		getInstance();
	}
	
	@Override
	public void onReload()
	{
		getInstance().reload();
	}
	
	@Override
	public void onShutdown()
	{
	}
}