package l2.gameserver.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArabicConv
{
	private static final char ALF_UPPER_MDD = '\u0622';
	private static final char ALF_UPPER_HAMAZA = '\u0623';
	private static final char ALF_LOWER_HAMAZA = '\u0625';
	private static final char ALF = '\u0627';
	private static final char LAM = '\u0644';
	private static final char[][] ARABIC_GLPHIES = {{'\u0622', '\ufe81', '\ufe81', '\ufe82', '\ufe82', '\u0002'}, {'\u0623', '\ufe82', '\ufe83', '\ufe84', '\ufe84', '\u0002'}, {'\u0624', '\ufe85', '\ufe85', '\ufe86', '\ufe86', '\u0002'}, {'\u0625', '\ufe87', '\ufe87', '\ufe88', '\ufe88', '\u0002'}, {'\u0626', '\ufe89', '\ufe8b', '\ufe8c', '\ufe8a', '\u0004'}, {'\u0627', '\u0627', '\u0627', '\ufe8e', '\ufe8e', '\u0002'}, {'\u0628', '\ufe8f', '\ufe91', '\ufe92', '\ufe90', '\u0004'}, {'\u0629', '\ufe93', '\ufe93', '\ufe94', '\ufe94', '\u0002'}, {'\u062a', '\ufe95', '\ufe97', '\ufe98', '\ufe96', '\u0004'}, {'\u062b', '\ufe99', '\ufe9b', '\ufe9c', '\ufe9a', '\u0004'}, {'\u062c', '\ufe9d', '\ufe9f', '\ufea0', '\ufe9e', '\u0004'}, {'\u062d', '\ufea1', '\ufea3', '\ufea4', '\ufea2', '\u0004'}, {'\u062e', '\ufea5', '\ufea7', '\ufea8', '\ufea6', '\u0004'}, {'\u062f', '\ufea9', '\ufea9', '\ufeaa', '\ufeaa', '\u0002'}, {'\u0630', '\ufeab', '\ufeab', '\ufeac', '\ufeac', '\u0002'}, {'\u0631', '\ufead', '\ufead', '\ufeae', '\ufeae', '\u0002'}, {'\u0632', '\ufeaf', '\ufeaf', '\ufeb0', '\ufeb0', '\u0002'}, {'\u0633', '\ufeb1', '\ufeb3', '\ufeb4', '\ufeb2', '\u0004'}, {'\u0634', '\ufeb5', '\ufeb7', '\ufeb8', '\ufeb6', '\u0004'}, {'\u0635', '\ufeb9', '\ufebb', '\ufebc', '\ufeba', '\u0004'}, {'\u0636', '\ufebd', '\ufebf', '\ufec0', '\ufebe', '\u0004'}, {'\u0637', '\ufec1', '\ufec3', '\ufec2', '\ufec4', '\u0004'}, {'\u0638', '\ufec5', '\ufec7', '\ufec6', '\ufec6', '\u0004'}, {'\u0639', '\ufec9', '\ufecb', '\ufecc', '\ufeca', '\u0004'}, {'\u063a', '\ufecd', '\ufecf', '\ufed0', '\ufece', '\u0004'}, {'\u0641', '\ufed1', '\ufed3', '\ufed4', '\ufed2', '\u0004'}, {'\u0642', '\ufed5', '\ufed7', '\ufed8', '\ufed6', '\u0004'}, {'\u0643', '\ufed9', '\ufedb', '\ufedc', '\ufeda', '\u0004'}, {'\u0644', '\ufedd', '\ufedf', '\ufee0', '\ufede', '\u0004'}, {'\u0645', '\ufee1', '\ufee3', '\ufee4', '\ufee2', '\u0004'}, {'\u0646', '\ufee5', '\ufee7', '\ufee8', '\ufee6', '\u0004'}, {'\u0647', '\ufee9', '\ufeeb', '\ufeec', '\ufeea', '\u0004'}, {'\u0648', '\ufeed', '\ufeed', '\ufeee', '\ufeee', '\u0002'}, {'\u0649', '\ufeef', '\ufeef', '\ufef0', '\ufef0', '\u0002'}, {'\u0671', '\u0671', '\u0671', '\ufb51', '\ufb51', '\u0002'}, {'\u064a', '\ufef1', '\ufef3', '\ufef4', '\ufef2', '\u0004'}, {'\u066e', '\ufbe4', '\ufbe8', '\ufbe9', '\ufbe5', '\u0004'}, {'\u0671', '\u0671', '\u0671', '\ufb51', '\ufb51', '\u0002'}, {'\u06aa', '\ufb8e', '\ufb90', '\ufb91', '\ufb8f', '\u0004'}, {'\u06c1', '\ufba6', '\ufba8', '\ufba9', '\ufba7', '\u0004'}, {'\u06e4', '\u06e4', '\u06e4', '\u06e4', '\ufeee', '\u0002'}, {'\u0686', '\ufb7a', '\ufb7c', '\ufb7d', '\ufb7b', '\u0004'}, {'\u067e', '\ufb56', '\ufb58', '\ufb59', '\ufb57', '\u0004'}, {'\u0698', '\ufb8a', '\ufb8a', '\ufb8b', '\ufb8b', '\u0002'}, {'\u06af', '\ufb92', '\ufb94', '\ufb95', '\ufb93', '\u0004'}, {'\u06cc', '\ufeef', '\ufef3', '\ufef4', '\ufef0', '\u0004'}, {'\u06a9', '\ufb8e', '\ufb90', '\ufb91', '\ufb8f', '\u0004'}};
	private static final Map<Character, char[]> ARABIC_GLPHIES_MAP;
	private static final char[] HARAKATE;
	private static final char[][] LAM_ALEF_GLPHIES;
	
	static
	{
		HARAKATE = new char[] {'\u064b', '\u064c', '\u064d', '\u064e', '\u064f', '\u0650', '\u0651', '\u0652', '\u0653', '\u0654', '\u0655', '\u0656'};
		LAM_ALEF_GLPHIES = new char[][] {{'\u3ba6', '\ufef6', '\ufef5'}, {'\u3ba7', '\ufef8', '\ufef7'}, {'\u0625', '\ufefa', '\ufef9'}, {'\u0627', '\ufefc', '\ufefb'}};
		HashMap<Character, char[]> arabivGlphiesMap = new HashMap<>();
		for(char[] forms : ARABIC_GLPHIES)
		{
			arabivGlphiesMap.put(Character.valueOf(forms[0]), forms);
		}
		ARABIC_GLPHIES_MAP = Collections.unmodifiableMap(arabivGlphiesMap);
	}
	
	private static char getLamAlef(char AlefCand, char LamCand, boolean isEnd)
	{
		int shiftRate = 1;
		if(isEnd)
		{
			++shiftRate;
		}
		if('\u0644' == LamCand)
		{
			switch(AlefCand)
			{
				case '\u0622':
				{
					return LAM_ALEF_GLPHIES[0][shiftRate];
				}
				case '\u0623':
				{
					return LAM_ALEF_GLPHIES[1][shiftRate];
				}
				case '\u0625':
				{
					return LAM_ALEF_GLPHIES[2][shiftRate];
				}
				case '\u0627':
				{
					return LAM_ALEF_GLPHIES[3][shiftRate];
				}
			}
		}
		return '\u0000';
	}
	
	private static final char getReshapedGlphy(char ch, int off)
	{
		char[] forms = ARABIC_GLPHIES_MAP.get(Character.valueOf(ch));
		if(forms != null)
		{
			if(ch != forms[0])
			{
				throw new RuntimeException();
			}
			return forms[off];
		}
		return ch;
	}
	
	private static final char getGlphyType(char ch)
	{
		char[] forms = ARABIC_GLPHIES_MAP.get(Character.valueOf(ch));
		if(forms != null)
		{
			if(ch != forms[0])
			{
				throw new RuntimeException();
			}
			return forms[5];
		}
		return '\u0002';
	}
	
	private static String shapeArabic0(String src)
	{
		if(src.isEmpty())
		{
			return "";
		}
		switch(src.length())
		{
			case 0:
			{
				return "";
			}
			case 1:
			{
				return new String(new char[] {getReshapedGlphy(src.charAt(0), 0)});
			}
			case 2:
			{
				char lam = src.charAt(0);
				char alif = src.charAt(1);
				char lam_alif2 = getLamAlef(alif, lam, true);
				if(lam_alif2 <= '\u0000')
					break;
				return new String(new char[] {lam_alif2});
			}
		}
		char[] reshapedLetters = new char[src.length()];
		char currLetter = src.charAt(0);
		reshapedLetters[0] = getReshapedGlphy(currLetter, 2);
		char lam_alif;
		for(int i = 1;i < src.length() - 1;++i)
		{
			lam_alif = getLamAlef(src.charAt(i), currLetter, true);
			if(lam_alif > '\u0000')
			{
				if(i - 2 < 0 || i - 2 >= 0 && getGlphyType(src.charAt(i - 2)) == '\u0002')
				{
					reshapedLetters[i - 1] = '\u0000';
					reshapedLetters[i] = lam_alif;
				}
				else
				{
					reshapedLetters[i - 1] = '\u0000';
					reshapedLetters[i] = getLamAlef(src.charAt(i), currLetter, false);
				}
			}
			else
			{
				reshapedLetters[i] = getGlphyType(src.charAt(i - 1)) == '\u0002' ? getReshapedGlphy(src.charAt(i), 2) : getReshapedGlphy(src.charAt(i), 3);
			}
			currLetter = src.charAt(i);
		}
		int len = src.length();
		lam_alif = getLamAlef(src.charAt(len - 1), src.charAt(len - 2), true);
		if(lam_alif > '\u0000')
		{
			if(len > 3 && getGlphyType(src.charAt(len - 3)) == '\u0002')
			{
				reshapedLetters[len - 2] = '\u0000';
				reshapedLetters[len - 1] = lam_alif;
			}
			else
			{
				reshapedLetters[len - 2] = '\u0000';
				reshapedLetters[len - 1] = getLamAlef(src.charAt(len - 1), src.charAt(len - 2), false);
			}
		}
		else
		{
			reshapedLetters[len - 1] = getGlphyType(src.charAt(len - 2)) == '\u0002' ? getReshapedGlphy(src.charAt(len - 1), 1) : getReshapedGlphy(src.charAt(len - 1), 4);
		}
		StringBuilder sb = new StringBuilder();
		for(char ch : reshapedLetters)
		{
			if(ch == '\u0000')
				continue;
			sb.append(ch);
		}
		return sb.toString();
	}
	
	public static boolean isArChar(char ch)
	{
		char[] form = ARABIC_GLPHIES_MAP.get(Character.valueOf(ch));
		return form != null;
	}
	
	public static String shapeArabic(String src)
	{
		StringBuilder sb = new StringBuilder();
		int len = src.length();
		for(int i = 0;i < len;++i)
		{
			if(isArChar(src.charAt(i)))
			{
				int arStart = i;
				while(i < len && isArChar(src.charAt(i)))
				{
					++i;
				}
				sb.append(shapeArabic0(src.substring(arStart, i)));
				if(i >= len)
					continue;
				sb.append(src.charAt(i));
				continue;
			}
			sb.append(src.charAt(i));
		}
		return sb.toString();
	}
	
	public static final void main(String... args)
	{
	}
}