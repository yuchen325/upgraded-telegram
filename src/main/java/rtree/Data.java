package rtree;

public class Data 
{
	public Rectangle mbr;// 成员变量1,一个MBR矩形

	/**
	 * 成员变量2
	 * 妈妈节点
	 */
	public RTNode parent;

	/**
	 * 成员变量3
	 * 在结点中的位置
	 */
	public int position;

	/**
	 * 成员变量4
	 * 先不管
	 */
	public double minDist;

	/**
	 * 构造方法1
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
	 * 构造方法2
	 * @param mbr
	 * @param position
	 */
	public Data(Rectangle mbr, int position) 
	{
		this.mbr = mbr;
		this.position = position;
	}

	/**
	 * 构造方法3
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
	 * 成员方法
	 * toString,用于描述对象
	 * @return
	 */
	@Override
	public String toString() 
	{
		return mbr.toString() + "-->Position:" + position + " -->minDist:" + minDist + "\n";
	}
}
