package rtree;

import entity.RoadPoint;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * �������<p>
 * ʵ��Cloneable��Comparable�ӿڣ����Ը���Hilbertֵ���бȽ�����
 * @ClassName Rectangle 
 * @Description 
 */
public class Rectangle implements Cloneable, Serializable
{

	private static final long serialVersionUID = -3784504003866244090L;
	/**
	 * ���½�����
	 */
	private Point low;
	
	/**
	 * ���Ͻ�����
	 */
	private Point high;
	
	/**
	 * ���������������ؼ���
	 */
	private String description;

	/**
	 * �������һ��MBR����һ��·,�������������˵��ǲ�����
	 * ���������Ա�������ڴ洢��·�ϵĵ�
	 * ���ָ߼����͵ĳ�Ա����,setter��getter�����������Ԫ�ؿ�¡����,����������͸���������
	 */
	private ArrayList<RoadPoint> road;

	public  ArrayList<RoadPoint> getRoad()
	{
		return (ArrayList<RoadPoint>) road.clone();
	}

	// ���췽��
	public Rectangle(Point p1, Point p2, ArrayList<RoadPoint> roadpoints)
	{
		if(p1 == null || p2 == null)
		{
			throw new IllegalArgumentException("Points cannot be null.");
		}
		if(p1.getDimension() != p2.getDimension())
		{
			throw new IllegalArgumentException("Points must be of same dimension.");
		}
		//check the points
		for(int i = 0; i < p1.getDimension(); i ++)
		{
			if(p1.getDoubleCoordinate(i) > p2.getDoubleCoordinate(i))
			{
				throw new IllegalArgumentException("�����Ϊ�����½Ǻ����Ͻ�");
			}
		}
		low = (Point) p1.clone();
		high = (Point) p2.clone();

		this.road = (ArrayList<RoadPoint>) roadpoints.clone();

	}
	
	public Rectangle(Point p1, Point p2)
	{
		if(p1 == null || p2 == null)
		{
			throw new IllegalArgumentException("Points cannot be null.");
		}
		if(p1.getDimension() != p2.getDimension())
		{
			throw new IllegalArgumentException("Points must be of same dimension.");
		}
		/*�����½Ǻ����Ͻ�
		for(int i = 0; i < p1.getDimension(); i ++)
		{
			if(p1.getDoubleCoordinate(i) > p2.getDoubleCoordinate(i))
			{
				throw new IllegalArgumentException("�����Ϊ�����½Ǻ����Ͻ�");
			}
		}*/
		low = (Point) p1.clone();
		high = (Point) p2.clone();
		
	}
	
	public Rectangle(Point p1, Point p2, String description)
	{
		this(p1, p2);
		this.description = description;
	}

	/**
	 * ����Rectangle���½ǵ�Point
	 * @return Point
	 */
	public Point getLow()
	{
		return (Point) low.clone();
	}

	/**
	 * ����Rectangle���Ͻǵ�Point
	 * @return Point
	 */
	public Point getHigh()
	{
		return high;
	}
	
	/**
	 * @param rectangle
	 * @return ��Χ����Rectangle����СRectangle
	 */
	public Rectangle getUnionRectangle(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
		{
			throw new IllegalArgumentException("Rectangle must be of same dimension.");
		}
		
		double[] min = new double[getDimension()];
		double[] max = new double[getDimension()];
		
		for(int i = 0; i < getDimension(); i++)
		{
			min[i] = Math.min(low.getDoubleCoordinate(i), rectangle.low.getDoubleCoordinate(i));
			max[i] = Math.max(high.getDoubleCoordinate(i), rectangle.high.getDoubleCoordinate(i));
		}
		
		Rectangle ret = new Rectangle(new Point(min), new Point(max));
//		ret.hilbertValue = ((hilbertValue >= rectangle.hilbertValue) ? hilbertValue : rectangle.hilbertValue);
		
		return ret;
	}
	
	/**
	 * @return ����Rectangle�����
	 */
	public float getArea()
	{
		float area = 1;
		for(int i = 0; i < getDimension(); i ++)
		{
			area *= high.getDoubleCoordinate(i) - low.getDoubleCoordinate(i);
		}
		
		return area;
	}
	
