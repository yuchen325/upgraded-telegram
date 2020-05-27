/**
 * Viterbi算法是个什么东西?
        在回答这个问题之前,我们先了解一下Viterbi算法位于什么样的知识脉络之中。
        上一次的PPT已经讲过,HMM是一种动态混合模型(又称状态空间模型)，此类模型研究的问题大致可分为如下两类:
        Learning
        通过机器学习，求解模型中的位置参数
        Influence
        通过数据(后果)推断后验分布P(Z|X)
        Influence又可以分支出多个子问题,其中有一个叫做Decoding的问题分支,
            要解决的问题是:对于给定的观测序列(O_1,O_2,…,O_T),求解使得P(I|O,π)最大的状态序列(i_1,i_2,…,i_T)
        即求解I ?=(?argmax P(I│O,π))┬I
        Viterbi算法就是用来解决上述问题的。其核心思想可概括为:递推求解每一时刻概率值最大的状态转移,得出的状态序列就是概率值最大的状态序列
        Viterbi算法的具体实现我参考了网上找来的代码,不过解读工作还未完全完成(80%),如下:
        对Viterbi源码实现的解读  **/
/**
 * Copyright (C) 2015-2016, BMW Car IT GmbH and BMW AG
 * Author: Stefan Holder (stefan.holder@bmw.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hmmLib;

import java.util.*;

/**
 * Implementation of the Viterbi algorithm for time-inhomogeneous Markov processes,
 * meaning that the set of states and state transition probabilities are not necessarily fixed
 * for all time steps. The plain Viterbi algorithm for stationary Markov processes is described e.g.
 * in Rabiner, Juang, An introduction to Hidden Markov Models, IEEE ASSP Mag., pp 4-16, June 1986.
 * 这个class,基于 时间-非齐次Markov过程 实现了Viterbi算法
 * 这意味着没必要确定所有时刻的状态和状态转移
 * 基于静态的 Markov过程的清晰的Vitrbi算法，参见
 * e.g. in Rabiner, Juang, An introduction to Hidden Markov Models, IEEE ASSP Mag., pp 4-16, June 1986.
 *
 * <p>Generally expects logarithmic probabilities as input to prevent arithmetic underflows for
 * small probability values.
 * <p> 通常希望输入概率的对数值，以防止小概率值的算术下溢
 *
 * transition descriptor (隐)状态转移的实体化描述对象,各个实际位置GPS点在时间轴上的先后指向关系
 * back pointer 后向指针,记录着transition descriptor的转移关系(只不过是逆序),用于在算出最优序列后反向回溯推算出其具体内容
 *
 * <p>This algorithm supports storing transition objects in
 * {@link #nextStep(Object, Collection, Map, Map, Map)}. For instance if a HMM is
 * used for map matching, this could be routes between road position candidates.
 * The transition descriptors of the most likely sequence can be retrieved later in
 * {@link SequenceState#transitionDescriptor} and hence do not need to be stored by the
 * caller. Since the caller does not know in advance which transitions will occur in the most
 * likely sequence, this reduces the number of transitions that need to be kept in memory
 * from t*n? to t*n since only one transition descriptor is stored per back pointer,
 * where t is the number of time steps and n the number of candidates per time step.
 * 这个算法允许将transition对象存储在{@link #nextStep(Object, Collection, Map, Map, Map)}(链表?)中
 * 举个例子,如果这个HMM应用在地图匹配中,那么{@link #nextStep(Object, Collection, Map, Map, Map)}存的就应该是
 * 公路上的候选匹配点之间的路段。
 * 这些最有可能的候选序列的状态转移(transition descriptors),事后可以在{@link SequenceState#transitionDescriptor}中找到，
 * 因此调用者不必去存储它们。
 * 由于调用者事先不知道在候选序列中会发生哪个状态转移，由于每个后向指针(back pointer)中只存储着一个状态转移
 * 对状态转移数量的reduce操作需要在内存中保持t*n^2~t*n的复杂度
 * 这里的t是时刻的数量,n是每个时刻候选者的数量
 *
 * <p>For long observation sequences, back pointers usually converge to a single path after a
 * certain number of time steps. For instance, when matching GPS coordinates to roads, the last
 * GPS positions in the trace usually do not affect the first road matches anymore.
 * This implementation exploits this fact by letting the Java garbage collector
 * take care of unreachable back pointers. If back pointers converge to a single path after a
 * constant number of time steps, only O(t) back pointers and transition descriptors need to be
 * stored in memory.
 * 对于较长的观测序列,后向指针常常在特定数目的时刻之后汇聚单独的一条路径
 * 例如，匹配GPS点到公路上的时候,轨迹上最新的GPS点往往不再影响第一个路段匹配
 * 这个算法实现考虑到了上述情况:让Java拾荒者去关照那些不可达的后向指针
 * 如果后向指针在连续多个时刻之后汇聚成了单独一条路径,那么在内存中只需要存储数量级为O(t)的后向指针和状态转移
 *
 * @param <S> the state type  <S> 状态变量的数据类型
 * @param <O> the observation type <O> 观测变量的数据类型
 * @param <D> the transition descriptor type. Pass {@link Object} if transition descriptors are not
 * needed. <D> 状态转移的数据类型,如果不需要状态转移的话,就用{@link Object}糊弄过去
 */
