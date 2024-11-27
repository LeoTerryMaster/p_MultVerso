package l2.commons.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Comparator;

public class RandomUtils
{
	public static final Comparator<Pair<?, Double>> DOUBLE_GROUP_COMPARATOR = new Comparator<Pair<?, Double>>()
	{
		@Override
		public int compare(Pair<?, Double> o1, Pair<?, Double> o2)
		{
			double v = o1.getRight() - o2.getRight();
			return v > 0.0 ? 1 : v < 0.0 ? -1 : 0;
		}
	};
	
	public static <G> G pickRandomSortedGroup(Collection<Pair<G, Double>> sortedGroups, double total)
	{
		double r = total * Rnd.get();
		double share = 0.0;
		for(Pair<G, Double> group : sortedGroups)
		{
			if(r > (share += group.getRight().doubleValue()))
				continue;
			return group.getLeft();
		}
		return null;
	}
}