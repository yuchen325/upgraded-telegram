package rtree;

import java.io.Serializable;

/**
 * @ClassName Point 
 * @Description nά�ռ��еĵ㣬���е�ά�ȱ��洢��һ��float������
 */
public class Point implements Cloneable, Serializable
{
	private static final long serialVersionUID = 3810775231553230680L;
	
	private double[] data; // ��Ա����1,������洢����ֵ

	/**
	 * ���췽��1
	 * ����һ������ֵ����,�㹹��һ����
	 * @param data
	 */
	public Point(double[] data)
	{
		if(data == null)
		{
			throw new IllegalArgumentException("Coordinates cannot be null.");
		}
		if(data.length < 2)
		{
			throw new IllegalArgumentException("Point dimension should be greater than 1.");
		}
		
		this.data = new double[data.length];

		// ���鸴��,ֵ��ͬ��ַ��ͬ
		System.arraycopy(data, 0, this.data, 0, data.length);
	}
	
	public Point(int[] data)
	{
		if(data == null)
		{
			throw new IllegalArgumentException("Coordinates cannot be null.");
		}
		if(data.length < 2)
		{
			throw new IllegalArgumentException("Point dimension should be greater than 1.");
		}
		
		this.data = new double[data.length];
		for(int i = 0 ; i < data.length ; i ++)
		{
			this.data[i] = data[i];
		}
	}
	
	@Override
	public Object clone()
	{
		double[] copy = new double[data.length];
		System.arraycopy(data, 0, copy, 0, data.length);
		return new Point(copy);
	}
	
	@Override
	public String toString() 
	{
		StringBuffer sBuffer = new StringBuffer("(");
		
		for(int i = 0 ; i < data.length - 1 ; i ++)
		{
			sBuffer.append(data[i]).append(",");
		}
		
		sBuffer.append(data[data.length - 1]).append(")");
		
		return sBuffer.toString();
	}
	
	public static void main(String[] args) 
	{
		double[] test = {1.2f,2f,34f};
		Point point1 = new Point(test);
		System.out.println(point1);
		
		int[] test2 = {1,2,3,4};
		point1 = new Point(test2);
		System.out.println(point1);
	}

	/**
	 * @return ����Point��ά��
	 */
	public int getDimension() 
	{
		return data.length;
	}

	/**
	 * @param index
	 * @return ����Point�����iλ��floatֵ
	 */
	public double getDoubleCoordinate(int index)
	{
		return data[index];
	}
	
	/**
	 * @param index
	 * @return ����Point�����iλ��intֵ
	 */
	public int getIntCoordinate(int index)
	{
		return (int) data[index];
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(obj instanceof Point)
		{
			Point point = (Point) obj;
			
			if(point.getDimension() != getDimension())
				throw new IllegalArgumentException("Points must be of equal dimensions to be compared.");
			
			for(int i = 0; i < getDimension(); i ++)
			{
				if(getDoubleCoordinate(i) != point.getDoubleCoordinate(i))
					return false;
			}
		}
		
		if(! (obj instanceof Point))
			return false;
		
		return true;
	}
}