public class ViterbiAlgorithm<S, O, D>
{

    /**
     * Stores addition information for each candidate.
     * 下面这个静态类,名叫详细状态(ExtendedState)
     * 定义了一个用于存储state相关信息的存储结构。其中包括:状态变量,观测变量,状态转移Desc,后向指针
     */
    private static class ExtendedState<S, O, D> // 详细状态
    {

        S state;    //成员1，状态变量

        /**
         * Back pointer to previous state candidate in the most likely sequence.
         * Back pointers are chained using plain Java references.
         * This allows garbage collection of unreachable back pointers.
         * 在最有可能的序列中,后向指针指向前一个候选者的状态
         * 连接后向指针使用的是普通的Java reference
         * 可以对不可达的后向指针进行垃圾回收
         */
        ExtendedState<S, O, D> backPointer; //成员2 一个后向指针,嵌套声明(reference)看到没,指代上一时刻的详细状态
        //由于Java中没有指针,所以这里用了一个嵌套声明,达到了相同的效果

        O observation; //成员3,观测变量
        D transitionDescriptor; //成员4,状态转移Desc

        // 构造方法
        ExtendedState(S state,
                      ExtendedState<S, O, D> backPointer,
                      O observation, D transitionDescriptor) {
            this.state = state;
            this.backPointer = backPointer;
            this.observation = observation;
            this.transitionDescriptor = transitionDescriptor;
        }
    }


    /**
     * 下面这个静态类,定义了 前向算法中每个递推结果的结构
     * 其中包含两个Map类型的成员变量:新消息?,新的详细状态
     * @param <S> 状态变量
     * @param <O> 观测变量
     * @param <D> 状态转移Desc
     */
    private static class ForwardStepResult<S, O, D> //存储前向推算的结果--StateSequence 包含两个link,
    {
        // 这个类中的两个成员变量都是链表?
        // final修饰,不可变
        final Map<S, Double> newMessage; // 成员1，一个由<state,对应的概率>键值对组成的link

        /**
         * Includes back pointers to previous state candidates for retrieving the most likely
         * sequence after the forward pass.
         * 下面这个成员内部包含了指向上一时刻状态的后向指针,在前向推算结束后,用于回溯得出可能性最大的序列
         */
        final Map<S, ExtendedState<S, O, D>> newExtendedStates;
        // 成员2,一个link,其中的元素是一个个<state,对应的ExtendedState>键值对

        // 构造方法
        ForwardStepResult(int numberStates)
        {                   // numberStates,状态的个数,也就是时刻的个数?

            // 新消息,一个由<state,对应的概率>键值对组成的link
            newMessage = new LinkedHashMap<S, Double>(Utils.initialHashMapCapacity(numberStates));
            // 成员2,一个link,其中的元素是一个个<state,对应的ExtendedState>键值对
            newExtendedStates = new LinkedHashMap<S, ExtendedState<S, O, D>>(Utils.initialHashMapCapacity(numberStates));
        }
    }

