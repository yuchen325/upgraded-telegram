package rtree;

import entity.CarPoint;
import entity.RoadPoint;
import entity.MatchedPoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;

import static calculator.Calculator.getCatchRectangle;
import static calculator.Calculator.getDistance;

public class GetRoads
{

    private static final long serialVersionUID = 1362045661105216022L;
    /**
     *
     * @param car_point 一个行车轨迹点
     * @param range     range是抓路矩形的1/2倍边长,单位:m
     * @param tree      2D-RTree
     * @return  由路段候选点组成的集合

    public static ArrayList<RoadPoint> getCandidates(CarPoint car_point, double range, RTree tree)
    {
        ArrayList<RoadPoint> candidates = new ArrayList<RoadPoint>(); //用于记录路段候选点

        Rectangle catchRectangle = getCatchRectangle(car_point,range);//获取抓路矩形

        RTNode root = tree.file.readNode(0);   // 获取2D-Rtree的root节点

        //获取候选路段矩形组成的列表
        ArrayList<Data> candidateRoads = tree.intersection_Rectangles(catchRectangle,root);

        if (candidateRoads.size()<1)
            System.err.println("candidate roads does not exist!!!");

        //遍历所有候选路段
        for (Data candidateRoad:candidateRoads)
        {
            Rectangle candidateRectangle = candidateRoad.mbr;   //提取这条候选路段的MBR最小外包矩形

            ArrayList<RoadPoint> road_points = candidateRectangle.getRoad(); //取出该路段的所有轨迹点

            // 参见 <<针对2019.12.21 Map-Matching问题1的改进>>
            double min_distance=Double.POSITIVE_INFINITY;

            double distance;               // 用于记录距离

            RoadPoint candidate = new RoadPoint("",0,0); // 用于记录候选点

            for(RoadPoint roadpoint:road_points)      //遍历这条路中的所有点
            {
                distance=getDistance(roadpoint,car_point); // 记录轨迹点与路段点的距离

                if (distance<min_distance)      //如果二者距离最近
                {
                    min_distance=distance;      //记下这个最短距离

                    candidate.setRoadName(roadpoint.getRoadName());    // 记录这个路点的道路编号

                    candidate.setLongitude(roadpoint.getLongitude());   // 记录这个路点的经度

                    candidate.setLatitude(roadpoint.getLatitude());    // 记录这个路点的纬度
                }
            }

            candidates.add(candidate); // candidate这就是最近的那个路段点
        }

        return candidates;  // 返回路段候选点组成的聚合
    }*/

    /**
     * 轨迹点:候选匹配路段=1:n & 候选匹配路段:候选匹配点=1:1 => 轨迹点:候选匹配点 = 1:n
     * @param car_point 当前时刻的轨迹点
     * @param range 抓路范围
     * @param tree 2D-Rtree
     * @return 当前时刻的候选匹配点组成的ArrayList
     */
    public static ArrayList<MatchedPoint> candidates(CarPoint car_point, double range, RTree tree)
    {
        ArrayList<MatchedPoint> c = new ArrayList<MatchedPoint>();  // 用来装候选路段匹配点

        Rectangle catchRectangle = getCatchRectangle(car_point,range);//获取抓路矩形

        RTNode root = tree.file.readNode(0);   // 获取2D-Rtree的root节点

        //获取候选路段矩形组成的列表
        ArrayList<Data> candidateRoads = tree.intersection_Rectangles(catchRectangle,root);

        if (candidateRoads.size()<1)
            System.err.println("GetRoads.candidates:candidate roads does not exist!!!");

        String car_id = car_point.id;   //取车牌号

        Date timestamp = car_point.timestamp;   //取时间戳

        //遍历所有候选路段
        for (Data candidateRoad:candidateRoads)
        {
            Rectangle candidateRectangle = candidateRoad.mbr;   //提取这条候选路段的MBR最小外包矩形(路段GPS包含在其中)

            ArrayList<RoadPoint> road_points = candidateRectangle.getRoad(); //取出该路段的所有GPS点

            // 参见 <<针对2019.12.21 Map-Matching问题1的改进>>
            double min_distance = Double.POSITIVE_INFINITY;

            double distance = 0;               // 用于记录距离

            double longitude = 0;

            double latitude = 0;

            String road_id = "";

            for(RoadPoint roadpoint:road_points)      //遍历这条路中的所有点
            {
                distance=getDistance(roadpoint,car_point); // 计算轨迹点与路段点的距离

                if (distance<min_distance)      //如果二者距离最近
                {
                    min_distance=distance;      //记下这个最短距离

                    longitude = roadpoint.getLongitude(); // 记下这个点的经度

                    latitude = roadpoint.getLatitude(); // 记下这个点的纬度

                    road_id = roadpoint.getRoadName(); //   记下这个点所属道路
                }
            }

            c.add(new MatchedPoint(road_id,car_id,timestamp,longitude,latitude,distance));
        }

        return c;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        CarPoint c = new CarPoint(113.989403,22.6695);//车辆的轨迹点

        ////////////////////////////////////// 原生树/////////////////////////////////////////////////////
        /*RTree tree_original = generateRTreeFromCFW_output("D:\\daochu.txt");

        ArrayList<RoadPoint> candidates = getCandidates(c,20,tree_original); // 返回边长范围40m正方形内的候选匹配点

        System.out.println("tree_original:all matching candidates for this point are shown below");

        // 遍历候选匹配点
        for (RoadPoint candidate:candidates)
        {
            System.out.println("road number:"+candidate.getRoadName()+" coordinate:"+candidate.getLongitude()+","+candidate.getLatitude());
        }

        System.out.println("tree_original:I have "+tree_original.getTreeLevel()+" levels.");

        PageFile file_original = tree_original.getFile();

        // 序列化pageFile
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:\\stuff\\file1"));
        oos.writeObject(file_original);
        oos.close();
*/
        /////////////////////////////再生树/////////////////////////////////////////////////////////////////

        // 反序列化,生成再生PageFile
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D:\\stuff\\tree3"));
        RTree tree_regenerate = (RTree)ois.readObject();

        System.out.println(tree_regenerate.toString());

        System.out.println("tree_regenerate:I have "+tree_regenerate.getTreeLevel()+" levels.");


    }
}
