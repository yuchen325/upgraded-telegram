package rtree;

// �ڵ�ķ�֧�б�?
public class BranchList 
{
	public RTNode node; // һ��R-Tree�ڵ�
	
	/**
	 * ��Point��node����Χ��Rectangle����С������
	 * ��̫���
	 */
	public double minMaxDist;

	/**
	 * ��Point��node����Χ��Rectangle����С����
	 * ��̫���
	 */
	public double minDist;
	
//	public BranchList(){
//		
//	}

	// ���췽��
	public BranchList(RTNode node, double minDist, double minMaxDist)
	{
		this.node = node;
		this.minDist = minDist;
		this.minMaxDist = minMaxDist;
	}
	
	
}
