package l2.commons.math;

public class SafeMath
{
	public static int addAndCheck(int a, int b) throws ArithmeticException
	{
		return addAndCheck(a, b, "overflow: add", false);
	}
	
	public static int addAndLimit(int a, int b)
	{
		return addAndCheck(a, b, null, true);
	}
	
	private static int addAndCheck(int a, int b, String msg, boolean limit)
	{
		if(a > b)
		{
			return addAndCheck(b, a, msg, limit);
		}
		if(a < 0)
		{
			if(b >= 0)
				return a + b;
			if(Integer.MIN_VALUE - b <= a)
			{
				return a + b;
			}
			if(!limit)
				throw new ArithmeticException(msg);
			return Integer.MIN_VALUE;
		}
		if(a <= Integer.MAX_VALUE - b)
		{
			return a + b;
		}
		if(!limit)
			throw new ArithmeticException(msg);
		return Integer.MAX_VALUE;
	}
	
	public static long addAndLimit(long a, long b)
	{
		return addAndCheck(a, b, "overflow: add", true);
	}
	
	public static long addAndCheck(long a, long b) throws ArithmeticException
	{
		return addAndCheck(a, b, "overflow: add", false);
	}
	
	private static long addAndCheck(long a, long b, String msg, boolean limit)
	{
		if(a > b)
		{
			return addAndCheck(b, a, msg, limit);
		}
		if(a < 0)
		{
			if(b >= 0)
				return a + b;
			if(Long.MIN_VALUE - b <= a)
			{
				return a + b;
			}
			if(!limit)
				throw new ArithmeticException(msg);
			return Long.MIN_VALUE;
		}
		if(a <= Long.MAX_VALUE - b)
		{
			return a + b;
		}
		if(!limit)
			throw new ArithmeticException(msg);
		return Long.MAX_VALUE;
	}
	
	public static int mulAndCheck(int a, int b) throws ArithmeticException
	{
		return mulAndCheck(a, b, "overflow: mul", false);
	}
	
	public static int mulAndLimit(int a, int b)
	{
		return mulAndCheck(a, b, "overflow: mul", true);
	}
	
	private static int mulAndCheck(int a, int b, String msg, boolean limit)
	{
		if(a > b)
		{
			return mulAndCheck(b, a, msg, limit);
		}
		if(a < 0)
		{
			if(b < 0)
			{
				if(a >= Integer.MAX_VALUE / b)
				{
					return a * b;
				}
				if(!limit)
					throw new ArithmeticException(msg);
				return Integer.MAX_VALUE;
			}
			if(b <= 0)
				return 0;
			if(Integer.MIN_VALUE / b <= a)
			{
				return a * b;
			}
			if(!limit)
				throw new ArithmeticException(msg);
			return Integer.MIN_VALUE;
		}
		if(a <= 0)
			return 0;
		if(a <= Integer.MAX_VALUE / b)
		{
			return a * b;
		}
		if(!limit)
			throw new ArithmeticException(msg);
		return Integer.MAX_VALUE;
	}
	
	public static long mulAndCheck(long a, long b) throws ArithmeticException
	{
		return mulAndCheck(a, b, "overflow: mul", false);
	}
	
	public static long mulAndLimit(long a, long b)
	{
		return mulAndCheck(a, b, "overflow: mul", true);
	}
	
	private static long mulAndCheck(long a, long b, String msg, boolean limit)
	{
		if(a > b)
		{
			return mulAndCheck(b, a, msg, limit);
		}
		if(a < 0)
		{
			if(b < 0)
			{
				if(a >= Long.MAX_VALUE / b)
				{
					return a * b;
				}
				if(!limit)
					throw new ArithmeticException(msg);
				return Long.MAX_VALUE;
			}
			if(b <= 0)
				return 0;
			if(Long.MIN_VALUE / b <= a)
			{
				return a * b;
			}
			if(!limit)
				throw new ArithmeticException(msg);
			return Long.MIN_VALUE;
		}
		if(a <= 0)
			return 0;
		if(a <= Long.MAX_VALUE / b)
		{
			return a * b;
		}
		if(!limit)
			throw new ArithmeticException(msg);
		return Long.MAX_VALUE;
	}
}