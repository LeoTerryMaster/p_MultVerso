package l2.commons.lang;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;

public final class ArrayUtils
{
	public static final int INDEX_NOT_FOUND = -1;
	
	public static <T> T valid(T[] array, int index)
	{
		if(array == null)
		{
			return null;
		}
		if(index < 0 || array.length <= index)
		{
			return null;
		}
		return array[index];
	}
	
	public static <T> T[] add(T[] array, T element)
	{
		Class type = array != null ? array.getClass().getComponentType() : element != null ? element.getClass() : Object.class;
		T[] newArray = (T[]) copyArrayGrow(array, type);
		newArray[newArray.length - 1] = element;
		return newArray;
	}
	
	private static <T> T[] copyArrayGrow(T[] array, Class<? extends T> type)
	{
		if(array != null)
		{
			int arrayLength = Array.getLength(array);
			Object[] newArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
			System.arraycopy(array, 0, newArray, 0, arrayLength);
			return (T[]) newArray;
		}
		return (T[]) Array.newInstance(type, 1);
	}
	
	public static <T> boolean contains(T[] array, T value)
	{
		if(array == null)
		{
			return false;
		}
		for(int i = 0;i < array.length;++i)
		{
			if(value != array[i])
				continue;
			return true;
		}
		return false;
	}
	
	public static <T> int indexOf(T[] array, T value, int index)
	{
		if(index < 0 || array.length <= index)
		{
			return -1;
		}
		for(int i = index;i < array.length;++i)
		{
			if(value != array[i])
				continue;
			return i;
		}
		return -1;
	}
	
	public static <T> T[] remove(T[] array, T value)
	{
		if(array == null)
		{
			return null;
		}
		int index = indexOf(array, value, 0);
		if(index == -1)
		{
			return array;
		}
		int length = array.length;
		Object[] newArray = (Object[]) Array.newInstance(array.getClass().getComponentType(), length - 1);
		System.arraycopy(array, 0, newArray, 0, index);
		if(index < length - 1)
		{
			System.arraycopy(array, index + 1, newArray, index, length - index - 1);
		}
		return (T[]) newArray;
	}
	