	/**
	 * @param rectangles
	 * @return ��Χһϵ��Rectangle����СRectangle
	 */
	public static Rectangle getUnionRectangle(Rectangle[] rectangles)
	{
		if(rectangles == null || rectangles.length == 0)
			throw new IllegalArgumentException("Rectangle array is empty.");
		
		Rectangle r0 = (Rectangle) rectangles[0].clone();
		for(int i = 1; i < rectangles.length; i ++)
		{
			r0 = r0.getUnionRectangle(rectangles[i]);
		}
		
		return r0;
	}
	
	@Override
	protected Object clone()
	{
		Point p1 = (Point) low.clone();
		Point p2 = (Point) high.clone();
		return new Rectangle(p1, p2);
	}
	
	@Override
	public String toString() 
	{
//		if(description == null)
//			return "Rectangle Low:" + low + "\tHigh:" + high + "\tHilbert value: " + hilbertValue + "\n";
		return "Rectangle Low:" + low + "\tHigh:" + high  /* + "\tCenter: " + getCenter() + "\n"*/;
//		return "Rectangle Low:" + low + " High:" + high + "\n" + description ;
	}
	
	public String toStr() {
		if(description == null)
			return "Rectangle Low:" + low + "\tHigh:" + high ;
		return "Rectangle Low:" + low + "\tHigh:" + high  + "\n" + description ;
	}

	/**
	 * ����Rectangle�ཻ�����
	 * @param rectangle Rectangle
	 * @return float
	 */
	public double intersectingArea(Rectangle rectangle)
	{
		if(! isIntersection(rectangle))
		{
			return 0;
		}
		
		double ret = 1;
		for(int i = 0; i < rectangle.getDimension(); i ++)
		{
			double l1 = this.low.getDoubleCoordinate(i);
			double h1 = this.high.getDoubleCoordinate(i);
			double l2 = rectangle.low.getDoubleCoordinate(i);
			double h2 = rectangle.high.getDoubleCoordinate(i);
			
			//rectangle1��rectangle2�����
			if(l1 <= l2 && h1 <= h2)
			{
				ret *= (h1 - l1) - (l2 - l1);
			}else if(l1 >= l2 && h1 >= h2)
			//rectangle1��rectangle2���ұ�
			{
				ret *= (h2 - l2) - (l1 - l2);
			}else if(l1 >= l2 && h1 <= h2)			
			//rectangle1��rectangle2����
			{
				ret *= h1 - l1;
			}else if(l1 <= l2 && h1 >= h2)	
			//rectangle1����rectangle2
			{
				ret *= h2 - l2;
			}
		}
		return ret;
	}
	
