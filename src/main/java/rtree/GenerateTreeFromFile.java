package rtree;

import java.io.*;
import java.util.ArrayList;

import static filer.FileStandard.readnodes_cfw;

public class GenerateTreeFromFile
{

    /**
     * 读取指定路网文件,返回一棵2D-RTree
     * @param filePath 路网文件路径,需要整理为发哥格式
     * @return 一棵2D-RTree
     */
    public static RTree generateRTreeFromCFW_output(String filePath)
    {
        RTree tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC);  //初始化一颗树

        ArrayList<Rectangle> MBRs = readnodes_cfw(filePath);

        for (Rectangle rectangle:MBRs)
        {
            //System.out.println("insert " + MBRs.indexOf(rectangle)+ "th" + rectangle.toString());
            tree.insert(rectangle, -2);
        }

        System.out.println("your 2D-RTree is ready.");

        return tree;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        RTree tree = generateRTreeFromCFW_output("D:\\daochu.txt");

        System.out.println("the level of original root is "+tree.getTreeLevel());

        // 序列化
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:\\stuff\\tree2"));
        oos.writeObject(tree);
        oos.close();

        //反序列化
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D:\\stuff\\tree2"));
        RTree tree1 = (RTree)ois.readObject();
        ois.close();

        System.out.println("the level of regenerate root is "+tree1.getTreeLevel());

    }
}
