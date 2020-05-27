package rtree;

import java.io.*;
import java.util.ArrayList;

import static filer.FileStandard.readnodes_cfw;

public class GenerateTreeFromFile
{

    /**
     * ��ȡָ��·���ļ�,����һ��2D-RTree
     * @param filePath ·���ļ�·��,��Ҫ����Ϊ�����ʽ
     * @return һ��2D-RTree
     */
    public static RTree generateRTreeFromCFW_output(String filePath)
    {
        RTree tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC);  //��ʼ��һ����

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

        // ���л�
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:\\stuff\\tree2"));
        oos.writeObject(tree);
        oos.close();

        //�����л�
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D:\\stuff\\tree2"));
        RTree tree1 = (RTree)ois.readObject();
        ois.close();

        System.out.println("the level of regenerate root is "+tree1.getTreeLevel());

    }
}
