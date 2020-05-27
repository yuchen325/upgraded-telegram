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

		//创建一个空的R-tree
		RTree tree = new RTree(2, 0.4f, 5, file, Constants.RTREE_QUADRATIC);

//		RTree tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC);

		//LB数据集格式如下：
		//一行:ID lx(左下角longitude) ly(左下角latitude) hx(右上角longitude) hy(右上角latitude)
		BufferedReader reader = new BufferedReader(new FileReader(new File("D:\\RTree_work\\LB.txt")));
		String line ;
		int j = 0;
		
		Map<Integer, String> description = new HashMap<Integer, String>(); // 一个<文件编号,文件内容>键值对组成的链表?

		// 下面这两行是要干嘛?
		File _20news = new File("D:\\RTree_work\\dataset\\20news"); // ?
		File[] listFiles = _20news.listFiles();

		for(int i = 0; i < listFiles.length; i ++)
		{
			description.put(i + 1, readFromFile(listFiles[i]));  // 将读到的<文件编号,文件内容>加入description
		}
		
		int size = description.size();  // 记录文件个数?

		Random random = new Random(size); //
		
		while((line = reader.readLine()) != null)
		{
			String[] splits = line.split(" ");

			// 读取左下角坐标值
			double lx = Double.parseDouble(splits[1]);
			double ly = Double.parseDouble(splits[2]);

			// 读取右上角坐标值
			double hx = Double.parseDouble(splits[3]);
			double hy = Double.parseDouble(splits[4]);
			
			Point p1 = new Point(new double[]{lx, ly}); // 左下角
			Point p2 = new Point(new double[]{hx, hy}); // 右上角

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
		
		
		//删除结点
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
