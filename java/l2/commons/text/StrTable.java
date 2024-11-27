package l2.commons.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StrTable
{
	private final Map<Integer, Map<String, String>> rows = new HashMap<>();
	private final Map<String, Integer> columns = new LinkedHashMap<>();
	private final List<String> titles = new ArrayList<>();
	
	public StrTable(String title)
	{
		if(title != null)
		{
			titles.add(title);
		}
	}
	
	public StrTable()
	{
		this(null);
	}
	
	private static StringBuilder right(StringBuilder result, String s, int sz)
	{
		result.append(s);
		if((sz -= s.length()) > 0)
		{
			for(int i = 0;i < sz;++i)
			{
				result.append(" ");
			}
		}
		return result;
	}
	
	private static StringBuilder center(StringBuilder result, String s, int sz)
	{
		int offset = result.length();
		result.append(s);
		int i;
		while((i = sz - (result.length() - offset)) > 0)
		{
			result.append(" ");
			if(i <= 1)
				continue;
			result.insert(offset, " ");
		}
		return result;
	}
	
	private static StringBuilder repeat(StringBuilder result, String s, int sz)
	{
		for(int i = 0;i < sz;++i)
		{
			result.append(s);
		}
		return result;
	}
	
	public StrTable set(int rowIndex, String colName, boolean val)
	{
		return set(rowIndex, colName, Boolean.toString(val));
	}
	
	public StrTable set(int rowIndex, String colName, byte val)
	{
		return set(rowIndex, colName, Byte.toString(val));
	}
	
	public StrTable set(int rowIndex, String colName, char val)
	{
		return set(rowIndex, colName, String.valueOf(val));
	}
	
	public StrTable set(int rowIndex, String colName, short val)
	{
		return set(rowIndex, colName, Short.toString(val));
	}
	
	public StrTable set(int rowIndex, String colName, int val)
	{
		return set(rowIndex, colName, Integer.toString(val));
	}
	
	public StrTable set(int rowIndex, String colName, long val)
	{
		return set(rowIndex, colName, Long.toString(val));
	}
	
	public StrTable set(int rowIndex, String colName, float val)
	{
		return set(rowIndex, colName, Float.toString(val));
	}
	
	public StrTable set(int rowIndex, String colName, double val)
	{
		return set(rowIndex, colName, Double.toString(val));
	}
	
	public StrTable set(int rowIndex, String colName, Object val)
	{
		return set(rowIndex, colName, String.valueOf(val));
	}
	
	public StrTable set(int rowIndex, String colName, String val)
	{
		Map row;
		if(rows.containsKey(rowIndex))
		{
			row = rows.get(rowIndex);
		}
		else
		{
			row = new HashMap();
			rows.put(rowIndex, row);
		}
		row.put(colName, val);
		int columnSize;
		if(!columns.containsKey(colName))
		{
			columnSize = Math.max(colName.length(), val.length());
		}
		else
		{
			columnSize = val.length();
			if(columns.get(colName) >= columnSize)
			{
				return this;
			}
		}
		columns.put(colName, columnSize);
		return this;
	}
	
	public StrTable addTitle(String s)
	{
		titles.add(s);
		return this;
	}
	
	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		if(columns.isEmpty())
		{
			return result.toString();
		}
		StringBuilder header = new StringBuilder("|");
		StringBuilder line = new StringBuilder("|");
		for(String c : columns.keySet())
		{
			center(header, c, columns.get(c) + 2).append("|");
			repeat(line, "-", columns.get(c) + 2).append("|");
		}
		if(!titles.isEmpty())
		{
			result.append(" ");
			repeat(result, "-", header.length() - 2).append(" ").append("\n");
			for(String title : titles)
			{
				result.append("| ");
				right(result, title, header.length() - 3).append("|").append("\n");
			}
		}
		result.append(" ");
		repeat(result, "-", header.length() - 2).append(" ").append("\n");
		result.append(header).append("\n");
		result.append(line).append("\n");
		for(Map row : rows.values())
		{
			result.append("|");
			for(String c : columns.keySet())
			{
				center(result, row.containsKey(c) ? (String) row.get(c) : "-", columns.get(c) + 2).append("|");
			}
			result.append("\n");
		}
		result.append(" ");
		repeat(result, "-", header.length() - 2).append(" ").append("\n");
		return result.toString();
	}
}