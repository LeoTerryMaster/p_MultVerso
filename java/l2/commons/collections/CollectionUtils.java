package l2.commons.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class CollectionUtils
{
	private CollectionUtils()
	{
	}
	
	private static <T extends Comparable<T>> void eqBrute(List<T> list, int lo, int hi)
	{
		if((hi - lo) == 1)
		{
			if(list.get(hi).compareTo(list.get(lo)) < 0)
			{
				T e = list.get(lo);
				list.set(lo, list.get(hi));
				list.set(hi, e);
			}
		}
		else if((hi - lo) == 2)
		{
			int pmin = list.get(lo).compareTo(list.get(lo + 1)) < 0 ? lo : lo + 1;
			pmin = list.get(pmin).compareTo(list.get(lo + 2)) < 0 ? pmin : lo + 2;
			if(pmin != lo)
			{
				T e = list.get(lo);
				list.set(lo, list.get(pmin));
				list.set(pmin, e);
			}
			eqBrute(list, lo + 1, hi);
		}
		else if((hi - lo) == 3)
		{
			int pmin = list.get(lo).compareTo(list.get(lo + 1)) < 0 ? lo : lo + 1;
			pmin = list.get(pmin).compareTo(list.get(lo + 2)) < 0 ? pmin : lo + 2;
			pmin = list.get(pmin).compareTo(list.get(lo + 3)) < 0 ? pmin : lo + 3;
			if(pmin != lo)
			{
				T e = list.get(lo);
				list.set(lo, list.get(pmin));
				list.set(pmin, e);
			}
			int pmax = list.get(hi).compareTo(list.get(hi - 1)) > 0 ? hi : hi - 1;
			pmax = list.get(pmax).compareTo(list.get(hi - 2)) > 0 ? pmax : hi - 2;
			if(pmax != hi)
			{
				T e = list.get(hi);
				list.set(hi, list.get(pmax));
				list.set(pmax, e);
			}
			eqBrute(list, lo + 1, hi - 1);
		}
	}
	
	private static <T extends Comparable<T>> void eqSort(List<T> list, int lo0, int hi0)
	{
		int lo = lo0;
		int hi = hi0;
		if((hi - lo) <= 3)
		{
			eqBrute(list, lo, hi);
			return;
		}
		T pivot = list.get((lo + hi) / 2);
		list.set((lo + hi) / 2, list.get(hi));
		list.set(hi, pivot);
		while(lo < hi)
		{
			while(list.get(lo).compareTo(pivot) <= 0 && lo < hi)
			{
				lo++;
			}
			while(pivot.compareTo(list.get(hi)) <= 0 && lo < hi)
			{
				hi--;
			}
			if(lo < hi)
			{
				T e = list.get(lo);
				list.set(lo, list.get(hi));
				list.set(hi, e);
			}
		}
		list.set(hi0, list.get(hi));
		list.set(hi, pivot);
		eqSort(list, lo0, lo - 1);
		eqSort(list, hi + 1, hi0);
	}
	
	public static <T extends Comparable<T>> void eqSort(List<T> list)
	{
		eqSort(list, 0, list.size() - 1);
	}
	
	private static <T> void eqBrute(List<T> list, int lo, int hi, Comparator<? super T> c)
	{
		if((hi - lo) == 1)
		{
			if(c.compare(list.get(hi), list.get(lo)) < 0)
			{
				T e = list.get(lo);
				list.set(lo, list.get(hi));
				list.set(hi, e);
			}
		}
		else if((hi - lo) == 2)
		{
			int pmin = c.compare(list.get(lo), list.get(lo + 1)) < 0 ? lo : lo + 1;
			pmin = c.compare(list.get(pmin), list.get(lo + 2)) < 0 ? pmin : lo + 2;
			if(pmin != lo)
			{
				T e = list.get(lo);
				list.set(lo, list.get(pmin));
				list.set(pmin, e);
			}
			eqBrute(list, lo + 1, hi, c);
		}
		else if((hi - lo) == 3)
		{
			int pmin = c.compare(list.get(lo), list.get(lo + 1)) < 0 ? lo : lo + 1;
			pmin = c.compare(list.get(pmin), list.get(lo + 2)) < 0 ? pmin : lo + 2;
			pmin = c.compare(list.get(pmin), list.get(lo + 3)) < 0 ? pmin : lo + 3;
			if(pmin != lo)
			{
				T e = list.get(lo);
				list.set(lo, list.get(pmin));
				list.set(pmin, e);
			}
			int pmax = c.compare(list.get(hi), list.get(hi - 1)) > 0 ? hi : hi - 1;
			pmax = c.compare(list.get(pmax), list.get(hi - 2)) > 0 ? pmax : hi - 2;
			if(pmax != hi)
			{
				T e = list.get(hi);
				list.set(hi, list.get(pmax));
				list.set(pmax, e);
			}
			eqBrute(list, lo + 1, hi - 1, c);
		}
	}
	
	private static <T> void eqSort(List<T> list, int lo0, int hi0, Comparator<? super T> c)
	{
		int lo = lo0;
		int hi = hi0;
		if((hi - lo) <= 3)
		{
			eqBrute(list, lo, hi, c);
			return;
		}
		T pivot = list.get((lo + hi) / 2);
		list.set((lo + hi) / 2, list.get(hi));
		list.set(hi, pivot);
		while(lo < hi)
		{
			while(c.compare(list.get(lo), pivot) <= 0 && lo < hi)
			{
				lo++;
			}
			while(c.compare(pivot, list.get(hi)) <= 0 && lo < hi)
			{
				hi--;
			}
			if(lo < hi)
			{
				T e = list.get(lo);
				list.set(lo, list.get(hi));
				list.set(hi, e);
			}
		}
		list.set(hi0, list.get(hi));
		list.set(hi, pivot);
		eqSort(list, lo0, lo - 1, c);
		eqSort(list, hi + 1, hi0, c);
	}
	
	public static <T> void eqSort(List<T> list, Comparator<? super T> c)
	{
		eqSort(list, 0, list.size() - 1, c);
	}
	
	public static <T extends Comparable<T>> void insertionSort(List<T> list)
	{
		for(int i = 1;i < list.size();i++)
		{
			int j = i;
			T A;
			T B = list.get(i);
			while((j > 0) && ((A = list.get(j - 1)).compareTo(B) > 0))
			{
				list.set(j, A);
				j--;
			}
			list.set(j, B);
		}
	}
	
	public static <T> void insertionSort(List<T> list, Comparator<? super T> c)
	{
		for(int i = 1;i < list.size();i++)
		{
			int j = i;
			T A;
			T B = list.get(i);
			while((j > 0) && (c.compare(A = list.get(j - 1), B) > 0))
			{
				list.set(j, A);
				j--;
			}
			list.set(j, B);
		}
	}
	
	public static <E> int hashCode(Collection<E> collection)
	{
		int hashCode = 1;
		Iterator<E> i = collection.iterator();
		while(i.hasNext())
		{
			E obj = i.next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}
	
	public static <E> E safeGet(List<E> list, int index)
	{
		return list.size() > index ? list.get(index) : null;
	}
}