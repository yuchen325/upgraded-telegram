package rtree;

// ������������
public class Coordinate
{
	
	//��Ը�ά�����
	/**
	 * Rectangle���ĵ������
	 */
	private int[] coordinate; //���ڼ�¼����ֵ
	
	/**
	 * ά��Ĭ��Ϊ2
	 */
	private int dimension = 2; // ά��
	
	/**
	 * ���췽��
	 * ��Ը�ά�����
	 * @param coord // ��������ֵ
	 * @param hilbert // ?
	 */
	public Coordinate(int[] coord, long hilbert)
	{
		int dim = coord.length; // ��¼����ά��
		if (dim < 2)  // ���ά��С��2,�׳��쳣
		{
			throw new RuntimeException("����ά��С��2��");
		}
		coordinate = coord;
		this.dimension = dim;
	}
	
	public int[] getCoordinate() {
		return coordinate;
	} // ����coordinates��һ��private��Ա����,��������д��һ��getter����

	public int getDimension() {
		return dimension;
	} // ͬ��,dimension��һ��private����,��Ҳ��һ��getter����

	/**
	 * ��Ա����
	 * @return
	 */
	@Override
	public String toString()  // ����toString����,toString������������������
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
