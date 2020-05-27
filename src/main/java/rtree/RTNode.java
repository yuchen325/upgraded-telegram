package rtree;

import java.io.Serializable;
import java.util.List;

/**
 * ����һ��������,��ʹ����INode�ӿ�
 * @ClassName RTNode
 * @Description ��������һ��R -tree�ڵ�
 */
public abstract class RTNode implements INode, Serializable
{
	/**
	 * ��Ա����1
	 * ������ڵ���
	 * transient���εĳ�Ա�������������л�������Ҫ�������л����Ծ�ȥ��
	 */
	//protected transient RTree tree;
	protected RTree tree;

	/**
	 * ��Ա����2
	 * ������ڵĲ㣬Ҷ�ӽ�����ڵĲ�Ϊ0
	 */
	protected int level;

	/**
	 * ��Ա����3
	 * ����ڵ��а�����MBR
	 * �൱����Ŀ
	 */
	public Rectangle[] datas;

	/**
	 * ��Ա����4
	 * ������õĿռ�
	 */
	protected int usedSpace;

	/**
	 * ��Ա����5
	 * �Ա�ŵ���ʽ��¼����ڵ�֮�µķ�֧
	 */
	public int[] branches;

	/**
	 * ��Ա����6
	 * ��node���洢��pageNumber
	 */
	protected int pageNumber;

	/**
	 * ��Ա����7
	 * �˽��ĸ��ڵ㱻�洢��pageNumber
	 */
	protected int parent;

	/**
	 * ���췽��1
	 * @param tree
	 * @param parent
	 * @param pageNumber
	 * @param level
	 */
	protected RTNode(RTree tree, int parent, int pageNumber, int level)
	{
		this.parent = parent;
		this.tree = tree;
		this.pageNumber = pageNumber;
		this.level = level;
		datas = new Rectangle[tree.getNodeCapacity() + 1];
		branches = new int[tree.getNodeCapacity() + 1];
		usedSpace = 0;
	}


	/**
	 * ��Ա����1
	 * level��getter����
	 * @return ������ڵĲ�
	 */
	@Override
	public int getLevel() {
		return level;
	}

	/**
	 * ��Ա����2
	 * @return ���ظ��ڵ�
	 */
	@Override
	public RTNode getParent()
	{
		if (isRoot()) {
			return null;
		} else {
			return tree.file.readNode(parent);
		}
	}

	/**
	 * ��Ա����3
	 * ��ȡ�ڵ��pageNumber
	 * @return Returns a unique id for this node. The page number is unique for every node.
	 */
	@Override
	public String getUniqueId() {
		return Integer.toBinaryString(pageNumber);
	}
	
	protected void addData(Rectangle rectangle, int page) {
		if (usedSpace == tree.getNodeCapacity()) {
			throw new IllegalArgumentException("Node is full.");
		}
		datas[usedSpace] = rectangle;
		branches[usedSpace] = page;
		usedSpace++;
	}
	
	/**
     * Adds a child node into this node.
     * This function does not save the node into persistent storage. 
     * It is used for bulk loading a node whith data. The user must 
     * make sure that she saves the node into persistent storage, after
     * calling this function.
     *
	 * Ϊ����ڵ����һ�����ӽڵ�
	 * �������ñ�������ڵ�
	 * �ڵ�������������û�����ȷ�����ñ���������ڵ�
     * @param node The new node to insert as a child of the current node.
     *
     */
	public void addData(RTNode node) {
		addData(node.getNodeRectangle(), node.pageNumber);
	}

	/**
	 *
	 * ɾ������еĵ�i����Ŀ
	 * Ҳ����ɾ��datas�еĵ�i��MBR
	 * @param i
	 */
	protected void deleteData(int i) {
		if (datas[i + 1] != null) {
			System.arraycopy(datas, i + 1, datas, i, usedSpace - i - 1);
			System.arraycopy(branches, i + 1, branches, i, usedSpace - i - 1);
			datas[usedSpace - 1] = null;
			branches[usedSpace - 1] = 0;
		} else {
			datas[i] = null;
			branches[i] = 0;
		}
//		if (datas[i].getHilbertValue() >= LHV) {
			//TODO
//		}
		usedSpace--;
	}

