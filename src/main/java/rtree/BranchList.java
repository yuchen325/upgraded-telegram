package rtree;

// 节点的分支列表?
public class BranchList 
{
	public RTNode node; // 一个R-Tree节点
	
	/**
	 * 点Point到node所包围的Rectangle的最小最大距离
	 * 不太清楚
	 */
	public double minMaxDist;

	/**
	 * 点Point到node所包围的Rectangle的最小距离
	 * 不太清楚
	 */
	public double minDist;
	
//	public BranchList(){
//		
//	}

	// 构造方法
	public BranchList(RTNode node, double minDist, double minMaxDist)
	{
		this.node = node;
		this.minDist = minDist;
		this.minMaxDist = minMaxDist;
	}
	
	
}