	/**
	 * @param rectangle
	 * @return �ж�����Rectangle�Ƿ��ཻ
	 */
	public boolean isIntersection(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
		{
			throw new IllegalArgumentException("Rectangle cannot be null.");
		}
		
		
		for(int i = 0; i < getDimension(); i ++)
		{
			if(low.getDoubleCoordinate(i) > rectangle.high.getDoubleCoordinate(i) ||
					high.getDoubleCoordinate(i) < rectangle.low.getDoubleCoordinate(i))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @return ����Rectangle��ά��
	 */
	public int getDimension() 
	{
		return low.getDimension();
	}

	/**
	 * �ж�rectangle�Ƿ񱻰�Χ
	 * @param rectangle
	 * @return
	 */
	public boolean enclosure(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
			throw new IllegalArgumentException("Rectangle dimension is different from current dimension.");
		
		for(int i = 0; i < getDimension(); i ++)
		{
			if(rectangle.low.getDoubleCoordinate(i) < low.getDoubleCoordinate(i) ||
					rectangle.high.getDoubleCoordinate(i) > high.getDoubleCoordinate(i))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(obj instanceof Rectangle)
		{
			Rectangle rectangle = (Rectangle) obj;
			if(low.equals(rectangle.getLow()) && high.equals(rectangle.getHigh()))
				return true;
		}
		return false;
	}

	/**
	 * MINDIST����<p>
	 * 1.��ѯ��p��R�ڻ�R�ı߽�����MINDIST=0<br>
     * 2.��ѯ��p��R�⣬����̾�����루p��R�ıߣ����ڣ���MINDIST=p��R�ıߵ���̾��룬 ����MINDIST=p��R�Ķ���������̾��롣<p>
	 * MINDIST(P,R):  the shortest distance from P to R
	 * @param point
	 * @return ����Point��Rectangle����С���룬��MINDIST����
	 */
	public double getMinDist(Point point)
	{
		if(point == null)
			throw new IllegalArgumentException("Point cannot be null.");
		if(point.getDimension() != getDimension())
			throw new IllegalArgumentException("Point dimension is different from Rectangle dimension.");
		
		double ret = 0;
		for(int i = 0; i < getDimension(); i ++)
		{
			double p = point.getDoubleCoordinate(i);
			double l = low.getDoubleCoordinate(i);
			double h = high.getDoubleCoordinate(i);
			double r;
			
			if(p < l)
				r = l;
			else if(p > h)
				r = h;
			else 
				r = p;
			
			ret += Math.pow(Math.abs(p - r), 2);
		}
		
		return ret;
		
	}
	
	/**
	 * MINMAXDIST����<p>
	 * 1.�ҳ����k�ᴹֱ�Ĳ������ѯ��p������棬��ΪH<br>
     * 2.ѡ��Ӳ�ѯ��p����H�о�����Զ���Ǹ��㣬��Ϊa<br>
     * 3.�����ѯ��p����a�ľ���,��Ϊdk<br>
     * 4.��ÿ���������ظ�����1-����3���Ǽ������þ���Ϊd1��d2��...dk<br>
     * 5.�����м������þ�����ѡ����С����һ������MINMAXDIST<p>
	 * MINMAXDIST(P,R)��the minimum over all dimensions distance from P  to the furthest point of the closest face of the R<p>
	 * The important of MINMAXDIST(P,M) is that it computes the smallest distance between point P
	 * and MBR M that gurantees the finding of an object in M at a Enclidean distance less than or equal to MINMAXDIST(P,M).
	 * @param point
	 * @return ����Point��Rectangle����С�����룬��MINMAXDIST����
	 */
	public double getMinMaxDist(Point point)
	{
		if(point == null)
			throw new IllegalArgumentException("Point cannot be null.");
		if(point.getDimension() != getDimension())
			throw new IllegalArgumentException("Point dimension is different from Rectangle dimension.");
		
		double ret = Float.POSITIVE_INFINITY;
		for(int k = 0; k < getDimension(); k ++)
		{
			double p = point.getDoubleCoordinate(k);
			double s = low.getDoubleCoordinate(k);
			double t = high.getDoubleCoordinate(k);
			double rm;
			
			if(p <= (s + t)/2.0)
				rm = s;
			else
				rm = t;
			
			double sum = 0;
			for(int i = 0; i < getDimension(); i ++)
			{
				double p_ = point.getDoubleCoordinate(i);
				double s_ = low.getDoubleCoordinate(i);
				double t_ = high.getDoubleCoordinate(i);
				
				if(i != k)
				{
					double rM;
					
					if(p_ >= (s_ + t_)/2.0)
						rM = s_;
					else
						rM = t_;
					
					sum += Math.pow(Math.abs(p_ - rM), 2);
				}
			}
			
			sum += Math.pow(Math.abs(p - rm), 2);
			
			if(sum < ret)
			{
				ret = sum;
			}
			
		}
		
		return ret;
	}
	public String describe()
	{

		return "th road has " +this.road.size()+" points,all points constitute a MBR";
	}

	public String describeCatchRectangle()
	{
		return "This is a catchRectangle.";
	}

	public static void main(String[] args) 
	{



		/*ArrayList<Integer> a1 = new ArrayList<Integer>();
		ArrayList<Integer> a2 = new ArrayList<Integer>();

		for(int i=0;i<10;i++)
			a1.add(i);

		a2=a1; //��ǳ����

		for (int i=100;i<110;i++)
			a2.add(i);

		for (int a:a1)
			System.out.println(a);

		ArrayList<Integer> a3 = (ArrayList<Integer>) a1.clone(); // ���ֿ�����ʽ����ʹa1,a2�������

		for (int i=100;i<110;i++)
			a3.add(i);

		for (int a:a1)
			System.out.println(a); */

		Point p = new Point(new double[]{3.5,3.5});

		ArrayList<Point> a1 = new ArrayList<Point>();
		ArrayList<Point> a2 = new ArrayList<Point>();

		//a2 = (ArrayList<Point>) a1.clone();// ���ֿ�����ʽ����ʹa1,a2�������

		// a2=a1; ��ǳ���� a1,a2ָ��ͬһ��ַ

		a1.add(p);

		System.out.println(a2.size());
	}

}
