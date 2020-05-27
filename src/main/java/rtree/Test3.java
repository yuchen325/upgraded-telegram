package rtree;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Test3 
{
	public static void main(String[] args) throws Exception 
	{

		CachedPersistentPageFile file = new CachedPersistentPageFile("D:\\RTree_work\\rtree.dat", 10);

		//����һ���յ�R-tree
		RTree tree = new RTree(2, 0.4f, 5, file, Constants.RTREE_QUADRATIC);

//		RTree tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC);

		//LB���ݼ���ʽ���£�
		//һ��:ID lx(���½�longitude) ly(���½�latitude) hx(���Ͻ�longitude) hy(���Ͻ�latitude)
		BufferedReader reader = new BufferedReader(new FileReader(new File("D:\\RTree_work\\LB.txt")));
		String line ;
		int j = 0;
		
		Map<Integer, String> description = new HashMap<Integer, String>(); // һ��<�ļ����,�ļ�����>��ֵ����ɵ�����?

		// ������������Ҫ����?
		File _20news = new File("D:\\RTree_work\\dataset\\20news"); // ?
		File[] listFiles = _20news.listFiles();

		for(int i = 0; i < listFiles.length; i ++)
		{
			description.put(i + 1, readFromFile(listFiles[i]));  // ��������<�ļ����,�ļ�����>����description
		}
		
		int size = description.size();  // ��¼�ļ�����?

		Random random = new Random(size); //
		
		while((line = reader.readLine()) != null)
		{
			String[] splits = line.split(" ");

			// ��ȡ���½�����ֵ
			double lx = Double.parseDouble(splits[1]);
			double ly = Double.parseDouble(splits[2]);

			// ��ȡ���Ͻ�����ֵ
			double hx = Double.parseDouble(splits[3]);
			double hy = Double.parseDouble(splits[4]);
			
			Point p1 = new Point(new double[]{lx, ly}); // ���½�
			Point p2 = new Point(new double[]{hx, hy}); // ���Ͻ�

			String desc = description.get(random.nextInt(size));

			while(desc == null)
			{
				desc = description.get(random.nextInt(size));
			}
			
//			final Rectangle rectangle = new Rectangle(p1, p2);
			final Rectangle rectangle = new Rectangle(p1, p2, desc);
			System.out.println("insert " + j + "th " + rectangle + "......");

			tree.insert(rectangle, -2);
			j++;
		}
		
		System.out.println(tree.file.readNode(0));
		
		
		//ɾ�����
//		System.out.println("---------------------------------");
//		System.out.println("Begin delete.");
//		
//		reader = new BufferedReader(new FileReader(new File("d:\\RTree_work\\LB.txt")));
//		while((line = reader.readLine()) != null)
//		{
//			String[] splits = line.split(" ");
//			float lx = Float.parseFloat(splits[1]);
//			float ly = Float.parseFloat(splits[2]);
//			float hx = Float.parseFloat(splits[3]);
//			float hy = Float.parseFloat(splits[4]);
//			
//			Point p1 = new Point(new float[]{lx,ly});
//			Point p2 = new Point(new float[]{hx,hy});
//			
//			final Rectangle rectangle = new Rectangle(p1, p2);
//			tree.delete(rectangle);
//		}
//		
//		reader.close();
//		System.out.println(tree.file.readNode(0));
//		System.out.println("---------------------------------");
//		System.out.println("Delete finished.");
	}
	
	public static String readFromFile(final File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader
				(new FileInputStream(file)));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null)
		{
			sb.append(line).append("\n");
		}
		
		reader.close();
		
		return sb.toString();
	}
}
