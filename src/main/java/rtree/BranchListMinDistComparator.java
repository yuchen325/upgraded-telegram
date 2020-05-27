package rtree;

import java.util.Comparator;

// 一个比较器,用于比较两个BranchList的大小
public class BranchListMinDistComparator implements Comparator<BranchList>
{

	@Override
	public int compare(BranchList o1, BranchList o2) 
	{
		double f = o1.minDist - o2.minDist;
		if(f > 0)
			return 1;
		else if(f < 0)
			return -1;
		return 0;
	}

}
