package l2.gameserver.model.actor.instances.player;

public class Macro
{
	public static final int CMD_TYPE_SKILL = 1;
	public static final int CMD_TYPE_ACTION = 3;
	public static final int CMD_TYPE_SHORTCUT = 4;
	public final int icon;
	public final String name;
	public final String descr;
	public final String acronym;
	public final L2MacroCmd[] commands;
	public int id;
	
	public Macro(int id, int icon, String name, String descr, String acronym, L2MacroCmd[] commands)
	{
		this.id = id;
		this.icon = icon;
		this.name = name;
		this.descr = descr;
		this.acronym = acronym.length() > 4 ? acronym.substring(0, 4) : acronym;
		this.commands = commands;
	}
	
	@Override
	public String toString()
	{
		return "macro id=" + id + " icon=" + icon + "name=" + name + " descr=" + descr + " acronym=" + acronym + " commands=" + commands;
	}
	
	public static class L2MacroCmd
	{
		public final int entry;
		public final int type;
		public final int d1;
		public final int d2;
		public final String cmd;
		
		public L2MacroCmd(int entry, int type, int d1, int d2, String cmd)
		{
			this.entry = entry;
			this.type = type;
			this.d1 = d1;
			this.d2 = d2;
			this.cmd = cmd;
		}
	}
}