package rtree;

import java.io.Serializable;
import java.util.List;


/**
 * @ClassName RTDirNode
 * @Description ��Ҷ�ڵ�
 */
public class RTDirNode extends RTNode implements Serializable
{

	// ���췽��
	public RTDirNode(RTree tree, int parent, int pageNumber, int level) {
		super(tree, parent, pageNumber, level);
	}

	/**
	 * �����֧���
	 * ����:��Ӧ�ĺ��ӽڵ�
	 * @param index
	 * @return ��Ӧ�����µĺ��ӽ��
	 */
	public RTNode getChild(int index)
	{
		if (index < 0 || index >= usedSpace) {
			throw new IndexOutOfBoundsException("" + index);
		}
		return tree.file.readNode(branches[index]);
	}

	/**
	 * ��������ҳ��µľ������ڵ�level��
	 * ����һ������
	 * ������:�������Ӧ�����ĸ�Ҷ�ӽڵ���
	 *
	 * @param rectangle
	 * @return
	 */
	@Override
	public RTDataNode chooseLeaf(Rectangle rectangle) {

		int index;

		switch (tree.getTreeType()) {
		case Constants.RTREE_LINEAR:

		case Constants.RTREE_QUADRATIC:

		case Constants.RTREE_EXPONENTIAL:
			index = findLeastEnlargement(rectangle);
			break;
		case Constants.RSTAR:
			if (level == 1)// ���˽��ָ��Ҷ�ڵ�
			{
				index = findLeastOverlap(rectangle);
			} else {
				index = findLeastEnlargement(rectangle);
			}
			break;

		default:
			throw new IllegalStateException("Invalid tree type.");
		}

		return getChild(index).chooseLeaf(rectangle);
	}

	/**
	 * ����һ������
	 * ������:�����ص������С�������������С,�Ľڵ�ı��
	 *
	 * @param rectangle
	 * @return ������С�ص�����Ľ�������������ص���������ѡ������Rectangle�����������С�ģ�
	 *         �����������������ѡ�����������С��
	 */
	private int findLeastOverlap(Rectangle rectangle) {
		float overlap = Float.POSITIVE_INFINITY;
		int sel = -1;

		for (int i = 0; i < usedSpace; i++) {
			RTNode node = getChild(i);
			float ol = 0;

			for (int j = 0; j < node.datas.length; j++) {
				ol += rectangle.intersectingArea(node.datas[j]);
			}
			if (ol < overlap) {
				overlap = ol;// ��¼�ص������С��
				sel = i;// ��¼�ڼ������ӵ�����
			} else if (ol == overlap)// ����ص���������ѡ������Rectangle�����������С��,�����������������ѡ�����������С��
			{
				double area1 = datas[i].getUnionRectangle(rectangle).getArea()
						- datas[i].getArea();
				double area2 = datas[sel].getUnionRectangle(rectangle)
						.getArea() - datas[sel].getArea();

				if (area1 == area2) {
					sel = (datas[sel].getArea() <= datas[i].getArea()) ? sel
							: i;
				} else {
					sel = (area1 < area2) ? i : sel;
				}
			}
		}
		return sel;
	}

	/**
	 * ����һ������
	 * ������:����þ��κ����������С�Ľڵ�,��uniqueID
	 *
	 * @param rectangle
	 * @return ���������С�Ľ������������������������ѡ�����������С��
	 */
	private int findLeastEnlargement(Rectangle rectangle) {
		double area = Double.POSITIVE_INFINITY;
		int sel = -1;

		for (int i = 0; i < usedSpace; i++) {
			double enlargement = datas[i].getUnionRectangle(rectangle)
					.getArea() - datas[i].getArea();
			if (enlargement < area) {
				area = enlargement;
				sel = i;
			} else if (enlargement == area) {
				sel = (datas[sel].getArea() <= datas[i].getArea()) ? sel : i;
			}
		}

		return sel;
	}

	/**
	 * ���ڵ������Ľṹ
	 *
	 * �����µ�Rectangle��Ӳ����Ҷ�ڵ㿪ʼ���ϵ���RTree��ֱ�����ڵ�
	 * 
	 * @param node1
	 *            ������Ҫ�����ĺ��ӽ��
	 * @param node2
	 *            ���ѵĽ�㣬��δ������Ϊnull
	 */
	public void adjustTree(RTNode node1, RTNode node2)
	{
		// ��Ҫ�ҵ�ָ��ԭ���ɵĽ�㣨��δ���Rectangle֮ǰ������Ŀ������
		for (int i = 0; i < usedSpace; i++) {
			if (branches[i] == node1.pageNumber) {
				datas[i] = node1.getNodeRectangle();// ��������
//				tree.file.writeNode(this);//�¼ӵ�
				break;
			}
		}

		if (node2 == null) {
			tree.file.writeNode(this);
		}
		/*
		 * ��������������Ǳ�������µĽ�㣬�������Ǳ����������treeֱ������root��㡣
		 */
		if (node2 != null) {
			insert(node2);// �����µĽ��

		} else if (!isRoot())// ��û������ڵ�
		{
			RTDirNode parent = (RTDirNode) getParent();
			parent.adjustTree(this, null);// ���ϵ���ֱ�����ڵ�
		}
	}
	
//	/**
//	 * @param S S is a set of nodes that contains the node being updated,
//	 * its cooperating sibings(if overflow has occurred) and newly
//	 * created node NN(if split has occurred).
//	 */
	/**
	 * �Աշ���
	 * ���ڵ������Ľṹ
	 *
	 * @param node the node being updated
	 * @param siblings its cooperating sibings(if overflow has occurred)
	 * @param NN newly created node(if split has occurred).
	 */
	public void adjustTree(/*List<RTNode> S*/RTNode node, List<RTNode> siblings, RTNode NN)
	{
		// ��Ҫ�ҵ�ָ��ԭ���ɵĽ�㣨��δ���Rectangle֮ǰ������Ŀ������
		for (int i = 0; i < usedSpace; i++) {
			if (branches[i] == node.pageNumber) {
				datas[i] = node.getNodeRectangle();// ��������
//				tree.file.writeNode(this);//�¼ӵ�
				break;
			}
		}

		if (NN == null) {
			tree.file.writeNode(this);
		}
		/*
		 * ��������������Ǳ�������µĽ�㣬�������Ǳ����������treeֱ������root��㡣
		 */
		if (NN != null) {
			insert(NN);// �����µĽ��

		} else if (!isRoot())// ��û������ڵ�
		{
			RTDirNode parent = (RTDirNode) getParent();
			parent.adjustTree(this, null);// ���ϵ���ֱ�����ڵ�
		}
	}

