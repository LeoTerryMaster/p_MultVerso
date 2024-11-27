package l2.commons.util;

import java.io.File;
import java.util.Comparator;

public class NaturalOrderComparator implements Comparator
{
	public static final Comparator<String> STRING_COMPARATOR = new Comparator<String>()
	{
		@Override
		public int compare(String o1, String o2)
		{
			return compare0(o1, o2);
		}
	};
	public static final Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>()
	{
		@Override
		public int compare(File o1, File o2)
		{
			return compare0(o1.getName(), o2.getName());
		}
	};
	public static final Comparator<File> ABSOLUTE_FILE_NAME_COMPARATOR = new Comparator<File>()
	{
		@Override
		public int compare(File o1, File o2)
		{
			return compare0(o1.getAbsolutePath(), o2.getAbsolutePath());
		}
	};
	
	private static int compareRight(String a, String b)
	{
		int bias = 0;
		int ia = 0;
		int ib = 0;
		do
		{
			char ca = charAt(a, ia);
			char cb = charAt(b, ib);
			if(!Character.isDigit(ca) && !Character.isDigit(cb))
			{
				return bias;
			}
			if(!Character.isDigit(ca))
			{
				return -1;
			}
			if(!Character.isDigit(cb))
			{
				return 1;
			}
			if(ca < cb)
			{
				if(bias == 0)
				{
					bias = -1;
				}
			}
			else if(ca > cb)
			{
				if(bias == 0)
				{
					bias = 1;
				}
			}
			else if(ca == '\u0000' && cb == '\u0000')
			{
				return bias;
			}
			++ia;
			++ib;
		}
		while(true);
	}
	
	private static int compare0(String a, String b)
	{
		int ia = 0;
		int ib = 0;
		int nza = 0;
		int nzb = 0;
		do
		{
			nzb = 0;
			nza = 0;
			char ca = charAt(a, ia);
			char cb = charAt(b, ib);
			while(Character.isSpaceChar(ca) || ca == '0')
			{
				nza = ca == '0' ? ++nza : 0;
				ca = charAt(a, ++ia);
			}
			while(Character.isSpaceChar(cb) || cb == '0')
			{
				nzb = cb == '0' ? ++nzb : 0;
				cb = charAt(b, ++ib);
			}
			int result;
			if(Character.isDigit(ca) && Character.isDigit(cb) && (result = compareRight(a.substring(ia), b.substring(ib))) != 0)
			{
				return result;
			}
			if(ca == '\u0000' && cb == '\u0000')
			{
				return nza - nzb;
			}
			if(ca < cb)
			{
				return -1;
			}
			if(ca > cb)
			{
				return 1;
			}
			++ia;
			++ib;
		}
		while(true);
	}
	
	private static char charAt(String s, int i)
	{
		if(i >= s.length())
		{
			return '\u0000';
		}
		return s.charAt(i);
	}
	
	@Override
	public int compare(Object o1, Object o2)
	{
		String a = o1.toString();
		String b = o2.toString();
		return compare0(a, b);
	}
}