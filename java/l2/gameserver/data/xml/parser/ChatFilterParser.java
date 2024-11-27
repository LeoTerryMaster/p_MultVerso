package l2.gameserver.data.xml.parser;

import gnu.trove.TIntArrayList;
import l2.commons.data.xml.AbstractFileParser;
import l2.gameserver.model.chat.ChatFilters;
import l2.gameserver.model.chat.chatfilter.ChatFilter;
import l2.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import l2.gameserver.model.chat.chatfilter.matcher.*;
import l2.gameserver.network.l2.components.ChatType;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class ChatFilterParser extends AbstractFileParser<ChatFilters>
{
	private static final ChatFilterParser _instance = new ChatFilterParser();
	
	protected ChatFilterParser()
	{
		super(ChatFilters.getinstance());
	}
	
	public static ChatFilterParser getInstance()
	{
		return _instance;
	}
	
	protected List<ChatFilterMatcher> parseMatchers(Element n) throws Exception
	{
		ArrayList<ChatFilterMatcher> matchers = new ArrayList<>();
		Iterator nItr = n.elementIterator();
		while(nItr.hasNext())
		{
			StringTokenizer st;
			Element e = (Element) nItr.next();
			if(e.getName().equals("Channels"))
			{
				st = new StringTokenizer(e.getText(), ",");
				ArrayList<ChatType> channels = new ArrayList<>();
				while(st.hasMoreTokens())
				{
					channels.add(ChatType.valueOf(st.nextToken()));
				}
				matchers.add(new MatchChatChannels(channels.toArray(new ChatType[channels.size()])));
				continue;
			}
			if(e.getName().equals("Maps"))
			{
				TIntArrayList maps = new TIntArrayList();
				st = new StringTokenizer(e.getText(), ",");
				while(st.hasMoreTokens())
				{
					String[] map = st.nextToken().split("_");
					maps.add(Integer.parseInt(map[0]));
					maps.add(Integer.parseInt(map[1]));
				}
				matchers.add(new MatchMaps(maps.toNativeArray()));
				continue;
			}
			if(e.getName().equals("Words"))
			{
				st = new StringTokenizer(e.getText());
				ArrayList<String> words = new ArrayList<>();
				while(st.hasMoreTokens())
				{
					words.add(st.nextToken());
				}
				matchers.add(new MatchWords(words.toArray(new String[words.size()])));
				continue;
			}
			if(e.getName().equals("ExcludePremium"))
			{
				matchers.add(new MatchPremiumState(Boolean.parseBoolean(e.getText())));
				continue;
			}
			if(e.getName().equals("Level"))
			{
				matchers.add(new MatchMinLevel(Integer.parseInt(e.getText())));
				continue;
			}
			if(e.getName().equals("JobLevel"))
			{
				matchers.add(new MatchMinJobLevel(Integer.parseInt(e.getText())));
				continue;
			}
			if(e.getName().equals("OnlineTime"))
			{
				matchers.add(new MatchMinOnlineTime(Integer.parseInt(e.getText())));
				continue;
			}
			if(e.getName().equals("LiveTime"))
			{
				matchers.add(new MatchMinLiveTime(Integer.parseInt(e.getText())));
				continue;
			}
			if(e.getName().endsWith("Limit"))
			{
				int limitCount = 0;
				int limitTime = 0;
				int limitBurst = 0;
				Iterator eItr = e.elementIterator();
				while(eItr.hasNext())
				{
					Element d = (Element) eItr.next();
					if(d.getName().equals("Count"))
					{
						limitCount = Integer.parseInt(d.getText());
						continue;
					}
					if(d.getName().equals("Time"))
					{
						limitTime = Integer.parseInt(d.getText());
						continue;
					}
					if(!d.getName().equals("Burst"))
						continue;
					limitBurst = Integer.parseInt(d.getText());
				}
				if(limitCount < 1)
				{
					throw new IllegalArgumentException("Limit Count < 1!");
				}
				if(limitTime < 1)
				{
					throw new IllegalArgumentException("Limit Time  < 1!");
				}
				if(limitBurst < 1)
				{
					throw new IllegalArgumentException("Limit Burst < 1!");
				}
				if(e.getName().equals("Limit"))
				{
					matchers.add(new MatchChatLimit(limitCount, limitTime, limitBurst));
					continue;
				}
				if(e.getName().equals("FloodLimit"))
				{
					matchers.add(new MatchFloodLimit(limitCount, limitTime, limitBurst));
					continue;
				}
				if(!e.getName().equals("RecipientLimit"))
					continue;
				matchers.add(new MatchRecipientLimit(limitCount, limitTime, limitBurst));
				continue;
			}
			List<ChatFilterMatcher> matches;
			if(e.getName().equals("Or"))
			{
				matches = parseMatchers(e);
				matchers.add(new MatchLogicalOr(matches.toArray(new ChatFilterMatcher[matches.size()])));
				continue;
			}
			if(e.getName().equals("And"))
			{
				matches = parseMatchers(e);
				matchers.add(new MatchLogicalAnd(matches.toArray(new ChatFilterMatcher[matches.size()])));
				continue;
			}
			if(e.getName().equals("Not"))
			{
				matches = parseMatchers(e);
				if(matches.size() == 1)
				{
					matchers.add(new MatchLogicalNot(matches.get(0)));
					continue;
				}
				matchers.add(new MatchLogicalNot(new MatchLogicalAnd(matches.toArray(new ChatFilterMatcher[matches.size()]))));
				continue;
			}
			if(!e.getName().equals("Xor"))
				continue;
			matches = parseMatchers(e);
			matchers.add(new MatchLogicalXor(matches.toArray(new ChatFilterMatcher[matches.size()])));
		}
		return matchers;
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		Iterator iterator = rootElement.elementIterator();
		while(iterator.hasNext())
		{
			int action = 0;
			String value = null;
			Element filterElement = (Element) iterator.next();
			Iterator filterItr = filterElement.elementIterator();
			while(filterItr.hasNext())
			{
				Element e = (Element) filterItr.next();
				if(e.getName().equals("Action"))
				{
					String banStr = e.getText();
					if(banStr.equals("BanChat"))
					{
						action = 1;
						continue;
					}
					if(banStr.equals("WarnMsg"))
					{
						action = 2;
						continue;
					}
					if(banStr.equals("ReplaceMsg"))
					{
						action = 3;
						continue;
					}
					if(!banStr.equals("RedirectMsg"))
						continue;
					action = 4;
					continue;
				}
				if(e.getName().equals("BanTime"))
				{
					value = String.valueOf(Integer.parseInt(e.getText()));
					continue;
				}
				if(e.getName().equals("RedirectChannel"))
				{
					value = ChatType.valueOf(e.getText()).toString();
					continue;
				}
				if(e.getName().equals("ReplaceMsg"))
				{
					value = e.getText();
					continue;
				}
				if(!e.getName().equals("WarnMsg"))
					continue;
				value = e.getText();
			}
			List<ChatFilterMatcher> matchers = parseMatchers(filterElement);
			if(matchers.isEmpty())
			{
				throw new IllegalArgumentException("No matchers defined for a filter!");
			}
			ChatFilterMatcher matcher = matchers.size() == 1 ? matchers.get(0) : new MatchLogicalAnd(matchers.toArray(new ChatFilterMatcher[matchers.size()]));
			getHolder().add(new ChatFilter(matcher, action, value));
		}
	}
	
	@Override
	public File getXMLFile()
	{
		return new File("config/chatfilters.xml");
	}
	
	@Override
	public String getDTDFileName()
	{
		return "chatfilters.dtd";
	}
}