	/**
	 * ����һ���ڵ�,����ɲ��붯��
	 * Ȼ��������ҽڵ��Ƿ���Ҫ����
	 *
	 * ��������������������Ҫ���ѣ�<br>
	 * ������Ҫ���ѣ�ֻ��������ݲ�����д��file�����adjustTree()
	 * 
	 * @param node
	 * @return ��������Ҫ�����򷵻�true
	 */
	protected boolean insert(RTNode node)
	{
		if (usedSpace < tree.getNodeCapacity()) {
			datas[usedSpace] = node.getNodeRectangle();
			branches[usedSpace] = node.pageNumber;
			usedSpace++;
			node.parent = pageNumber;
			tree.file.writeNode(node);
			tree.file.writeNode(this);
			/* �Ȼ�ȡ�丸�ڵ㣬Ȼ����丸�ڵ㿪ʼ�������ṹ */
			RTDirNode parent = (RTDirNode) getParent();
			if (parent != null) {
				parent.adjustTree(this, null);
			}
			return false;
		} else {// ��Ҷ�ӽ����Ҫ����
			RTDirNode[] a = splitIndex(node);
			RTDirNode n = a[0];
			RTDirNode nn = a[1];

			if (isRoot()) {
				n.parent = 0;// �丸�ڵ�Ϊ���ڵ�
				n.pageNumber = -1;
				nn.parent = 0;
				nn.pageNumber = -1;
				/*
				 * �Ƚ����Ѻ�Ľ��д��file�����᷵��һ���洢page��Ȼ��������ӽ�㣬
				 * �����ӽ���parentָ��ָ��˽�㣬Ȼ�󽫺��ӽ������д��file��
				 */
				int p = tree.file.writeNode(n);
				for (int i = 0; i < n.usedSpace; i++) {
					RTNode ch = n.getChild(i);
					ch.parent = p;
					tree.file.writeNode(ch);
				}
				p = tree.file.writeNode(nn);
				for (int i = 0; i < nn.usedSpace; i++) {
					RTNode ch = nn.getChild(i);
					ch.parent = p;
					tree.file.writeNode(ch);
				}
				// �½����ڵ㣬������1
				RTDirNode newRoot = new RTDirNode(tree, Constants.NIL, 0,
						level + 1);
				newRoot.addData(n.getNodeRectangle(), n.pageNumber);
				newRoot.addData(nn.getNodeRectangle(), nn.pageNumber);
				tree.file.writeNode(newRoot);

			} else {// not root node, but need split
				n.pageNumber = pageNumber;
				n.parent = parent;
				nn.pageNumber = -1;
				nn.parent = parent;
				tree.file.writeNode(n);
				int j = tree.file.writeNode(nn);
				for (int i = 0; i < nn.usedSpace; i++) {
					RTNode ch = nn.getChild(i);
					ch.parent = j;
					tree.file.writeNode(ch);
				}
				RTDirNode p = (RTDirNode) getParent();
				p.adjustTree(n, nn);
			}
		}
		return true;
	}

	/**
	 * ���ڷ�Ҷ�ӽ��ķ���
	 * index,��Ҷ�ӽڵ�
	 * @param node
	 * @return
	 */
	private RTDirNode[] splitIndex(RTNode node)
	{
		int[][] group = null;

		switch (tree.getTreeType()) {
		case Constants.RTREE_LINEAR:
			break;
		case Constants.RTREE_QUADRATIC:
			group = quadraticSplit(node.getNodeRectangle(), node.pageNumber);
			break;
		case Constants.RTREE_EXPONENTIAL:
			break;
		case Constants.RSTAR:
			break;
		default:
			throw new IllegalStateException("Invalid tree type.");
		}

		RTDirNode index1 = new RTDirNode(tree, parent, pageNumber, level);
		RTDirNode index2 = new RTDirNode(tree, parent, -1, level);

		int[] group1 = group[0];
		int[] group2 = group[1];

		for (int i = 0; i < group1.length; i++) {
			index1.addData(datas[group1[i]], branches[group1[i]]);
		}
		for (int i = 0; i < group2.length; i++) {
			index2.addData(datas[group2[i]], branches[group2[i]]);
		}

		return new RTDirNode[] { index1, index2 };
	}

	/**
	 * ���ڲ���
	 *
	 * ����һ������
	 * ����������������ĸ��ڵ���
	 *
	 * @param rectangle
	 * @return
	 */
	@Override
	protected RTDataNode findLeaf(Rectangle rectangle)
	{
		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].enclosure(rectangle)) {
				RTDataNode leaf = getChild(i).findLeaf(rectangle);
				if (leaf != null)
					return leaf;
			}
		}
		return null;
	}

}
