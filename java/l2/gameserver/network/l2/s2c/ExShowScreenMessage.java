package l2.gameserver.network.l2.s2c;

public class ExShowScreenMessage extends L2GameServerPacket
{
	public static final int SYSMSG_TYPE = 0;
	public static final int STRING_TYPE = 1;
	private final String _text;
	private int _type;
	private int _sysMessageId;
	private boolean _big_font;
	private boolean _effect;
	private ScreenMessageAlign _text_align;
	private int _time;
	
	@Deprecated
	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font)
	{
		this(text, time, 0, text_align, big_font, 1, -1, false);
	}
	
	public ExShowScreenMessage(String text, int time, int sysMsgId, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect)
	{
		_type = type;
		_sysMessageId = messageId;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
		_type = type;
		_sysMessageId = sysMsgId;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
		_text = text;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(56);
		writeD(_type);
		writeD(_sysMessageId);
		writeD(_text_align.ordinal() + 1);
		writeD(0);
		writeD(_big_font ? 0 : 1);
		writeD(0);
		writeD(0);
		writeD(_effect ? 1 : 0);
		writeD(_time);
		writeD(1);
		writeS(_text);
	}
	
	public enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT;
	}
}