    /**
     * 也是一个键值对link,元素是一个个<state,Extended>
     * 根据齐次马尔科夫假设:next时刻的state只与last时刻的state相关,这就是lastExtendedState存在的意义
     * Allows to retrieve the most likely sequence using back pointers.
     * 可以使用它里面的后向指针作为起点,回溯推演出隐状态序列
     * 它是一个 <状态点,详细状态>链表
     */
    private Map<S, ExtendedState<S, O, D>> lastExtendedStates; // 最后一个详细状态,一个单独的成员变量

    /**
     * 一个私有成员,用于存储此前的隐状态序列?
     */
    private Collection<S> prevCandidates; // 一个集合,先前的候选者 一个单独的成员变量

    /**
     * For each state s_t of the current time step t, message.get(s_t) contains the log
     * probability of the most likely sequence ending in state s_t with given observations
     * o_1, ..., o_t.
     * 对于当前时刻t下的每一个状态变量(s_t),给定观测变量o_1,o_2,...,o_t
     * message.get(s_t)包含了最大概率的隐状态序列在状态s_t结束的概率的对数值
     *
     * Formally, this is max log p(s_1, ..., s_t, o_1, ..., o_t) w.r.t. s_1, ..., s_{t-1}.
     * Note that to compute the most likely state sequence, it is sufficient and more
     * efficient to compute in each time step the joint probability of states and observations
     * instead of computing the conditional probability of states given the observations.
     * 形式上看,这是p(s_1, ..., s_t, o_1, ..., o_t)关于(s_1, ..., s_{t-1})的最大概率
     * 注意,在计算隐状态序列时,计算:每个时刻状态变量、观测变量对接的概率 比 计算:给定观测变量后状态变量的条件概率
     * 更加充分而高效
     */

    /**
     * 用于存储最大概率序列中的一个个state及其对应的log概率
     * 一个link,其中的元素是一个个<state,对应的概率的对数值>键值对
     */
    private Map<S, Double> message; // 它的get(s_t)方法会返回隐状态序列在状态s_t结束的概率的对数值?

    public Map<S, Double> getMessage()
    {
        return message;
    }

    /**
     * 一个私有成员,用于标志HMM链是否断裂
     */
    private boolean isBroken = false; //HMM断了没?

    /**
     * 一个私有成员,用于记录历史消息
     */
    private List<Map<S, Double>> messageHistory; // 历史消息，只在调试时用.For debugging only.

    /**
     * 构造方法 1
     * Need to construct a new instance for each sequence of observations.
     * Does not keep the message history.
     * 需要为每一个观测变量序列构建新实例
     * 不保留历史消息
     */
    public ViterbiAlgorithm() // constructor 1
    {
        this(false); //把false传给被调用方法的keepMessageHistory参数
    }

    /**
     * 构造方法 2
     * Need to construct a new instance for each sequence of observations.
     * @param keepMessageHistory Whether to store intermediate forward messages
     * (probabilities of intermediate most likely paths) for debugging.
     * 需要为每一个观测变量序列构建新实例
     * @param keepMessageHistory 这个参数决定：为了调试,是否存储前向推算过程中的中间信息(隐状态序列的中间概率值)?
     */
    public ViterbiAlgorithm(boolean keepMessageHistory)
    {
        if (keepMessageHistory) // 若为true
        {
            messageHistory = new ArrayList<Map<S, Double>>(); // 则创建messageHistory
        }
    }