	/**
	 * ɾ�����κ����ض���,�����ع�R-tree
	 * ����ѹ����Ҷ�ڵ�L�иո�ɾ����һ����Ŀ���������ڵ����Ŀ��̫�٣���ɾ���ý�㣬
	 * ͬʱ����Щ��Ŀ�ض�λ�������ڵ��С�����б�Ҫ��Ҫ�����Ͻ�������ɾ���� �������ϴ��ݵ�·���ϵ������������Σ�ʹ�価����С��ֱ�����ڵ㡣
	 * <p>
	 * <b>����CT1��</b>��ʼ��������N=L������QΪɾ���ڵ�ļ��ϣ���ʼ����ʱ�򽫴������ÿա�<br>
	 * <b>����CT2��</b>���Ҹ���Ŀ��ע���Ǹ���Ŀ�����Ǹ��ڵ㡪�����N�Ǹ��ڵ㣬ת������CT6��
	 * ���N���Ǹ��ڵ㣬��PΪN�ĸ��ڵ㣬����EnΪP�д���N���Ǹ���Ŀ��<br>
	 * <b>����CT3��</b>ɾ�������㡪�����N�е���Ŀ��С��m����ζ�Žڵ�N���磬��ʱӦ����En��P���Ƴ�������N����Q��<br>
	 * <b>����CT4��</b>�����������Ρ������Nû�б�ɾ���������En����������EnI��ʹ�価����С��ǡ�ð���N�е�������Ŀ��<br>
	 * <b>����CT5��</b>����һ�㡪����N=P�����ز���CT2����ִ�С�<br>
	 * <b>����CT6��</b>���²��������Ŀ������Q�����нڵ��������Ŀִ�����²��롣Ҷ�ڵ��е���Ŀ
	 * ʹ���㷨Insert���²��뵽����Ҷ�ڵ��У��ϸ߲�ڵ��е���Ŀ������뵽���Ľϸ�λ���ϡ�
	 * ����Ϊ�˱�֤��Щ�ϸ߲�ڵ��µ�������Ҷ�ӽڵ㡢������Ҷ�ӽڵ��ܹ�������ͬһ���ϡ�<br>
	 * --------------------------------------------------------------------
	 * <p>
	 * Ҷ�ڵ�L�иո�ɾ����һ����Ŀ��������������Ŀ��̫�ٶ����磬��ɾ���ý�㣬ͬʱ���ý����ʣ�����Ŀ�ض�λ����������С�
	 * ����б�Ҫ��Ҫ�����Ͻ�������ɾ�����������ϴ��ݵ�·���ϵ�����������Σ�ʹ�価����С��ֱ�����ڵ㡣
	 * 
	 * @param list
	 *            �洢ɾ�������ʣ����Ŀ
	 */
	protected void condenseTree(List<RTNode> list)
	{
		if (isRoot()) {
			// ���ڵ�ֻ��һ����Ŀ�ˣ���ֻ�����ӻ����Һ���
			if (!isLeaf() && usedSpace == 1) {
				RTNode n = tree.file.readNode(branches[0]);
				tree.file.deletePage(n.pageNumber);
				n.pageNumber = 0;
				n.parent = Constants.NIL;//?
				tree.file.writeNode(n);//������д��
				if (!n.isLeaf()) {
					for (int i = 0; i < n.usedSpace; i++) {
						RTNode m = ((RTDirNode)n).getChild(i);
						m.parent = 0;//?
						tree.file.writeNode(m);//parent�����б仯����д��
					}
				}
			}
		} else {
			RTNode p = getParent();
			int e;
			//�ڸ��ڵ����ҵ��˽�����Ŀ
			for (e = 0; e < p.usedSpace; e++) {
				if (pageNumber == p.branches[e])
					break;
			}
			
			int min = Math.round(tree.getNodeCapacity()
					* tree.getFillFactor());
			if (usedSpace < min) {
				p.deleteData(e);
				list.add(this);// ֮ǰ�Ѿ�������ɾ����
			} else {
				p.datas[e] = getNodeRectangle();
			}
			tree.file.writeNode(p);
			p.condenseTree(list);
		}
	}

