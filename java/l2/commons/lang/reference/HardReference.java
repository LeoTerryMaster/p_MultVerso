package l2.commons.lang.reference;

public interface HardReference<T>
{
	T get();
	
	void clear();
}