package rtree;

import java.io.Serializable;
import java.util.*;

/**
 * 
 * @ClassName RTree
 * @Description ��������R-Tree��class
 */

public class RTree implements Serializable
{


	private static final long serialVersionUID = -3398089699801197058L;
	// �������Ա
	public static double alpha_dist;
	public static int numOfClusters = 0;
	
	/**
	 * ��Ա����1
	 * ���ݱ��洢��page�ļ�
	 */
	protected PageFile file = null;

	public void setPageFile(MemoryPageFile file)
	{
		this.file = file;
	}

	/**
	 * ���getter�򵥴ֱ��ķ���һ�����ӳ�Ա����,���γɴ�͸����Ĳ���(reference)
	 * @return
	 */
	public PageFile getFile(){ return file; }
	
	/**
	 * ���췽��1
	 *
	 * ΪPageFile��ʼ�����������ڵ㲢�����ڵ�д��file��
	 * 
	 * @param dimension ����ά��
	 * @param fillFactor �����������
	 * @param capacity ��������
	 * @param file
	 *                 �洢�����ڴ�����ļ�
	 * @param treeType ��������
	 */
	public RTree(int dimension, float fillFactor, int capacity, PageFile file,
			int treeType)
	{
		if (dimension <= 1) {
			throw new IllegalArgumentException(
					"Dimension must be larger than 1.");
		}

		if (fillFactor < 0 || fillFactor > 0.5) {
			throw new IllegalArgumentException(
					"Fill factor must be between 0 and 0.5.");
		}

		if (capacity <= 1) {
			throw new IllegalArgumentException(
					"Capacity must be larger than 1.");
		}

		if (file.tree != null) {
			throw new IllegalArgumentException(
					"PageFile already in use by another rtree instance.");
		}
		
		file.initialize(this, dimension, fillFactor, capacity, treeType);
		this.file = file;

		// ÿ��������洢��Ψһ��page�����ڵ����Ǵ洢��page 0.
		RTDataNode root = new RTDataNode(this, Constants.NIL, 0);
		file.writeNode(root);
	}

	/**
	 * ���췽��2
	 * @param file �洢����page
	 */
	public RTree(PageFile file)
	{
		if (file.tree != null)
		{
			throw new IllegalArgumentException(
					"PageFile already in use by another rtree instance.");
		}

		if (file.treeType == -1)
		{
			throw new IllegalArgumentException(
					"PageFile is empty. Use some other RTree constructor.");
		}

		file.tree = this;
		this.file = file;
	}

	/**
	 * ���췽��3
	 *
	 * Ĭ��д���ڴ�,����page����
	 * 
	 * @param dimension
	 * @param fillFactor
	 * @param capacity
	 * @param treeType
	 */
	public RTree(int dimension, float fillFactor, int capacity, int treeType) {
		this(dimension, fillFactor, capacity, new MemoryPageFile(), treeType);
	}

	/**
	 * @return RTree��ά��
	 */
	public int getDimension() {
		return file.dimension;
	}

	public int getPageSize() {
		return file.pageSize;
	}

	public float getFillFactor() {
		return file.fillFactor;
	}

	/**
	 * @return ���ؽ������
	 */
	public int getNodeCapacity() {
		return file.nodeCapacity;
	}

	/**
	 * @return ������������
	 */
	public int getTreeType() {
		return file.treeType;
	}

	/**
	 * ���ظ��ڵ��level
	 * Returns the level of the root Node, which signifies the level of the
	 * whole tree. Loads one page into main memory.
	 */
	public int getTreeLevel()
	{
		return file.readNode(0).getLevel();// ���ڵ����Ǵ洢��page 0.
	}