    /**
     * 启动点 1
     *
     * Lets the HMM computation start with the given initial state probabilities.
     * 从给定的初始状态概率,开始HMM的计算
     *
     * 参数说明:
     * @param initialStates Pass a collection with predictable iteration order such as
     * {@link ArrayList} to ensure deterministic results.
     *                      这个参数传递一个容器进来,里面是预测的迭代顺序
     *                      例如{@link ArrayList} ,来确认决定性结果
     *
     * @param initialLogProbabilities Initial log probabilities for each initial state.
     *                                每一个初始状态的初始对数概率
     *
     * @throws NullPointerException if any initial probability is missing
     *                              应对:初始概率缺失
     *
     * @throws IllegalStateException if this method or
     * {@link #startWithInitialObservation(Object, Collection, Map)}
     * has already been called
     *                               应对:如果这个方法 or {@link #startWithInitialObservation(Object, Collection, Map)}
     *                               已经被调用过了
     */
    public void startWithInitialStateProbabilities(Collection<S> initialStates,
                                                   Map<S, Double> initialLogProbabilities)
    {
        initializeStateProbabilities(null, initialStates, initialLogProbabilities); // 点火器
    }

    /**
     * 启动点 2
     *
     * Lets the HMM computation start at the given first observation and uses the given emission
     * probabilities as the initial state probability for each starting state s.
     * 从给定的第一个观测变量开始HMM的计算,用给定的发射概率作为每个初始状态s的初始状态概率
     *
     * @param candidates Pass a collection with predictable iteration order such as
     * {@link ArrayList} to ensure deterministic results.
     *                   这个参数传递一个容器进来,里面是预测的迭代顺序
     *                   例如{@link ArrayList} ,来确认决定性结果
     * @param emissionLogProbabilities Emission log probabilities of the first observation for
     * each of the road position candidates.
     *                                 每个路段候选点的第一个观测变量的发射对数概率
     *
     * @throws NullPointerException if any emission probability is missing
     *                              应对:发射概率缺失
     *
     * @throws IllegalStateException if this method or
     * {@link #startWithInitialStateProbabilities(Collection, Map)}} has already been called
     *                               应对:如果这个方法 or {@link #startWithInitialObservation(Object, Collection, Map)}
     *                               已经被调用过了
     */
    public void startWithInitialObservation(O observation, Collection<S> candidates,
                                            Map<S, Double> emissionLogProbabilities)
    {
        initializeStateProbabilities(observation, candidates, emissionLogProbabilities);  // 点火器
    }
    /**
     * @param observation Use only if HMM only starts with first observation.
     *                    这个参数.第一个时刻的观测变量,也就是第一个GPS点
     * @param candidates 第一个时刻的全体candidate(候选匹配点)组成的集合
     * @param initialLogProbabilities 一个link，元素是一个个<candidate,对应的log概率>键值对
     * 下面这个方法,用于初始化状态概率
     */
    private void initializeStateProbabilities(O observation, Collection<S> candidates,
                                              Map<S, Double> initialLogProbabilities)
    {
        if (message != null) // 先检查message是不是空的
        {
            throw new IllegalStateException("Initial probabilities have already been set.");
        }

        // Set initial log probability for each start state candidate based on first observation.
        // 为每一个基于初始观测变量的候选状态点求出概率的对数值
        // Do not assign initialLogProbabilities directly to message to not rely on its iteration
        // 不要直接对initialLogProbabilities赋值
        // order.

        final Map<S, Double> initialMessage = new LinkedHashMap<S, Double>();    // 初始化message,这是一个<候选状态点,对应的对数概率值>链表

        for (S candidate : candidates) // 遍历候选状态点
        {
            final Double logProbability = initialLogProbabilities.get(candidate); // 检查这些candidate的log概率

            if (logProbability == null) //如果log概率值为空
            {
                throw new NullPointerException("No initial probability for " + candidate); // 抛出异常,这个候选状态点没有初始化概率
            }

            // 否则,将这个候选状态点及其对数概率加入initialMessage
            initialMessage.put(candidate, logProbability);
        }

        if (initialMessage.size()<1)
        {
            System.out.println("initial message is null!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }

        System.out.println("location:ViterbiAlgorithm.initializeStateProbabilities,the length of initialMessage is "+initialMessage.size());

        /*for (double logProbability : message.values())  // 遍历message中的概率值
        {
            // 如果存在非0概率值,那么HMM链就没断
            if (logProbability != Double.NEGATIVE_INFINITY) // 如果概率值取对数之后不等于负无穷(概率不为0)
            {
                return false;                               // HMM链就没断
            }
        }*/
        message = initialMessage;



        // 根据初始消息的内容，判断HMM链断裂了没
        isBroken = hmmBreak(initialMessage);

        // 如果断了,说拜拜
        if (isBroken)
        {
            System.out.println("hmm is broken when the initializeStateProbabilities");

            return;
        }

        // 如果历史消息不为空,就把新消息加进去 ？这是什么操作
        if (messageHistory != null)
        {
            messageHistory.add(message);
        }

        // 初始化lastExtendedStates这个成员
        lastExtendedStates = new LinkedHashMap<S, ExtendedState<S, O, D>>();

        // 遍历当前时刻(也就是第一个时刻)的所有候选状态点
        for (S candidate : candidates)
        {
            // 把所有候选状态点及其详细信息加入lastExtendStates
            lastExtendedStates.put(candidate,
                    new ExtendedState<S, O, D>(candidate, null, observation, null));
            // 由于是第一个时刻,所以backPointer和transitionDescriptor都是null
        }

        // 把第一个时刻的全体候选点初始化赋值给prevCandidates
        prevCandidates = new ArrayList<S>(candidates); // Defensive copy.以防万一

        // 如果断了,说拜拜
        if (isBroken)
        {
            System.out.println("hmm is broken when the initializeStateProbabilities");

            return;
        }
    }


    /**
     * nextStep的另一个入口
     * See {@link #nextStep(Object, Collection, Map, Map, Map)}
     */
    public void nextStep(O observation, Collection<S> candidates,
                         Map<S, Double> emissionLogProbabilities,
                         Map<Transition<S>, Double> transitionLogProbabilities)
    {
        nextStep(observation, candidates, emissionLogProbabilities, transitionLogProbabilities,
                new LinkedHashMap<Transition<S>, D>());
    }
    /**
     * Processes the next time step. Must not be called if the HMM is broken.
     * 计算并记录下一个时刻(当前时刻)的隐状态.如果HMM链断开就用不了了
     *
     * @param candidates Pass a collection with predictable iteration order such as
     * {@link ArrayList} to ensure deterministic results.
     *                   candidates,这是当前时刻的候选state点集合.这个参数传递一个容器进来
     *
     * @param emissionLogProbabilities Emission log probabilities for each candidate state.
     *                                 每个候选状态点对应的发射log概率
     *
     * @param transitionLogProbabilities Transition log probability between all pairs of candidates.
     * A transition probability of zero is assumed for every missing transition.
     *                                   这个参数,是候选路段之间所有转移组合对应的概率(Transition log probability)
     *                                   缺失的转移组合的概率用0来填补
     *
     * @param transitionDescriptors Optional objects that describes the transitions.
     *                              这个参数,用来描述状态转移的实体化对象
     *
     * @throws NullPointerException if any emission probability is missing
     *                              应对:发射概率缺失
     *
     * @throws IllegalStateException if neither
     * {@link #startWithInitialStateProbabilities(Collection, Map)} nor
     * {@link #startWithInitialObservation(Object, Collection, Map)}
     * has not been called before or if this method is called after an HMM break has occurred
     *                              应对:
     *                              1. {@link #startWithInitialStateProbabilities(Collection, Map)}
     *                                 {@link #startWithInitialObservation(Object, Collection, Map)}
     *                                 都没有被调用过
     *                              2. HMM链断开后调用本方法
     */
    public void nextStep(O observation, Collection<S> candidates,
                         Map<S, Double> emissionLogProbabilities,
                         Map<Transition<S>, Double> transitionLogProbabilities,
                         Map<Transition<S>, D> transitionDescriptors)
    {
        //System.out.println("begin:one step to next moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // 两个if用来处理异常
        if (message == null)
        {
            throw new IllegalStateException(
                    "startWithInitialStateProbabilities() or startWithInitialObservation() "
                            + "must be called first.");
        }

        if (isBroken)
        {
            System.out.println("location:ViterbiAlgorithm.nextStep,hmm is broken before forwardStep");

            throw new IllegalStateException("Method must not be called after an HMM break.");
        }


        // Forward step 调用了forwardSetp这个方法(在后面实现了),计算出下一个隐状态forwardStepResult(一个ForwardStepResult对象)
        ForwardStepResult<S, O, D> forwardStepResult = forwardStep(observation, prevCandidates,
                candidates, message, emissionLogProbabilities, transitionLogProbabilities,
                transitionDescriptors);

        isBroken = hmmBreak(forwardStepResult.newMessage); // 记录HMM链的状态(调用后面实现的hmmBreak方法)

        if (isBroken) return; // 如果HMM链断了,计算失败,说拜拜

        // 否则

        // 如果历史消息不为null
        if (messageHistory != null)
        {
            messageHistory.add(forwardStepResult.newMessage); //就把新的消息填进去
        }

        // 假设当前时刻有k个候选点
        message = forwardStepResult.newMessage;    //记录最新时刻k个<候选状态点,累计至上一时刻的最"短"序列概率值>
        lastExtendedStates = forwardStepResult.newExtendedStates; //记录最新时刻的k个<候选状态点,对应的ExtendedState>

        prevCandidates = new ArrayList<S>(candidates); // Defensive copy.保险型copy?

        //System.out.println("end:one step to next moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
    /**
     *         //不出意外,如果当前时刻有k个候选匹配点 (curState:当前时刻候选点;max prevState:curState => max pervState的转移概率最大)
     *         // 那么result.newMessage中会包含k个<当前时刻候选匹配点,对应的max log概率> max
     *         // result.newExtendedStates中会包含k个<当前时刻候选匹配点,对应的ExtendedState(其中的back pointer执行max prevState)>
     * Computes the new forward message and the back pointers to the previous states.
     * 计算新的状态点及其对应的log概率,并将其添加到message中
     * 还计算了指向上一时刻状态点的后向指针
     * @throws NullPointerException if any emission probability is missing
     *                              应对:存在 发射概率缺失
     * 参数说明:
     *  curCandidates: current candidates，当前时刻的所有候选状态点
     */
    private ForwardStepResult<S, O, D> forwardStep(O observation, Collection<S> prevCandidates,
                                                   Collection<S> curCandidates, Map<S, Double> message,
                                                   Map<S, Double> emissionLogProbabilities,
                                                   Map<Transition<S>, Double> transitionLogProbabilities,
                                                   Map<Transition<S>,D> transitionDescriptors)
    {
        final ForwardStepResult<S, O, D> result = new ForwardStepResult<S, O, D>(curCandidates.size());
        // 初始化一个不可变变量,用于记录结果

        assert !prevCandidates.isEmpty();
        // 断言:若prevCandidates不为空,则没问题,程序继续执行；否则,抛出AssertionError，并终止执行

        // 下面是一个双重for循环,求出我在这个方法的注释第一行提到的概率,并记录最大概率
        // 遍历当前时刻的所有候选状态点
        for (S curState : curCandidates)
        { // start_1
            double maxLogProbability = Double.NEGATIVE_INFINITY; // 初始化最大log概率

            S maxPrevState = null;                               // 初始化 最大概率组合的上一时刻候选状态点

            // 遍历上一时刻的所有候选状态点
            for (S prevState : prevCandidates)
            {
                // 由于所有概率值都是取对数之后的log概率,所以下面的概率运算都是加法

                // logProbability=prevState的发射log概率+previous state=>current state对应的转移log概率
                final double logProbability = message.get(prevState) + transitionLogProbability(
                        prevState, curState, transitionLogProbabilities);

                //previous state=>current state对应的概率概率是null,那么logProbability也是null,下面这个if就不会执行
                if (logProbability > maxLogProbability) //如果这个logProbability是最大值
                {
                    maxLogProbability = logProbability; // 记录maxLogProbability
                    maxPrevState = prevState;           // 记录对应的prevState
                }
            }

            // Throws NullPointerException if curState is not stored in the map.
            result.newMessage.put(curState, maxLogProbability
                    + emissionLogProbabilities.get(curState)); //在result中的newMessage中记录这一个当前时刻候选点的<state,最大log概率>

            // Note that maxPrevState == null if there is no transition with non-zero probability.
            // 对于这一个当前时刻候选状态点，如果logProbability(状态转移概率)全都是0,那么就把 最大概率组合的上一时刻状态 赋值为空
            // In this case curState has zero probability and will not be part of the most likely
            // 在上述情况下,这一个当前时刻候选状态点不可能成为最大概率序列的一部分,所以我们就不需要对应的详细状态了
            // sequence, so we don't need an ExtendedState.
            // 否则,那就是需要,就像下面这样
            if (maxPrevState != null) // 如果存在不是null的prevState
            {
                final Transition<S> transition = new Transition<S>(maxPrevState, curState);
                // 建立状态转移的实体化描述对象 <最大转移概率的previous state,当前时刻的候选状态点current state>

                // 为这个当前时刻候选状态点建立一个 详细状态信息的实体化描述对象
                final ExtendedState<S, O, D> extendedState = new ExtendedState<S, O, D>(curState,
                        lastExtendedStates.get(maxPrevState), observation,
                        transitionDescriptors.get(transition));

                // 将<当前时刻候选点state,对应的实体化描述对象> 存入result
                result.newExtendedStates.put(curState, extendedState);
            }
        } //end_1

        //不出意外,如果当前时刻有k个候选匹配点
        // 那么result.newMessage中会包含k个<当前时刻候选匹配点,对应的max log概率>
        // result.newExtendedStates中会包含k个<当前时刻候选匹配点,对应的ExtendedState>
        return result;
    }




    /**
     * getter of isBroken
     * Returns whether an HMM occurred in the last time step.
     * 告诉你HMM链断了没
     * An HMM break means that the probability of all states equals zero.
     * 断了就意味着所有状态概率等于0
     */
    public boolean isBroken()
    {
        return isBroken;
    }

    /**
     * getter of messageHistory
     *  Returns the sequence of intermediate forward messages for each time step.
     *  前向推算中每个时间step的中间值
     *  Returns null if message history is not kept.
     */
    public List<Map<S, Double>> messageHistory()
    {
        return messageHistory;
    }

    /**
     * another getter for messageHistory
     * 将messageHistory中的内容以String形式返回
     * @return
     */
    public String messageHistoryString()
    {
        if (messageHistory == null)
        {
            throw new IllegalStateException("Message history was not recorded.");
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("Message history with log probabilies\n\n");
        int i = 0;
        for (Map<S, Double> message : messageHistory)
        {
            sb.append("Time step " + i + "\n");
            i++;
            for (S state : message.keySet())
            {
                sb.append(state + ": " + message.get(state) + "\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns whether the specified message is either empty or only contains state candidates
     * with zero probability and thus causes the HMM to break.
     * 如果message是空的 or message中的候选状态点对应的log概率全为0
     * 就返回true -> HMM链断裂了
     */
    private boolean hmmBreak(Map<S, Double> message)
    {
        for (double logProbability : message.values())  // 遍历message中的概率值
        {
            // 如果存在非0概率值,那么HMM链就没断
            if (logProbability != Double.NEGATIVE_INFINITY) // 如果概率值取对数之后不等于负无穷(概率不为0)
            {
                return false;                               // HMM链就没断
            }
        }
        return true; //否则就断裂了
    }



    // 一个getter方法,从一个 {<previous state=>current state,对应的log概率>}中get到指定previous state=>current state对应的概率
    private double transitionLogProbability(S prevState, S curState, Map<Transition<S>,
            Double> transitionLogProbabilities)
    {
        final Double transitionLogProbability =
                transitionLogProbabilities.get(new Transition<S>(prevState, curState));
        if (transitionLogProbability == null)
        {
            return Double.NEGATIVE_INFINITY; // Transition has zero probability.
        } else {
            return transitionLogProbability;
        }
    }

    /**
     * 在前向推算结束后,用这个入口方法获得整个隐状态序列
     * Returns the most likely sequence of states for all time steps. This includes the initial
     * states / initial observation time step. If an HMM break occurred in the last time step t,
     * then the most likely sequence up to t-1 is returned. See also {@link #isBroken()}.
     * 返回整个时间轴上可能性最大的隐状态序列。
     * 包含了初始的初始状态时刻、初始观测时刻。
     * 如果HMM链在当前t时刻断裂,那就返回截止t-1时刻可能性最大的隐状态序列
     *
     * <p>Formally, the most likely sequence is argmax p([s_0,] s_1, ..., s_T | o_1, ..., o_T)
     * with respect to s_1, ..., s_T, where s_t is a state candidate at time step t,
     * o_t is the observation at time step t and T is the number of time steps.
     * 形式上讲,可能性最大的隐状态序列是p([s_0,] s_1, ..., s_T | o_1, ..., o_T)关于(s_1,s_T)的最大似然估计
     * 在这里，s_t是t时刻的状态变量,o_t是t时刻的观测变量,T和t表示时刻(数量)
     */
    public List<SequenceState<S, O, D>> computeMostLikelySequence()
    {
        if (message == null) // 应对退化和异常
        {
            // Return empty most likely sequence if there are no time steps or if initial
            // observations caused an HMM break.
            // 没有时间 or 初始观测变量导致HMM链断裂
            return new ArrayList<SequenceState<S, O, D>>(); //返回空
        } else { //否则

            return retrieveMostLikelySequence(); //调用这个方法,回溯生成最大概率state sequence
        }
    }

    /**
     * Retrieves the first state of the current forward message with maximum probability.
     *
     */
    private S mostLikelyState()
    {
        // 这个断言可以成立
        // 否则就意味着:HMM链断裂,message为空,程序报错退出
        // Otherwise an HMM break would have occurred and message would be null.
        assert !message.isEmpty();

        // 此时,message里面是最后时刻(也就是T)的k个<候选状态点,累计至上一时刻的最"短"序列概率值>


        S result = null; //初始化result,用于记录一个状态点

        double maxLogProbability = Double.NEGATIVE_INFINITY; // 初始化,这个变量用于记录最大概率的对数值

        // Map.Entry<S, Double>是一个<S, Double>类型键值对的提取器
        for (Map.Entry<S, Double> entry : message.entrySet())  // 遍历message中的enteySet里面的一个个<state,概率>键值对
        {
            if (entry.getValue() > maxLogProbability) // 如果 这个<state,概率>的概率是最大值了
            {
                result = entry.getKey();              // 记录这个<state,概率>中的state
                maxLogProbability = entry.getValue(); // 记录这个<state,概率>中的概率值
            }
        }

        // 如果result不为空
        assert result != null; // Otherwise an HMM break would have occurred.

        return result; //于是,result中记录了最后的T时刻message中拥有最大概率的state
    }

    /**
     * Retrieves most likely sequence from the internal back pointer sequence.
     * 用内部的后向指针序列,回溯推算出可能性最大的状态序列
     * 返回一个状态序列
     */
    private List<SequenceState<S, O, D>> retrieveMostLikelySequence()
    {
        // 这个断言可以成立
        // 否则就意味着:HMM链断裂,message为空,程序报错退出
        // Otherwise an HMM break would have occurred and message would be null.
        assert !message.isEmpty();

        final S lastState = mostLikelyState(); //先求出最后最新的那个匹配状态点

        // Retrieve most likely state sequence in reverse order
        // 反向回溯推算出状态序列

        final List<SequenceState<S, O, D>> result = new ArrayList<SequenceState<S, O, D>>(); // 初始化result

        ExtendedState<S, O, D> es = lastExtendedStates.get(lastState); // es是lastState的实体化描述对象

        while(es != null)
        {
            // 状态序列中的各个状态变量被表达为SequenceState对象
            final SequenceState<S, O, D> ss = new SequenceState<S, O, D>(es.state, es.observation,
                    es.transitionDescriptor);

            result.add(ss); //将SquenceState加入result

            es = es.backPointer; // 回溯到上一状态
        }
        // 这个while循环结束后,result里面是一个时间轴上的逆序序列
        // 下面将其置为顺序
        Collections.reverse(result);

        return result;
    }

}