	/**
	 * Ϊ�ӽڵ�������(���·���)
	 * <b>���ѽ���ƽ���㷨</b>
	 * <p>
	 * 1��Ϊ������ѡ���һ����Ŀ(����)--�����㷨pickSeeds()��Ϊ������ѡ���һ��Ԫ�أ��ֱ��ѡ�е�������Ŀ(����)���䵽�����鵱�С�<br>
	 * 2������Ƿ��Ѿ�������ϣ����һ�����е���Ŀ̫�٣�Ϊ�������磬��ʣ���������Ŀȫ�����䵽������У��㷨��ֹ<br>
	 * 3������pickNext��ѡ����һ�����з������Ŀ--�����ÿ����Ŀ����ÿ����֮�������������ѡ�����������������������Ŀ����,
	 * 	    ���������������ѡ�������С���飬�����Ҳ�����ѡ����Ŀ�����ٵ���<br>
	 * 
	 * @param rectangle
	 *            ���·��ѵ����Rectangle
	 * @param page 
	 * 			      ������ѵĺ��ӽ�㱻�洢��page��������ѷ�����Ҷ�ӽ����Ϊ-1,
	 * @return �������е���Ŀ������
	 */
	protected int[][] quadraticSplit(Rectangle rectangle, int page)
	{
		if (rectangle == null) {
			throw new IllegalArgumentException("Rectangle cannot be null.");
		}

		datas[usedSpace] = rectangle; // ����ӽ�ȥ
		branches[usedSpace] = page;
		int total = usedSpace + 1; // �������

		// ��Ƿ��ʵ���Ŀ
		int[] mask = new int[total];
		for (int i = 0; i < total; i++) {
			mask[i] = 1;
		}

		// ÿ����ֻ����total/2����Ŀ
		int c = total / 2 + 1;
		// ÿ�������С��Ŀ����
		int minNodeSize = Math.round(tree.getNodeCapacity()
				* tree.getFillFactor());
		// ����������
		if (minNodeSize < 2)
			minNodeSize = 2;

		// ��¼û�б�������Ŀ�ĸ���
		int rem = total;

		int[] group1 = new int[c];// ��¼�������Ŀ������
		int[] group2 = new int[c];// ��¼�������Ŀ������
		// ���ٱ�����ÿ�������Ŀ������
		int i1 = 0, i2 = 0;

		int[] seed = quadraticPickSeeds();
		group1[i1++] = seed[0];
		group2[i2++] = seed[1];
		rem -= 2;
		mask[group1[0]] = -1;
		mask[group2[0]] = -1;

		while (rem > 0) {
			// ��ʣ���������Ŀȫ�����䵽group1���У��㷨��ֹ
			if (minNodeSize - i1 == rem) {
				for (int i = 0; i < total; i++)// �ܹ�rem��
				{
					if (mask[i] != -1)// ��û�б�����
					{
						group1[i1++] = i;
						mask[i] = -1;
						rem--;
					}
				}
				// ��ʣ���������Ŀȫ�����䵽group1���У��㷨��ֹ
			} else if (minNodeSize - i2 == rem) {
				for (int i = 0; i < total; i++)// �ܹ�rem��
				{
					if (mask[i] != -1)// ��û�б�����
					{
						group2[i2++] = i;
						mask[i] = -1;
						rem--;
					}
				}
			} else {
				// ��group1��������Ŀ����С�������
				Rectangle mbr1 = (Rectangle) datas[group1[0]].clone();
				for (int i = 1; i < i1; i++) {
					mbr1 = mbr1.getUnionRectangle(datas[group1[i]]);
				}
				// ��group2��������Ŀ���������
				Rectangle mbr2 = (Rectangle) datas[group2[0]].clone();
				for (int i = 1; i < i2; i++) {
					mbr2 = mbr2.getUnionRectangle(datas[group2[i]]);
				}

				// �ҳ���һ�����з������Ŀ
				double dif = Double.NEGATIVE_INFINITY;
				double areaDiff1 = 0, areaDiff2 = 0;
				int sel = -1;
				for (int i = 0; i < total; i++) {
					if (mask[i] != -1)// ��û�б��������Ŀ
					{
						// �����ÿ����Ŀ����ÿ����֮�������������ѡ�����������������������Ŀ����
						Rectangle a = mbr1.getUnionRectangle(datas[i]);
						areaDiff1 = a.getArea() - mbr1.getArea();

						Rectangle b = mbr2.getUnionRectangle(datas[i]);
						areaDiff2 = b.getArea() - mbr2.getArea();

						if (Math.abs(areaDiff1 - areaDiff2) > dif) {
							dif = Math.abs(areaDiff1 - areaDiff2);
							sel = i;
						}
					}
				}

				if (areaDiff1 < areaDiff2)// �ȱȽ��������
				{
					group1[i1++] = sel;
				} else if (areaDiff1 > areaDiff2) {
					group2[i2++] = sel;
				} else if (mbr1.getArea() < mbr2.getArea())// �ٱȽ��������
				{
					group1[i1++] = sel;
				} else if (mbr1.getArea() > mbr2.getArea()) {
					group2[i2++] = sel;
				} else if (i1 < i2)// ���Ƚ���Ŀ����
				{
					group1[i1++] = sel;
				} else if (i1 > i2) {
					group2[i2++] = sel;
				} else {
					group1[i1++] = sel;
				}
				mask[sel] = -1;
				rem--;

			}
		}// end while

		int[][] ret = new int[2][];
		ret[0] = new int[i1];
		ret[1] = new int[i2];

		for (int i = 0; i < i1; i++) {
			ret[0][i] = group1[i];
		}
		for (int i = 0; i < i2; i++) {
			ret[1][i] = group2[i];
		}
		return ret;
	}