	/**
	 * <b>����I1��</b>Ϊ�¼�¼Ѱ�ұ���λ�á��������㷨ChooseLeaf��ѡ��һ�����ڱ���E��Ҷ�ڵ�L��<br>
	 * <b>����I2��</b>����¼����Ҷ�ڵ㡪������ڵ�L���д洢�ռ䣬��E���������档����ʹ���㷨
	 * SplitNodeִ�нڵ���Ѳ������ڵ�L����������½ڵ�L��LL��L��LL�б�����E�;�L�е�������Ŀ��<br>
	 * <b>����I3��</b>���ϴ������ı仯�����Խڵ�L�����㷨AdjustTree���������I2�н��й��ڵ���Ѳ�����
	 * �ǻ�Ҫ��LL�����㷨AdjustTree��<br>
	 * <b>����I4��</b>���ĳ��ߡ�������ڵ�ķ��Ѳ������ϴ��ݵ��¸��ڵ���ѣ��Ǿ�Ҫ�½�һ�����ڵ㡣
	 * �µĸ��ڵ�������ӽڵ���Ǿ��ӽڵ���Ѻ��γɵ������ڵ㡣<br>
	 * <p>
	 * ��Rtree�в���Rectangle<br>
	 * 1�����ҵ����ʵ�Ҷ�ڵ� <br>
	 * 2�������Ҷ�ڵ��в���<br>
	 * 
	 * @param rectangle
	 * @param page
	 * @return rectangle�������Ҷ�ӽ���pageNumber��(the parent of the data entry.)
	 */
	public int insert(Rectangle rectangle, int page)
	{
		if (rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");

		if (rectangle.getHigh().getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than RTree dimension.");
		}

		RTNode root = file.readNode(0);         // �����˶�̬

		RTDataNode leaf = root.chooseLeaf(rectangle); // ��һ������,�ҵ��������Ӧ�����ĸ�Ҷ�ӽڵ�

		return leaf.insert(rectangle, page);
	}
	
	/**
	 *
	 * ��R����ɾ��Rectangle
	 * <p>
	 * 1��Ѱ�Ұ�����¼�Ľ��--�����㷨findLeaf()����λ�����˼�¼��Ҷ�ӽ��L�����û���ҵ����㷨��ֹ��<br>
	 * 2��ɾ����¼--���ҵ���Ҷ�ӽ��L�еĴ˼�¼ɾ��<br>
	 * 3�������㷨condenseTree<br>
	 * 
	 * @param rectangle
	 * @return ��ɾ����Ŀ������ָ��
	 */
	public int delete(Rectangle rectangle)
	{
		if (rectangle == null) {
			throw new IllegalArgumentException("Rectangle cannot be null.");
		}

		if (rectangle.getHigh().getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than RTree dimension.");
		}

		RTNode root = file.readNode(0);

		RTDataNode leaf = root.findLeaf(rectangle);

		if (leaf != null) {
			return leaf.delete(rectangle);
		}

		return -1;
	}

	/**
	 * ����������
	 * �Ӹ����Ľ��root��ʼ����������еĽ��
	 * 
	 * @param root
	 * @return ���б����Ľ�㼯��
	 */
	public List<RTNode> traversePostOrder(RTNode root)
	{
		if (root == null)
			throw new IllegalArgumentException("Node cannot be null.");

		List<RTNode> list = new ArrayList<RTNode>();

		if (!root.isLeaf()) {
			for (int i = 0; i < root.usedSpace; i++) {
				List<RTNode> a = traversePostOrder(((RTDirNode) root)
						.getChild(i));
				for (int j = 0; j < a.size(); j++) {
					list.add(a.get(j));
				}
			}
		}

		list.add(root);

		return list;
	}

	/**
	 * @param rectangle ץ·����
	 * @return �����������rectangleץ·�����ཻ�����н���ö��
	 */
	public Enumeration<RTNode> intersection(Rectangle rectangle)
	{
		class IntersectionEnum implements Enumeration<RTNode> {
			private List<RTNode> nodes;

			private int index = 0;

			private boolean hasNext = true;

			public IntersectionEnum(Rectangle rectangle) {
				nodes = intersection(rectangle, file.readNode(0));
				if (nodes.isEmpty()) {
					hasNext = false;
				}
			}

			//@Override
			public boolean hasMoreElements() {
				return hasNext;
			}

			//@Override
			public RTNode nextElement() {
				if (!hasNext) {
					throw new NoSuchElementException("intersection");
				}

				RTNode node = nodes.get(index);
				index++;

				if (index == nodes.size()) {
					hasNext = false;
				}
				return node;
			}

		}
		return new IntersectionEnum(rectangle);
	}

	/**
	 *
	 * @param rectangle ץ·����?
	 * @param node
	 *            ��ǰ���
	 * @return �����������rectangleץ·�����ཻ������Ҷ�ӽ��ļ���
	 */
	public List<RTNode> intersection(Rectangle rectangle, RTNode node)
	{
		if (rectangle == null || node == null) {
			throw new IllegalArgumentException("Arguments cannot be null.");
		}
		if (rectangle.getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than Rtree dimension.");
		}

		List<RTNode> list = new ArrayList<RTNode>();

		if (node.getNodeRectangle().isIntersection(rectangle)) {
			if (node.isLeaf())// �¼ӵ�
			{
				list.add(node);
			} else
			// if(! node.isLeaf())
			{
				for (int i = 0; i < node.usedSpace; i++) {
					if (node.datas[i].isIntersection(rectangle)) {
						// �ݹ����
						List<RTNode> nodes = intersection(rectangle,
								((RTDirNode) node).getChild(i));
						for (int j = 0; j < nodes.size(); j++) {
							list.add(nodes.get(j));
						}
					}
				}
			}
		}

		return list;
	}

	/**
	 * @param rectangle ץ·����
	 * @param node
	 *            ��ǰ���
	 * @return �����������rectangleץ·�����ཻ�����н��ļ���
	 */
	public List<RTNode> intersection_All(Rectangle rectangle, RTNode node)
	{
		if (rectangle == null || node == null) {
			throw new IllegalArgumentException("Arguments cannot be null.");
		}
		if (rectangle.getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than Rtree dimension.");
		}

		List<RTNode> list = new ArrayList<RTNode>();

		if (node.getNodeRectangle().isIntersection(rectangle)) {
			list.add(node);

			if (!node.isLeaf()) {
				for (int i = 0; i < node.usedSpace; i++) {
					if (node.datas[i].isIntersection(rectangle)) {
						// �ݹ����
						List<RTNode> nodes = intersection_All(rectangle,
								((RTDirNode) node).getChild(i));
						for (int j = 0; j < nodes.size(); j++) {
							list.add(nodes.get(j));
						}
					}
				}
			}
		}

		return list;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!intersection_Rectangles!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  �ǳ��ؼ�!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  ����һ��ץ·����
	 *  ���������������ཻ��,��Ҷ�ӽڵ��е�,���������ɵ��б�
	 * @param rectangle
	 *            ������rectangle ץ·����
	 * @param node
	 *            ����
	 * @return �������������rectangle�ཻ��Ҷ�ӽ���е�rectangle��ɵļ���,��װ��Data����
	 */
	public ArrayList<Data> intersection_Rectangles(Rectangle rectangle, RTNode node)
	{
		if (rectangle == null || node == null) {
			throw new IllegalArgumentException("Arguments cannot be null.");
		}
		if (rectangle.getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than Rtree dimension.");
		}

		ArrayList<Data> list = new ArrayList<Data>();

		if (node.getNodeRectangle().isIntersection(rectangle)) {
			if (node.isLeaf())// �¼ӵ�
			{
				Rectangle[] rectangles = new Rectangle[node.usedSpace];
				for (int i = 0; i < rectangles.length; i++) {
					rectangles[i] = node.datas[i];
					if (rectangle.isIntersection(rectangles[i]))
						list.add(new Data(rectangles[i], i));
				}
			} else {
				for (int i = 0; i < node.usedSpace; i++) {
					if (node.datas[i].isIntersection(rectangle)) {
						// �ݹ����
						List<Data> nodes = intersection_Rectangles(rectangle,
								((RTDirNode) node).getChild(i));
						for (int j = 0; j < nodes.size(); j++) {
							list.add(nodes.get(j));
						}
					}
				}
			}
		}

		return list;
	}

	/**
	 * �ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ�ϸ����ѽ
	 *  nearestNeighbor������������������������������������������������������������������������������������������������
	 * �ϸ����ѽ
	 *
	 * ������,��ֱ�Ӹ��㳵���켣��
	 * ������������������ľ��ι��ɵ��б�
	 *
	 * @param point
	 *            ��ѯPoint
	 * @return �����������point�����Rectangle����װ��Data���͵ļ���
	 */
	public List<Data> nearestNeighbor(Point point)
	{
		return nearestNeighborSearch(file.readNode(0), point,
				Float.POSITIVE_INFINITY, false);
		// return nearestNeighborSearch(root, point, 5000);
	}

	/**
	 *  �����ֵ�!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * ������ѯ��(�����켣��)����ѯָ����Χ�ڵ�n������
	 * 
	 * @param queryPoint
	 *            ��ѯ��. �����켣��
	 * @param range
	 *            ������Χ.
	 * @param n
	 *            ��ѯ�������Ŀ.����Ҫ�������
	 * @return the leaf elements found near the point.The length of the returned
	 *         array would be equal to <code>n</code>.
	 */
	public Data[] nearestSearch(Point queryPoint, double range, int n)
	{
		if (n <= 0 || range < 0 || queryPoint == null)
			throw new IllegalArgumentException(
					"RTree.nearestSearch: Illegal arguments");

		Data[] datas = new Data[n];

		// �̶�nearest����ѯ����ָ����Χ�ڵ����ж���
		List<Data> dataList = nearestNeighborSearch(file.readNode(0),
				queryPoint, range, true);

		// �Բ�ѯ�Ľ����minDist����
		Collections.sort(dataList, new Comparator<Data>() {

			//@Override
			public int compare(Data o1, Data o2) {
				double f = o1.minDist - o2.minDist;
				if (f > 0)
					return 1;
				else if (f < 0)
					return -1;
				return 0;
			}
		});

		// ȡǰn��
		if (dataList.size() < n) {
			for (int i = 0; i < dataList.size(); i++) {
				datas[i] = dataList.get(i);
			}
		} else {
			for (int i = 0; i < n; i++) {
				datas[i] = dataList.get(i);
			}
		}
		return datas;

	}

	/**
	 * See the paper "<b><u>Nearest Neighbor Queries</u></b>"<br>
	 * 1�����һ����P��MBR M�ľ���MINDIST(P,M)���ڵ�P����һ��MBR M���ľ���MINMAXDIST(P,M��)��
	 * ��M������������Ϊ�������ܰ���NN�����ݶ���1�Ͷ���2���������������¼�֦��<br>
	 * 2�����һ����P��һ��������O��ʵ�ʾ�����ڵ�P��MBR M�ľ���MINMAXDIST (P,M)����M����������ʵ��
	 * ������ΪNN����Ĺ��Ʊ��滻��������ΪM��������O����P���������ݶ���2�����ⱻ�������¼�֦��<br>
	 * 3�����ÿ��MBR�ľ���MINDIST(P,M)���ڵ�P��һ��������O��ʵ�ʾ��룬���M������������Ϊ�������ܰ���
	 * ������O��һ�����󣨶���1�����ⱻ�������ϼ�֦��<br>
	 * <p>
	 * 
	 * ���㷨�Ӹ���㿪ʼ���·��ʸ���MBR���㷨�����ȼٶ�����ھ���NearestΪ����������������½���
	 * ��ÿ�����·��ʵ�<b><u>��Ҷ���</u></b>���ȼ������������ ��MBR��MINDISTֵ��������Щֵ
	 * ����������֧����ABL(Active Branch List)�У����Ŷ�ABL���ü�֦����1�Ͳ���2����
	 * ������Ҫ�ķ�֧���㷨��ABL���ظ�����ֱ��ABLΪ�ա�ÿ���ظ��������㷨��ѡ�������е���һ����֧����
	 * ����÷�֧��MBR��Ӧ�Ľ���ϵݹ�������Ϲ��̡�����<b><u>Ҷ���</u></b>�㷨��ÿ���������һ
	 * ���ض����͵ľ��뺯���������������ֵ�����Nearest�Ƚϣ�ѡ�����и�С��ֵ�滻Nearest���ڵݹ�
	 * ���̷���ʱʹ������µ�����ھ���Ĺ���ֵ��Ϊ�ж����������ò���3��֦���Ƴ�ABL������ MINDISTֵ ����Nearest��MBR���ڵķ�֧��
	 * 
	 * @param node
	 *            ���֧�еĵ�ǰ���
	 * @param queryPoint
	 *            ��ѯPoint
	 * @param nearest
	 *            point����ǰRectangle���������
	 * @param nearestIsFixed
	 *            nearest�Ƿ�̶�����
	 * @return
	 */
	protected List<Data> nearestNeighborSearch(RTNode node, Point queryPoint,
                                               double nearest, boolean nearestIsFixed) {
		List<Data> ret = new ArrayList<Data>();
		Rectangle rectangle;

		if (node.isLeaf())// nodeΪҶ�ӽ����Ƚ�Point��ÿ����Ŀ����С����
		{
			for (int i = 0; i < node.usedSpace; i++) {
				double dist = node.datas[i].getMinDist(queryPoint);
				if (dist < nearest) {
					rectangle = node.datas[i];
					if (!nearestIsFixed)// nearest���̶�
						nearest = dist;// ���Գ���ע�ʹ˴�
					ret.add(new Data(rectangle, dist, i));
				}
			}
		} else {// nodeΪ��Ҷ�ӽ��
				// 1�����ɷ�֧�б�
				// ��ÿ�����·��ʵķ�Ҷ������ȼ������������
				// ��MBR��MINDIST��MINMAXDISTֵ��������Щֵ����������֧����ABL(Active Branch
				// List)��
			BranchList[] branchList = new BranchList[node.usedSpace];
			for (int i = 0; i < node.usedSpace; i++) {
				RTNode rtNode = ((RTDirNode) node).getChild(i);
				branchList[i] = new BranchList(rtNode, rtNode
						.getNodeRectangle().getMinDist(queryPoint), rtNode
						.getNodeRectangle().getMinMaxDist(queryPoint));
			}

			// 2�������֧�б�
			// �Ѹ�����point��node���ĺ��ӽ���MINDIST����С����˳������
			Arrays.sort(branchList, new BranchListMinDistComparator());

			// 3����֦��֧�б�
			int last = pruneBranchList(nearest, branchList, branchList.length);

			if (last != branchList.length)// ������
				System.out.println("����ȣ����� last = " + last + ",length = "
						+ branchList.length);

			for (int i = 0; i < last; i++) {
				if (branchList[i].minDist < nearest) {
					List<Data> nonLeaf = nearestNeighborSearch(
							branchList[i].node, queryPoint, nearest,
							nearestIsFixed);

					if (nonLeaf != null && nonLeaf.size() > 0) {
						for (int j = 0; j < nonLeaf.size(); j++)
							ret.add(nonLeaf.get(j));
					}

					int t = last;// ������
					last = pruneBranchList(nearest, branchList, last);
					if (last != t)// ������
						System.out.println("****����ȣ����� last = " + last
								+ ",length = " + t);
				}// end if
			}// end for
		}// end else

		return ret;
	}

	/**
	 * �԰�MINDIST������branchList���м�֦
	 * <p>
	 * <b><u>��֦����1��</u></b>���MINDIST(P,M1) >
	 * MINMAXDIST(P,M2)����M1������������Ϊ�������ܰ���NN�����ݶ���1�Ͷ���2��<br>
	 * <b><u>��֦����2:</u></b>���һ����P��һ��������O��ʵ�ʾ�����ڵ�P��MBR M�ľ���MINMAXDIST
	 * (P,M)����M����������ʵ��������ΪNN����Ĺ��Ʊ��滻������<br>
	 * <b><u>��֦����3:</u></b>���ÿ��MBR�ľ���MINDIST(P,M)���ڵ�P��һ��������O��ʵ�ʾ��룬���M����������
	 * ��Ϊ�������ܰ���������O��һ�����󣨶���1����<br>
	 * 
	 * @param nearest
	 * @param branchList
	 * @param usedSpace
	 * @return �������ֵΪLast��������ʱֻ��Ҫ����branchList��[0,Last]�����౻����
	 */
	public int pruneBranchList(double nearest, BranchList[] branchList,
			int usedSpace) {
		int last = usedSpace;
		int i;

		// ��֦����1�����MINDIST(P,M1) >
		// MINMAXDIST(P,M2)����M1������������Ϊ�������ܰ���NN�����ݶ���1�Ͷ���2��
		for (i = 0; i < last; i++) {
			// �Ⱥ�����MINDIST�Ƚϣ������MINMAXDISTС������MINDIST����ҳ�����λ��
			if (branchList[i].minMaxDist < branchList[last - 1].minDist) {
				for (int j = 0; j < last; j++) {
					if ((i != j)
							&& (branchList[j].minDist > branchList[i].minMaxDist)) {
						last = j;
						break;
					}
				}
			}
		}

		// ��֦����2:���һ����P��һ��������O��ʵ�ʾ�����ڵ�P��MBR M�ľ���MINMAXDIST
		// (P,M)����M����������ʵ��������ΪNN����Ĺ��Ʊ��滻������
		// nearest > MINMAXDIST(P,M)
		// -> nearest = MIMMAXDIST(P,M)
		for (i = 0; i < last; i++) {
			if (nearest > branchList[i].minMaxDist)
				nearest = branchList[i].minMaxDist;
		}

		// ��֦����3:���ÿ��MBR�ľ���MINDIST(P,M)���ڵ�P��һ��������O��ʵ�ʾ��룬���M������������Ϊ�������ܰ���������O��һ�����󣨶���1����
		// nearest < MINDIST(P,M)
		for (i = 0; i < last && nearest >= branchList[i].minDist; i++)
			;

		last = i;

		return last;
	}

	public List<RTNode> traverseByLevel() {
		RTNode root = file.readNode(0);

		if (root == null)
			throw new IllegalArgumentException("Node cannot be null.");

		List<RTNode> list = traverseByLevel(root);

		return list;
	}

	/**
	 * ����һ������root�ڵ�
	 * �������нڵ���ɵ��б�
	 *
	 * @param root
	 *            ������ʼ�Ľ��
	 * @return �����Ե����ϣ��������ҵİ������н��ļ���
	 */
	public List<RTNode> traverseByLevel(RTNode root)
	{
		if (root == null)
			throw new IllegalArgumentException("Node cannot be null.");

		List<RTNode> ret = new ArrayList<RTNode>();
		List<RTNode> list = traversePostOrder(root);

		for (int i = getTreeLevel(); i >= 0; i--) {
			for (int j = 0; j < list.size(); j++) {
				RTNode n = list.get(j);
				if (n.getLevel() == i) {
					ret.add(n);
				}
			}
		}

		return ret;
	}

	public void painting()
	{
		System.out.println("���������");
	}

	/**
	 * ����������
	 * �Ӹ����Ľ��root��ʼ����������еĽ��
	 *
	 * @param root
	 * @return ���б����Ľ�㼯��
	 */
	public void paintingPostOrder(RTNode root)
	{
		if (root == null)
			throw new IllegalArgumentException("Node cannot be null.");

		List<RTNode> list = new ArrayList<RTNode>();

		if (!root.isLeaf()) {
			for (int i = 0; i < root.usedSpace; i++) {
				List<RTNode> a = traversePostOrder(((RTDirNode) root)
						.getChild(i));
				System.out.println(a.toString());
				/*for (int j = 0; j < a.size(); j++) {
					System.out.print(a.toString());
					System.out.println("\t");
				}*/
				System.out.println("\n");
			}
		}

	}
}
