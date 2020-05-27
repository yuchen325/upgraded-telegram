package rtree

import java.util

import entity.RoadPoint

/**
 * author: yc
 */
object GetSparkTree {

  /**
   * ����һ��Ԥ������˵�·������RDD,�ҷ���һ��2D-RTree
   * @param rdd1
   * @return
   */
    /*
    def getTree(rdd1:RDD[String]): RTree=
    ������Щһ��������rdd�ı�������ʵ����Array�ˣ�Ҳ����˵��ʵ���Ǳ��ؼ����ˣ�������spark���ܵ���
     */
  def getTree(rdd1:Array[String]): RTree=
  {
    //val rdd = rdd1.par ����Ժ��뵽spark�ϵĻ�
    // ArrayԪ��:String ==map==> Array[String]
    val road_rdd2=rdd1.map(_.split(","))

    // ArrayԪ��:Array[String] ==map==> <id,RoadPoint>
    val road_rdd3=road_rdd2.map(t=>(t(0),new RoadPoint(t(0),t(1).trim.toDouble,t(2).trim.toDouble)))

    // ArrayԪ��:<id,RoadPoint> ==map==> <id,Array[(String,RoadPoint)]>
    val road_rdd4=road_rdd3.groupBy(_._1)

    //  ArrayԪ��:<id,Array[(String,RoadPoint)]> ==map==> <id,Rectangle>
    val road_rdd5=road_rdd4.mapValues(t=>getRectangle(t))

    // ArrayԪ��: <id,Rectangle>.values => Rectangle
    val MBRs=road_rdd5.values

    val tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC) //��ʼ��һ����

    // �������е�ÿһ��MBR���������tree
    for (rectangle <- MBRs)
    {
      tree.insert(rectangle, -2)
    }

    tree  // ���������
  }

  /**
   *
   * @param roadPoints,�������ͬһ��·�ж��RoadPoint�Ŀɵ�����
   * @return  ��������·��MBR
   */
    //def getRectangle(roadPoints:Iterable[RoadPoint]):Rectangle =
  def getRectangle(roadPoints:Array[(String,RoadPoint)]):Rectangle =
  {
    val low = Array(Double.PositiveInfinity,Double.PositiveInfinity)

    val high =Array(Double.NegativeInfinity,Double.NegativeInfinity)

    // ����һ������ת��: Array[RoadPoint] -> ArrayList<RoadPoint> Ŀ����Ϊ�˴���Rectangle��������
    val roadPoints1 = new util.ArrayList[RoadPoint]() //���ڴ洢��·�켣

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
//    val taxi_rdd2 = taxi_rdd1.map(_.split(",")) // rddԪ��:Array[String],��һ�а����Ų�ֳ����ַ�������
//
//    val ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss") //Dateת����
//
//    val taxi_rdd3 = taxi_rdd2.map(
//      fields => (fields(0),new CarPoint(fields(0),fields(1).trim.toDouble,fields(2).trim.toDouble,ft.parse(fields(3)),fields(4).trim.toDouble))
//    ) // rddԪ��:<���ƺ�,(CarPoint��5������)> ��ʵ������汾��Map-Matching�в��ῼ�ǳ���,����fields(4)��ʵ��û�õ�
//
//    val taxi_rdd4 = taxi_rdd3.groupByKey() // rddԪ��:<���ƺ�,Iterable<����ĳ����켣��>>
//
//    // CarPoint����Ƚ���
//    def sortRule(carPoint: CarPoint ): Long = carPoint.timestamp.getTime() // �����Դ�GMT 1970-01-01 00:00:00����date�����"����",��λ:ms
//
//    val taxi_rdd5 = taxi_rdd4.mapValues(f => f.toArray.sortBy(sortRule)) // rddԪ��:<���ƺ�,Array<����ĳ����켣��>>
//
//    val tree_regenerate = broadcast.value // ��ȡ�㲥֮���������
//
//    taxi_rdd5.mapValues(f => Entrance.matchingEntranceForSpark(f,range,tree_regenerate)) //ƥ����:һ��RDD,Ԫ����<���ƺ�,ƥ���ĳ����켣>
//  }