package rtree;

import entity.CarPoint;
import entity.MatchedPoint;
import entity.RoadPoint;
import filer.TrajectoryFromFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TransformToCSV
{
    /**
     * 这个方法将抓路矩形之内的候选路段的GPS点写到一个CSV文件中
     * @param filePath 这是输出文件路径
     * @param catchRectangle 抓路矩形
     * @param tree 2D-Rtree
     */
    public static void candidateRoadToCSV(String filePath, Rectangle catchRectangle, RTree tree)
    {
        RTNode root = tree.file.readNode(0);   // 获取2D-Rtree的root节点

        ArrayList<Data> candidateRoads = tree.intersection_Rectangles(catchRectangle,root);//获取候选路段矩形组成的列表

        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            String line = "";
            /* 写入Txt文件 */
            File writename = new File(filePath); // 相对路径，如果没有则要建立一个新的output。txt文件
            writename.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //遍历所有候选路段
            for (Data candidateRoad:candidateRoads)
            {
                Rectangle candidateRectangle = candidateRoad.mbr;   //提取这条候选路段的MBR最小外包矩形

                ArrayList<RoadPoint> road_points = candidateRectangle.getRoad(); //取出该路段的所有轨迹点

                for(RoadPoint roadpoint:road_points)      //遍历这条路中的所有点
                {
                    out.write(roadpoint.toString()+"\n");
                }
            }

            out.close(); // 最后记得关闭文件

            } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 这个方法将车辆轨迹中的GPS点写到一个CSV文件中
     * @param filePath 输出文件路径
     * @param trajectory 车辆轨迹
     */
    public static void trajectoryToCSV_beforeMatching(String filePath, ArrayList<CarPoint> trajectory)
    {
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            String line = "";
            /* 写入Txt文件 */
            File writename = new File(filePath); // 相对路径，如果没有则要建立一个新的output。txt文件
            writename.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //遍历所有候选路段
            for (CarPoint carPoint:trajectory)
            {
                out.write(carPoint.toString()+"\n");
            }

            out.close(); // 最后记得关闭文件

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*public static void trajectoryToCSV_afterMatching(String filePath, ArrayList<RoadPoint> trajectory)
    {
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            String line = "";

            File writename = new File(filePath); // 相对路径，如果没有则要建立一个新的output。txt文件
            writename.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //遍历所有候选路段
            for (RoadPoint roadPoint:trajectory)
            {
                out.write(roadPoint.toString()+"\n");
            }

            out.close(); // 最后记得关闭文件

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/

    public static void trajectoryToCSV_afterMatching(String filePath, ArrayList<MatchedPoint> trajectory)
    {
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            String line = "";
            /* 写入Txt文件 */
            File writename = new File(filePath); // 相对路径，如果没有则要建立一个新的output。txt文件
            writename.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));

            //遍历所有候选路段
            for (MatchedPoint roadPoint:trajectory)
            {
                out.write(roadPoint.toString()+"\n");
            }

            out.close(); // 最后记得关闭文件

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        ArrayList<ArrayList<CarPoint>> trajectories_raw = TrajectoryFromFile.multipleTrajectoriesFromFile(
                "D:\\data-gps\\taxi_gps\\2017-03-07.gz\\part-m-00000\\part-m-00000");

        System.out.println("这里面有"+trajectories_raw.size()+"辆车的轨迹");

        System.out.println("下面我来看看一条轨迹的内容,里面的轨迹点是按照时间戳排序的");

        ArrayList<CarPoint> trajectory = trajectories_raw.get(0);

        for(CarPoint c:trajectory)
            System.out.println(c.toString());

    }
}