	/**
	 * �����γ����������ľ�����ϵ�uniqueID
	 *
	 * 1����ÿһ����Ŀ(��������)E1��E2�������Χ���ǵ�Rectangle J(һ���ϴ�ľ���)������ �������:d = area(J) - area(E1) - area(E2);<br>
	 * 2��Choose the pair with the largest d
	 * 
	 * @return ����������Ŀ�������һ�������������ռ����Ŀ����
	 * 			�����γ����������ľ�����ϵ�uniqueID
	 */
	protected int[] quadraticPickSeeds()
	{
		double inefficiency = Double.NEGATIVE_INFINITY;
		int i1 = 0, i2 = 0;

		//
		for (int i = 0; i < usedSpace; i++) {
			for (int j = i + 1; j <= usedSpace; j++)// ע��˴���jֵ
			{
				Rectangle rectangle = datas[i].getUnionRectangle(datas[j]);
				double d = rectangle.getArea() - datas[i].getArea()
						- datas[j].getArea();

				if (d > inefficiency) {
					inefficiency = d;
					i1 = i;
					i2 = j;
				}
			}
		}
		return new int[] { i1, i2 };
	}

//	/**
//	 * @return
//	 */
	// public int[] linearPickSeeds()
	// {
	//
	// }

	
	
	/**
	 * ������С�������(MBR)
	 * @return ���ذ��������������Ŀ����СRectangle
	 */
	@Override
	public Rectangle getNodeRectangle()
	{
		if (usedSpace > 0) {
			Rectangle[] rectangles = new Rectangle[usedSpace];
			System.arraycopy(datas, 0, rectangles, 0, usedSpace);
			Rectangle ret = Rectangle.getUnionRectangle(rectangles);
//			if (ret.getHilbertValue() > lhv) {
//				lhv = ret.getHilbertValue();
//			}
			return ret;
		} else {
			return new Rectangle(new Point(new double[] { 0, 0 }),
								 new Point(new double[] { 0, 0 }));
		}
	}

	/**
	 * @return �Ƿ���ڵ�
	 */
	@Override
	public boolean isRoot() {
		return (parent == Constants.NIL);
	}

	/**
	 * @return �Ƿ��Ҷ�ӽ��
	 */
	@Override
	public boolean isIndex() {
		return (level != 0);
	}

