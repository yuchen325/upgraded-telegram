package rtree

import java.util

import entity.RoadPoint

/**
 * author: yc
 */
object GetSparkTree {

  /**
   * 给我一个预处理好了的路网数据RDD,我返回一个2D-RTree
   * @param rdd1
   * @return
   */
    /*
    def getTree(rdd1:RDD[String]): RTree=
    下面那些一个个叫做rdd的变量，其实都是Array了，也就是说其实都是本地计算了，不再是spark上跑的了
     */
  def getTree(rdd1:Array[String]): RTree=
  {
    //val rdd = rdd1.par 如果以后想到spark上的话
    // Array元素:String ==map==> Array[String]
    val road_rdd2=rdd1.map(_.split(","))

    // Array元素:Array[String] ==map==> <id,RoadPoint>
    val road_rdd3=road_rdd2.map(t=>(t(0),new RoadPoint(t(0),t(1).trim.toDouble,t(2).trim.toDouble)))

    // Array元素:<id,RoadPoint> ==map==> <id,Array[(String,RoadPoint)]>
    val road_rdd4=road_rdd3.groupBy(_._1)

    //  Array元素:<id,Array[(String,RoadPoint)]> ==map==> <id,Rectangle>
    val road_rdd5=road_rdd4.mapValues(t=>getRectangle(t))

    // Array元素: <id,Rectangle>.values => Rectangle
    val MBRs=road_rdd5.values

    val tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC) //初始化一颗树

    // 遍历其中的每一个MBR，将其插入tree
    for (rectangle <- MBRs)
    {
      tree.insert(rectangle, -2)
    }

    tree  // 返回这棵树
  }

  /**
   *
   * @param roadPoints,包含多个同一条路中多个RoadPoint的可迭代集
   * @return  返回这条路的MBR
   */
    //def getRectangle(roadPoints:Iterable[RoadPoint]):Rectangle =
  def getRectangle(roadPoints:Array[(String,RoadPoint)]):Rectangle =
  {
    val low = Array(Double.PositiveInfinity,Double.PositiveInfinity)

    val high =Array(Double.NegativeInfinity,Double.NegativeInfinity)

    // 做了一个类型转换: Array[RoadPoint] -> ArrayList<RoadPoint> 目的是为了传入Rectangle构建矩形
    val roadPoints1 = new util.ArrayList[RoadPoint]() //用于存储道路轨迹

    for (roadPoint <- roadPoints)
      {
        if (roadPoint._2.getLongitude<low(0))
          low(0)=roadPoint._2.getLongitude
        if (roadPoint._2.getLatitude<low(1))
          low(1)=roadPoint._2.getLatitude
        if (roadPoint._2.getLongitude>high(0))
          high(0)=roadPoint._2.getLongitude
        if (roadPoint._2.getLatitude>high(1))
          high(1)=roadPoint._2.getLatitude

        roadPoints1.add(roadPoint._2)
      }

    val left = new Point(low)

    val right = new Point(high)

    new Rectangle(left,right,roadPoints1)
  }
}
//def matchingOnSpark(taxi_rdd1:RDD[String], broadcast: Broadcast[RTree], range:Double):RDD[(String,util.ArrayList[RoadPoint])] =
//  {
//    val taxi_rdd2 = taxi_rdd1.map(_.split(",")) // rdd元素:Array[String],将一行按逗号拆分出的字符串数组
//
//    val ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss") //Date转换器
//
//    val taxi_rdd3 = taxi_rdd2.map(
//      fields => (fields(0),new CarPoint(fields(0),fields(1).trim.toDouble,fields(2).trim.toDouble,ft.parse(fields(3)),fields(4).trim.toDouble))
//    ) // rdd元素:<车牌号,(CarPoint的5个属性)> 其实在这个版本的Map-Matching中不会考虑车速,所以fields(4)其实就没用到
//
//    val taxi_rdd4 = taxi_rdd3.groupByKey() // rdd元素:<车牌号,Iterable<无序的车辆轨迹点>>
//
//    // CarPoint对象比较器
//    def sortRule(carPoint: CarPoint ): Long = carPoint.timestamp.getTime() // 返回自从GMT 1970-01-01 00:00:00到此date对象的"距离",单位:ms
//
//    val taxi_rdd5 = taxi_rdd4.mapValues(f => f.toArray.sortBy(sortRule)) // rdd元素:<车牌号,Array<有序的车辆轨迹点>>
//
//    val tree_regenerate = broadcast.value // 获取广播之后的再生树
//
//    taxi_rdd5.mapValues(f => Entrance.matchingEntranceForSpark(f,range,tree_regenerate)) //匹配结果:一个RDD,元素是<车牌号,匹配后的车辆轨迹>
//  }