package rtree;

public class Data 
{
	public Rectangle mbr;// ��Ա����1,һ��MBR����

	/**
	 * ��Ա����2
	 * ����ڵ�
	 */
	public RTNode parent;

	/**
	 * ��Ա����3
	 * �ڽ���е�λ��
	 */
	public int position;

	/**
	 * ��Ա����4
	 * �Ȳ���
	 */
	public double minDist;

	/**
	 * ���췽��1
	 * @param mbr
	 * @param minDist
	 * @param position
	 */
	public Data(Rectangle mbr, double minDist, int position)
	{
		this.mbr = mbr;
		this.minDist = minDist;
		this.position = position;
	}

	/**
	 * ���췽��2
	 * @param mbr
	 * @param position
	 */
	public Data(Rectangle mbr, int position) 
	{
		this.mbr = mbr;
		this.position = position;
	}

	/**
	 * ���췽��3
	 * @param mbr
	 * @param parent
	 * @param position
	 */
	public Data(Rectangle mbr, RTNode parent, int position) 
	{
		this.mbr = mbr;
		this.parent = parent;
		this.position = position;
	}

	/**
	 * ��Ա����
	 * toString,������������
	 * @return
	 */
	@Override
	public String toString() 
	{
		return mbr.toString() + "-->Position:" + position + " -->minDist:" + minDist + "\n";
	}
}