	private static <T extends Comparable<T>> void eqBrute(T[] a, int lo, int hi)
	{
		if(hi - lo == 1)
		{
			if(a[hi].compareTo(a[lo]) < 0)
			{
				T e = a[lo];
				a[lo] = a[hi];
				a[hi] = e;
			}
		}
		else if(hi - lo == 2)
		{
			int pmin = a[lo].compareTo(a[lo + 1]) < 0 ? lo : lo + 1;
			int n = pmin = a[pmin].compareTo(a[lo + 2]) < 0 ? pmin : lo + 2;
			if(pmin != lo)
			{
				T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			eqBrute(a, lo + 1, hi);
		}
		else if(hi - lo == 3)
		{
			int pmin = a[lo].compareTo(a[lo + 1]) < 0 ? lo : lo + 1;
			pmin = a[pmin].compareTo(a[lo + 2]) < 0 ? pmin : lo + 2;
			int n = pmin = a[pmin].compareTo(a[lo + 3]) < 0 ? pmin : lo + 3;
			if(pmin != lo)
			{
				T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			int pmax = a[hi].compareTo(a[hi - 1]) > 0 ? hi : hi - 1;
			int n2 = pmax = a[pmax].compareTo(a[hi - 2]) > 0 ? pmax : hi - 2;
			if(pmax != hi)
			{
				T e = a[hi];
				a[hi] = a[pmax];
				a[pmax] = e;
			}
			eqBrute(a, lo + 1, hi - 1);
		}
	}
	
	private static <T extends Comparable<T>> void eqSort(T[] a, int lo0, int hi0)
	{
		int hi = hi0;
		int lo = lo0;
		if(hi - lo <= 3)
		{
			eqBrute(a, lo, hi);
			return;
		}
		T pivot = a[(lo + hi) / 2];
		a[(lo + hi) / 2] = a[hi];
		a[hi] = pivot;
		while(lo < hi)
		{
			while(a[lo].compareTo(pivot) <= 0 && lo < hi)
			{
				++lo;
			}
			while(pivot.compareTo(a[hi]) <= 0 && lo < hi)
			{
				--hi;
			}
			if(lo >= hi)
				continue;
			T e = a[lo];
			a[lo] = a[hi];
			a[hi] = e;
		}
		a[hi0] = a[hi];
		a[hi] = pivot;
		eqSort(a, lo0, lo - 1);
		eqSort(a, hi + 1, hi0);
	}
	
	public static <T extends Comparable<T>> void eqSort(T[] a)
	{
		eqSort(a, 0, a.length - 1);
	}
	
	private static <T> void eqBrute(T[] a, int lo, int hi, Comparator<T> c)
	{
		if(hi - lo == 1)
		{
			if(c.compare(a[hi], a[lo]) < 0)
			{
				T e = a[lo];
				a[lo] = a[hi];
				a[hi] = e;
			}
		}
		else if(hi - lo == 2)
		{
			int pmin = c.compare(a[lo], a[lo + 1]) < 0 ? lo : lo + 1;
			int n = pmin = c.compare(a[pmin], a[lo + 2]) < 0 ? pmin : lo + 2;
			if(pmin != lo)
			{
				T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			eqBrute(a, lo + 1, hi, c);
		}
		else if(hi - lo == 3)
		{
			int pmin = c.compare(a[lo], a[lo + 1]) < 0 ? lo : lo + 1;
			pmin = c.compare(a[pmin], a[lo + 2]) < 0 ? pmin : lo + 2;
			int n = pmin = c.compare(a[pmin], a[lo + 3]) < 0 ? pmin : lo + 3;
			if(pmin != lo)
			{
				T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			int pmax = c.compare(a[hi], a[hi - 1]) > 0 ? hi : hi - 1;
			int n2 = pmax = c.compare(a[pmax], a[hi - 2]) > 0 ? pmax : hi - 2;
			if(pmax != hi)
			{
				T e = a[hi];
				a[hi] = a[pmax];
				a[pmax] = e;
			}
			eqBrute(a, lo + 1, hi - 1, c);
		}
	}
	
	private static <T> void eqSort(T[] a, int lo0, int hi0, Comparator<T> c)
	{
		int hi = hi0;
		int lo = lo0;
		if(hi - lo <= 3)
		{
			eqBrute(a, lo, hi, c);
			return;
		}
		T pivot = a[(lo + hi) / 2];
		a[(lo + hi) / 2] = a[hi];
		a[hi] = pivot;
		while(lo < hi)
		{
			while(c.compare(a[lo], pivot) <= 0 && lo < hi)
			{
				++lo;
			}
			while(c.compare(pivot, a[hi]) <= 0 && lo < hi)
			{
				--hi;
			}
			if(lo >= hi)
				continue;
			T e = a[lo];
			a[lo] = a[hi];
			a[hi] = e;
		}
		a[hi0] = a[hi];
		a[hi] = pivot;
		eqSort(a, lo0, lo - 1, c);
		eqSort(a, hi + 1, hi0, c);
	}
	
	public static <T> void eqSort(T[] a, Comparator<T> c)
	{
		eqSort(a, 0, a.length - 1, c);
	}
	
	public static int[] toArray(Collection<Integer> collection)
	{
		int[] ar = new int[collection.size()];
		int i = 0;
		for(Integer t : collection)
		{
			ar[i++] = t;
		}
		return ar;
	}
	
	public static int[] createAscendingArray(int min, int max)
	{
		int length = max - min;
		int[] array = new int[length + 1];
		int x = 0;
		int i = min;
		while(i <= max)
		{
			array[x] = i++;
			++x;
		}
		return array;
	}
}