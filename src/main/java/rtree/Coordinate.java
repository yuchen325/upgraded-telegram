package rtree;

// 用于描述坐标
public class Coordinate
{
	
	//针对高维坐标点
	/**
	 * Rectangle中心点的坐标
	 */
	private int[] coordinate; //用于记录坐标值
	
	/**
	 * 维度默认为2
	 */
	private int dimension = 2; // 维度
	
	/**
	 * 构造方法
	 * 针对高维坐标点
	 * @param coord // 所有坐标值
	 * @param hilbert // ?
	 */
	public Coordinate(int[] coord, long hilbert)
	{
		int dim = coord.length; // 记录坐标维数
		if (dim < 2)  // 如果维度小于2,抛出异常
		{
			throw new RuntimeException("坐标维度小于2！");
		}
		coordinate = coord;
		this.dimension = dim;
	}
	
	public int[] getCoordinate() {
		return coordinate;
	} // 由于coordinates是一个private成员变量,所以这里写了一个getter方法

	public int getDimension() {
		return dimension;
	} // 同上,dimension是一个private变量,这也是一个getter方法

	/**
	 * 成员方法
	 * @return
	 */
	@Override
	public String toString()  // 重载toString方法,toString方法适用于描述对象
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < dimension; i++)
		{
			sb.append(coordinate[i]);
			sb.append(", ");
		}
		int index = sb.lastIndexOf(", ");
		sb.replace(index, index + 2, "");
		sb.append(")");
		return sb.toString();
	}


	public static void main(String[] args) {
		
	}
	
}