	/**
	 * @return �Ƿ�Ҷ�ӽ��
	 */
	@Override
	public boolean isLeaf() {
		return (level == 0);
	}

	@Override
	public String toString() {
		String s = "< Page: " + pageNumber + ", Level: " + level 
				+ ", UsedSpace: " + usedSpace + ", Parent: " + parent + " >\n";
		
		for (int i = 0; i < usedSpace; i++) {
		    s += "  " + (i + 1) + ") " + datas[i].toStr() + " --> " + " page: " + branches[i] + "\n";
		}
		
		return s;
	}

	/**
	 * �ǳ��ؼ�������ץ·
	 *
	 *
	 * ��R���ĸ��ڵ��ΪT�������㷨Ҫ������һ����������S�����������S�ཻ��������¼��<br>
	 * <b>����S1��</b>���������������T����һ��Ҷ�ڵ㣬�������е�ÿһ����ĿE�����EI��S�ཻ��
	 * ���Ep��ָ����Ǹ��������ڵ����Search�㷨������ע�⣬Search�㷨���յ�
	 * ����Ϊһ�����ڵ㣬�����������㷨��ʱ��ԭ�ĳƶ��������ڵ����Search�㷨�� �����Ƕ���������Search�㷨��<br>
	 * <b>����S2��</b>����Ҷ�ڵ㡪�����T��һ��Ҷ�ڵ㣬�������е�ÿ����ĿE�����EI��S�ཻ�� ��E������Ҫ���صļ������֮һ��
	 * 
	 * ����һ����������S�����������S�ཻ��������¼ �Ӹ��ڵ㿪ʼ�������������ǵ���� ��������
	 * ����Ҷ�ڵ㣬������е�ÿ����ĿE�����������������S�ཻ����E������Ҫ���صļ������֮һ
	 * 
	 * @param region
	 * @return List<Rectangle>
	 */
	public List<Rectangle> search(Rectangle region, RTNode root)
	{
		return null;

	}

	/**
	 * ���ڹ���
	 * ����һ������
	 * ������:���������Ӧ�����ĸ�Ҷ�ڵ���?
	 * �������жϱ�׼
	 *
	 * <b>����CL1��</b>��ʼ��������R���ĸ��ڵ�ΪN��<br>
	 * <b>����CL2��</b>���Ҷ�ڵ㡪�����N�Ǹ�Ҷ�ڵ㣬����N<br>
	 * <b>����CL3��</b>ѡ�������������N����Ҷ�ڵ㣬���N�����е���Ŀ��ѡ��һ����ѵ���ĿF��
	 * ѡ��ı�׼�ǣ����E����F��F����������FI������С����F������ѵ���Ŀ�����������
	 * ��Ŀ�ڼ���E���������ε����ų̶���ȣ�������������ѡ���������ν�С���Ǹ���<br>
	 * <b>����CL4��</b>����Ѱ��ֱ���ﵽҶ�ڵ㡪����Fpָ��ĺ��ӽڵ�ΪN��Ȼ�󷵻ز���CL2ѭ�����㣬 ֱ�����ҵ�Ҷ�ڵ㡣
	 * <p>
	 * 
	 * @param rectangle
	 * @return RTDataNode
	 */
	protected abstract RTDataNode chooseLeaf(Rectangle rectangle);

	/**
	 * ���ڲ���
	 * ����һ������
	 * ������:����������ĸ��ڵ���?
	 *
	 * R���ĸ��ڵ�ΪT�����Ұ���rectangle��Ҷ�ӽ��
	 * <p>
	 * 1�����T����Ҷ�ӽ�㣬���������T�е�ÿ����Ŀ�Ƿ��Χrectangle������Χ��ݹ����findLeaf()<br>
	 * 2�����T��һ��Ҷ�ӽ�㣬��������T�е�ÿ����Ŀ�ܷ�ƥ��rectangle<br>
	 * 
	 * @param rectangle
	 * @return ���ذ���rectangle��Ҷ�ڵ�
	 */
	protected abstract RTDataNode findLeaf(Rectangle rectangle);

}
