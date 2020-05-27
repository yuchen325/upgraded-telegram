/**
 * Viterbi�㷨�Ǹ�ʲô����?
        �ڻش��������֮ǰ,�������˽�һ��Viterbi�㷨λ��ʲô����֪ʶ����֮�С�
        ��һ�ε�PPT�Ѿ�����,HMM��һ�ֶ�̬���ģ��(�ֳ�״̬�ռ�ģ��)������ģ���о���������¿ɷ�Ϊ��������:
        Learning
        ͨ������ѧϰ�����ģ���е�λ�ò���
        Influence
        ͨ������(���)�ƶϺ���ֲ�P(Z|X)
        Influence�ֿ��Է�֧�����������,������һ������Decoding�������֧,
            Ҫ�����������:���ڸ����Ĺ۲�����(O_1,O_2,��,O_T),���ʹ��P(I|O,��)����״̬����(i_1,i_2,��,i_T)
        �����I ?=(?argmax P(I��O,��))��I
        Viterbi�㷨�������������������ġ������˼��ɸ���Ϊ:�������ÿһʱ�̸���ֵ����״̬ת��,�ó���״̬���о��Ǹ���ֵ����״̬����
        Viterbi�㷨�ľ���ʵ���Ҳο������������Ĵ���,�������������δ��ȫ���(80%),����:
        ��ViterbiԴ��ʵ�ֵĽ��  **/
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
 * ���class,���� ʱ��-�����Markov���� ʵ����Viterbi�㷨
 * ����ζ��û��Ҫȷ������ʱ�̵�״̬��״̬ת��
 * ���ھ�̬�� Markov���̵�������Vitrbi�㷨���μ�
 * e.g. in Rabiner, Juang, An introduction to Hidden Markov Models, IEEE ASSP Mag., pp 4-16, June 1986.
 *
 * <p>Generally expects logarithmic probabilities as input to prevent arithmetic underflows for
 * small probability values.
 * <p> ͨ��ϣ��������ʵĶ���ֵ���Է�ֹС����ֵ����������
 *
 * transition descriptor (��)״̬ת�Ƶ�ʵ�廯��������,����ʵ��λ��GPS����ʱ�����ϵ��Ⱥ�ָ���ϵ
 * back pointer ����ָ��,��¼��transition descriptor��ת�ƹ�ϵ(ֻ����������),����������������к��������������������
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
 * ����㷨����transition����洢��{@link #nextStep(Object, Collection, Map, Map, Map)}(����?)��
 * �ٸ�����,������HMMӦ���ڵ�ͼƥ����,��ô{@link #nextStep(Object, Collection, Map, Map, Map)}��ľ�Ӧ����
 * ��·�ϵĺ�ѡƥ���֮���·�Ρ�
 * ��Щ���п��ܵĺ�ѡ���е�״̬ת��(transition descriptors),�º������{@link SequenceState#transitionDescriptor}���ҵ���
 * ��˵����߲���ȥ�洢���ǡ�
 * ���ڵ��������Ȳ�֪���ں�ѡ�����лᷢ���ĸ�״̬ת�ƣ�����ÿ������ָ��(back pointer)��ֻ�洢��һ��״̬ת��
 * ��״̬ת��������reduce������Ҫ���ڴ��б���t*n^2~t*n�ĸ��Ӷ�
 * �����t��ʱ�̵�����,n��ÿ��ʱ�̺�ѡ�ߵ�����
 *
 * <p>For long observation sequences, back pointers usually converge to a single path after a
 * certain number of time steps. For instance, when matching GPS coordinates to roads, the last
 * GPS positions in the trace usually do not affect the first road matches anymore.
 * This implementation exploits this fact by letting the Java garbage collector
 * take care of unreachable back pointers. If back pointers converge to a single path after a
 * constant number of time steps, only O(t) back pointers and transition descriptors need to be
 * stored in memory.
 * ���ڽϳ��Ĺ۲�����,����ָ�볣�����ض���Ŀ��ʱ��֮���۵�����һ��·��
 * ���磬ƥ��GPS�㵽��·�ϵ�ʱ��,�켣�����µ�GPS����������Ӱ���һ��·��ƥ��
 * ����㷨ʵ�ֿ��ǵ����������:��Javaʰ����ȥ������Щ���ɴ�ĺ���ָ��
 * �������ָ�����������ʱ��֮���۳��˵���һ��·��,��ô���ڴ���ֻ��Ҫ�洢������ΪO(t)�ĺ���ָ���״̬ת��
 *
 * @param <S> the state type  <S> ״̬��������������
 * @param <O> the observation type <O> �۲��������������
 * @param <D> the transition descriptor type. Pass {@link Object} if transition descriptors are not
 * needed. <D> ״̬ת�Ƶ���������,�������Ҫ״̬ת�ƵĻ�,����{@link Object}��Ū��ȥ
 */
public class ViterbiAlgorithm<S, O, D>
{

    /**
     * Stores addition information for each candidate.
     * ���������̬��,������ϸ״̬(ExtendedState)
     * ������һ�����ڴ洢state�����Ϣ�Ĵ洢�ṹ�����а���:״̬����,�۲����,״̬ת��Desc,����ָ��
     */
    private static class ExtendedState<S, O, D> // ��ϸ״̬
    {

        S state;    //��Ա1��״̬����

        /**
         * Back pointer to previous state candidate in the most likely sequence.
         * Back pointers are chained using plain Java references.
         * This allows garbage collection of unreachable back pointers.
         * �����п��ܵ�������,����ָ��ָ��ǰһ����ѡ�ߵ�״̬
         * ���Ӻ���ָ��ʹ�õ�����ͨ��Java reference
         * ���ԶԲ��ɴ�ĺ���ָ�������������
         */
        ExtendedState<S, O, D> backPointer; //��Ա2 һ������ָ��,Ƕ������(reference)����û,ָ����һʱ�̵���ϸ״̬
        //����Java��û��ָ��,������������һ��Ƕ������,�ﵽ����ͬ��Ч��

        O observation; //��Ա3,�۲����
        D transitionDescriptor; //��Ա4,״̬ת��Desc

        // ���췽��
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
     * ���������̬��,������ ǰ���㷨��ÿ�����ƽ���Ľṹ
     * ���а�������Map���͵ĳ�Ա����:����Ϣ?,�µ���ϸ״̬
     * @param <S> ״̬����
     * @param <O> �۲����
     * @param <D> ״̬ת��Desc
     */
    private static class ForwardStepResult<S, O, D> //�洢ǰ������Ľ��--StateSequence ��������link,
    {
        // ������е�������Ա������������?
        // final����,���ɱ�
        final Map<S, Double> newMessage; // ��Ա1��һ����<state,��Ӧ�ĸ���>��ֵ����ɵ�link

        /**
         * Includes back pointers to previous state candidates for retrieving the most likely
         * sequence after the forward pass.
         * ���������Ա�ڲ�������ָ����һʱ��״̬�ĺ���ָ��,��ǰ�����������,���ڻ��ݵó���������������
         */
        final Map<S, ExtendedState<S, O, D>> newExtendedStates;
        // ��Ա2,һ��link,���е�Ԫ����һ����<state,��Ӧ��ExtendedState>��ֵ��

        // ���췽��
        ForwardStepResult(int numberStates)
        {                   // numberStates,״̬�ĸ���,Ҳ����ʱ�̵ĸ���?

            // ����Ϣ,һ����<state,��Ӧ�ĸ���>��ֵ����ɵ�link
            newMessage = new LinkedHashMap<S, Double>(Utils.initialHashMapCapacity(numberStates));
            // ��Ա2,һ��link,���е�Ԫ����һ����<state,��Ӧ��ExtendedState>��ֵ��
            newExtendedStates = new LinkedHashMap<S, ExtendedState<S, O, D>>(Utils.initialHashMapCapacity(numberStates));
        }
    }

    /**
     * Ҳ��һ����ֵ��link,Ԫ����һ����<state,Extended>
     * �����������Ʒ����:nextʱ�̵�stateֻ��lastʱ�̵�state���,�����lastExtendedState���ڵ�����
     * Allows to retrieve the most likely sequence using back pointers.
     * ����ʹ��������ĺ���ָ����Ϊ���,�������ݳ���״̬����
     * ����һ�� <״̬��,��ϸ״̬>����
     */
    private Map<S, ExtendedState<S, O, D>> lastExtendedStates; // ���һ����ϸ״̬,һ�������ĳ�Ա����

    /**
     * һ��˽�г�Ա,���ڴ洢��ǰ����״̬����?
     */
    private Collection<S> prevCandidates; // һ������,��ǰ�ĺ�ѡ�� һ�������ĳ�Ա����

    /**
     * For each state s_t of the current time step t, message.get(s_t) contains the log
     * probability of the most likely sequence ending in state s_t with given observations
     * o_1, ..., o_t.
     * ���ڵ�ǰʱ��t�µ�ÿһ��״̬����(s_t),�����۲����o_1,o_2,...,o_t
     * message.get(s_t)�����������ʵ���״̬������״̬s_t�����ĸ��ʵĶ���ֵ
     *
     * Formally, this is max log p(s_1, ..., s_t, o_1, ..., o_t) w.r.t. s_1, ..., s_{t-1}.
     * Note that to compute the most likely state sequence, it is sufficient and more
     * efficient to compute in each time step the joint probability of states and observations
     * instead of computing the conditional probability of states given the observations.
     * ��ʽ�Ͽ�,����p(s_1, ..., s_t, o_1, ..., o_t)����(s_1, ..., s_{t-1})��������
     * ע��,�ڼ�����״̬����ʱ,����:ÿ��ʱ��״̬�������۲�����Խӵĸ��� �� ����:�����۲������״̬��������������
     * ���ӳ�ֶ���Ч
     */

    /**
     * ���ڴ洢�����������е�һ����state�����Ӧ��log����
     * һ��link,���е�Ԫ����һ����<state,��Ӧ�ĸ��ʵĶ���ֵ>��ֵ��
     */
    private Map<S, Double> message; // ����get(s_t)�����᷵����״̬������״̬s_t�����ĸ��ʵĶ���ֵ?

    public Map<S, Double> getMessage()
    {
        return message;
    }

    /**
     * һ��˽�г�Ա,���ڱ�־HMM���Ƿ����
     */
    private boolean isBroken = false; //HMM����û?

    /**
     * һ��˽�г�Ա,���ڼ�¼��ʷ��Ϣ
     */
    private List<Map<S, Double>> messageHistory; // ��ʷ��Ϣ��ֻ�ڵ���ʱ��.For debugging only.

    /**
     * ���췽�� 1
     * Need to construct a new instance for each sequence of observations.
     * Does not keep the message history.
     * ��ҪΪÿһ���۲�������й�����ʵ��
     * ��������ʷ��Ϣ
     */
    public ViterbiAlgorithm() // constructor 1
    {
        this(false); //��false���������÷�����keepMessageHistory����
    }

    /**
     * ���췽�� 2
     * Need to construct a new instance for each sequence of observations.
     * @param keepMessageHistory Whether to store intermediate forward messages
     * (probabilities of intermediate most likely paths) for debugging.
     * ��ҪΪÿһ���۲�������й�����ʵ��
     * @param keepMessageHistory �������������Ϊ�˵���,�Ƿ�洢ǰ����������е��м���Ϣ(��״̬���е��м����ֵ)?
     */
    public ViterbiAlgorithm(boolean keepMessageHistory)
    {
        if (keepMessageHistory) // ��Ϊtrue
        {
            messageHistory = new ArrayList<Map<S, Double>>(); // �򴴽�messageHistory
        }
    }

    /**
     * ������ 1
     *
     * Lets the HMM computation start with the given initial state probabilities.
     * �Ӹ����ĳ�ʼ״̬����,��ʼHMM�ļ���
     *
     * ����˵��:
     * @param initialStates Pass a collection with predictable iteration order such as
     * {@link ArrayList} to ensure deterministic results.
     *                      �����������һ����������,������Ԥ��ĵ���˳��
     *                      ����{@link ArrayList} ,��ȷ�Ͼ����Խ��
     *
     * @param initialLogProbabilities Initial log probabilities for each initial state.
     *                                ÿһ����ʼ״̬�ĳ�ʼ��������
     *
     * @throws NullPointerException if any initial probability is missing
     *                              Ӧ��:��ʼ����ȱʧ
     *
     * @throws IllegalStateException if this method or
     * {@link #startWithInitialObservation(Object, Collection, Map)}
     * has already been called
     *                               Ӧ��:���������� or {@link #startWithInitialObservation(Object, Collection, Map)}
     *                               �Ѿ������ù���
     */
    public void startWithInitialStateProbabilities(Collection<S> initialStates,
                                                   Map<S, Double> initialLogProbabilities)
    {
        initializeStateProbabilities(null, initialStates, initialLogProbabilities); // �����
    }

    /**
     * ������ 2
     *
     * Lets the HMM computation start at the given first observation and uses the given emission
     * probabilities as the initial state probability for each starting state s.
     * �Ӹ����ĵ�һ���۲������ʼHMM�ļ���,�ø����ķ��������Ϊÿ����ʼ״̬s�ĳ�ʼ״̬����
     *
     * @param candidates Pass a collection with predictable iteration order such as
     * {@link ArrayList} to ensure deterministic results.
     *                   �����������һ����������,������Ԥ��ĵ���˳��
     *                   ����{@link ArrayList} ,��ȷ�Ͼ����Խ��
     * @param emissionLogProbabilities Emission log probabilities of the first observation for
     * each of the road position candidates.
     *                                 ÿ��·�κ�ѡ��ĵ�һ���۲�����ķ����������
     *
     * @throws NullPointerException if any emission probability is missing
     *                              Ӧ��:�������ȱʧ
     *
     * @throws IllegalStateException if this method or
     * {@link #startWithInitialStateProbabilities(Collection, Map)}} has already been called
     *                               Ӧ��:���������� or {@link #startWithInitialObservation(Object, Collection, Map)}
     *                               �Ѿ������ù���
     */
    public void startWithInitialObservation(O observation, Collection<S> candidates,
                                            Map<S, Double> emissionLogProbabilities)
    {
        initializeStateProbabilities(observation, candidates, emissionLogProbabilities);  // �����
    }
    /**
     * @param observation Use only if HMM only starts with first observation.
     *                    �������.��һ��ʱ�̵Ĺ۲����,Ҳ���ǵ�һ��GPS��
     * @param candidates ��һ��ʱ�̵�ȫ��candidate(��ѡƥ���)��ɵļ���
     * @param initialLogProbabilities һ��link��Ԫ����һ����<candidate,��Ӧ��log����>��ֵ��
     * �����������,���ڳ�ʼ��״̬����
     */
    private void initializeStateProbabilities(O observation, Collection<S> candidates,
                                              Map<S, Double> initialLogProbabilities)
    {
        if (message != null) // �ȼ��message�ǲ��ǿյ�
        {
            throw new IllegalStateException("Initial probabilities have already been set.");
        }

        // Set initial log probability for each start state candidate based on first observation.
        // Ϊÿһ�����ڳ�ʼ�۲�����ĺ�ѡ״̬��������ʵĶ���ֵ
        // Do not assign initialLogProbabilities directly to message to not rely on its iteration
        // ��Ҫֱ�Ӷ�initialLogProbabilities��ֵ
        // order.

        final Map<S, Double> initialMessage = new LinkedHashMap<S, Double>();    // ��ʼ��message,����һ��<��ѡ״̬��,��Ӧ�Ķ�������ֵ>����

        for (S candidate : candidates) // ������ѡ״̬��
        {
            final Double logProbability = initialLogProbabilities.get(candidate); // �����Щcandidate��log����

            if (logProbability == null) //���log����ֵΪ��
            {
                throw new NullPointerException("No initial probability for " + candidate); // �׳��쳣,�����ѡ״̬��û�г�ʼ������
            }

            // ����,�������ѡ״̬�㼰��������ʼ���initialMessage
            initialMessage.put(candidate, logProbability);
        }

        if (initialMessage.size()<1)
        {
            System.out.println("initial message is null!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }

        System.out.println("location:ViterbiAlgorithm.initializeStateProbabilities,the length of initialMessage is "+initialMessage.size());

        /*for (double logProbability : message.values())  // ����message�еĸ���ֵ
        {
            // ������ڷ�0����ֵ,��ôHMM����û��
            if (logProbability != Double.NEGATIVE_INFINITY) // �������ֵȡ����֮�󲻵��ڸ�����(���ʲ�Ϊ0)
            {
                return false;                               // HMM����û��
            }
        }*/
        message = initialMessage;



        // ���ݳ�ʼ��Ϣ�����ݣ��ж�HMM��������û
        isBroken = hmmBreak(initialMessage);

        // �������,˵�ݰ�
        if (isBroken)
        {
            System.out.println("hmm is broken when the initializeStateProbabilities");

            return;
        }

        // �����ʷ��Ϣ��Ϊ��,�Ͱ�����Ϣ�ӽ�ȥ ������ʲô����
        if (messageHistory != null)
        {
            messageHistory.add(message);
        }

        // ��ʼ��lastExtendedStates�����Ա
        lastExtendedStates = new LinkedHashMap<S, ExtendedState<S, O, D>>();

        // ������ǰʱ��(Ҳ���ǵ�һ��ʱ��)�����к�ѡ״̬��
        for (S candidate : candidates)
        {
            // �����к�ѡ״̬�㼰����ϸ��Ϣ����lastExtendStates
            lastExtendedStates.put(candidate,
                    new ExtendedState<S, O, D>(candidate, null, observation, null));
            // �����ǵ�һ��ʱ��,����backPointer��transitionDescriptor����null
        }

        // �ѵ�һ��ʱ�̵�ȫ���ѡ���ʼ����ֵ��prevCandidates
        prevCandidates = new ArrayList<S>(candidates); // Defensive copy.�Է���һ

        // �������,˵�ݰ�
        if (isBroken)
        {
            System.out.println("hmm is broken when the initializeStateProbabilities");

            return;
        }
    }


    /**
     * nextStep����һ�����
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
     * ���㲢��¼��һ��ʱ��(��ǰʱ��)����״̬.���HMM���Ͽ����ò�����
     *
     * @param candidates Pass a collection with predictable iteration order such as
     * {@link ArrayList} to ensure deterministic results.
     *                   candidates,���ǵ�ǰʱ�̵ĺ�ѡstate�㼯��.�����������һ����������
     *
     * @param emissionLogProbabilities Emission log probabilities for each candidate state.
     *                                 ÿ����ѡ״̬���Ӧ�ķ���log����
     *
     * @param transitionLogProbabilities Transition log probability between all pairs of candidates.
     * A transition probability of zero is assumed for every missing transition.
     *                                   �������,�Ǻ�ѡ·��֮������ת����϶�Ӧ�ĸ���(Transition log probability)
     *                                   ȱʧ��ת����ϵĸ�����0���
     *
     * @param transitionDescriptors Optional objects that describes the transitions.
     *                              �������,��������״̬ת�Ƶ�ʵ�廯����
     *
     * @throws NullPointerException if any emission probability is missing
     *                              Ӧ��:�������ȱʧ
     *
     * @throws IllegalStateException if neither
     * {@link #startWithInitialStateProbabilities(Collection, Map)} nor
     * {@link #startWithInitialObservation(Object, Collection, Map)}
     * has not been called before or if this method is called after an HMM break has occurred
     *                              Ӧ��:
     *                              1. {@link #startWithInitialStateProbabilities(Collection, Map)}
     *                                 {@link #startWithInitialObservation(Object, Collection, Map)}
     *                                 ��û�б����ù�
     *                              2. HMM���Ͽ�����ñ�����
     */
    public void nextStep(O observation, Collection<S> candidates,
                         Map<S, Double> emissionLogProbabilities,
                         Map<Transition<S>, Double> transitionLogProbabilities,
                         Map<Transition<S>, D> transitionDescriptors)
    {
        //System.out.println("begin:one step to next moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // ����if���������쳣
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


        // Forward step ������forwardSetp�������(�ں���ʵ����),�������һ����״̬forwardStepResult(һ��ForwardStepResult����)
        ForwardStepResult<S, O, D> forwardStepResult = forwardStep(observation, prevCandidates,
                candidates, message, emissionLogProbabilities, transitionLogProbabilities,
                transitionDescriptors);

        isBroken = hmmBreak(forwardStepResult.newMessage); // ��¼HMM����״̬(���ú���ʵ�ֵ�hmmBreak����)

        if (isBroken) return; // ���HMM������,����ʧ��,˵�ݰ�

        // ����

        // �����ʷ��Ϣ��Ϊnull
        if (messageHistory != null)
        {
            messageHistory.add(forwardStepResult.newMessage); //�Ͱ��µ���Ϣ���ȥ
        }

        // ���赱ǰʱ����k����ѡ��
        message = forwardStepResult.newMessage;    //��¼����ʱ��k��<��ѡ״̬��,�ۼ�����һʱ�̵���"��"���и���ֵ>
        lastExtendedStates = forwardStepResult.newExtendedStates; //��¼����ʱ�̵�k��<��ѡ״̬��,��Ӧ��ExtendedState>

        prevCandidates = new ArrayList<S>(candidates); // Defensive copy.������copy?

        //System.out.println("end:one step to next moment!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
    /**
     *         //��������,�����ǰʱ����k����ѡƥ��� (curState:��ǰʱ�̺�ѡ��;max prevState:curState => max pervState��ת�Ƹ������)
     *         // ��ôresult.newMessage�л����k��<��ǰʱ�̺�ѡƥ���,��Ӧ��max log����> max
     *         // result.newExtendedStates�л����k��<��ǰʱ�̺�ѡƥ���,��Ӧ��ExtendedState(���е�back pointerִ��max prevState)>
     * Computes the new forward message and the back pointers to the previous states.
     * �����µ�״̬�㼰���Ӧ��log����,��������ӵ�message��
     * ��������ָ����һʱ��״̬��ĺ���ָ��
     * @throws NullPointerException if any emission probability is missing
     *                              Ӧ��:���� �������ȱʧ
     * ����˵��:
     *  curCandidates: current candidates����ǰʱ�̵����к�ѡ״̬��
     */
    private ForwardStepResult<S, O, D> forwardStep(O observation, Collection<S> prevCandidates,
                                                   Collection<S> curCandidates, Map<S, Double> message,
                                                   Map<S, Double> emissionLogProbabilities,
                                                   Map<Transition<S>, Double> transitionLogProbabilities,
                                                   Map<Transition<S>,D> transitionDescriptors)
    {
        final ForwardStepResult<S, O, D> result = new ForwardStepResult<S, O, D>(curCandidates.size());
        // ��ʼ��һ�����ɱ����,���ڼ�¼���

        assert !prevCandidates.isEmpty();
        // ����:��prevCandidates��Ϊ��,��û����,�������ִ�У�����,�׳�AssertionError������ִֹ��

        // ������һ��˫��forѭ��,����������������ע�͵�һ���ᵽ�ĸ���,����¼������
        // ������ǰʱ�̵����к�ѡ״̬��
        for (S curState : curCandidates)
        { // start_1
            double maxLogProbability = Double.NEGATIVE_INFINITY; // ��ʼ�����log����

            S maxPrevState = null;                               // ��ʼ�� ��������ϵ���һʱ�̺�ѡ״̬��

            // ������һʱ�̵����к�ѡ״̬��
            for (S prevState : prevCandidates)
            {
                // �������и���ֵ����ȡ����֮���log����,��������ĸ������㶼�Ǽӷ�

                // logProbability=prevState�ķ���log����+previous state=>current state��Ӧ��ת��log����
                final double logProbability = message.get(prevState) + transitionLogProbability(
                        prevState, curState, transitionLogProbabilities);

                //previous state=>current state��Ӧ�ĸ��ʸ�����null,��ôlogProbabilityҲ��null,�������if�Ͳ���ִ��
                if (logProbability > maxLogProbability) //������logProbability�����ֵ
                {
                    maxLogProbability = logProbability; // ��¼maxLogProbability
                    maxPrevState = prevState;           // ��¼��Ӧ��prevState
                }
            }

            // Throws NullPointerException if curState is not stored in the map.
            result.newMessage.put(curState, maxLogProbability
                    + emissionLogProbabilities.get(curState)); //��result�е�newMessage�м�¼��һ����ǰʱ�̺�ѡ���<state,���log����>

            // Note that maxPrevState == null if there is no transition with non-zero probability.
            // ������һ����ǰʱ�̺�ѡ״̬�㣬���logProbability(״̬ת�Ƹ���)ȫ����0,��ô�Ͱ� ��������ϵ���һʱ��״̬ ��ֵΪ��
            // In this case curState has zero probability and will not be part of the most likely
            // �����������,��һ����ǰʱ�̺�ѡ״̬�㲻���ܳ�Ϊ���������е�һ����,�������ǾͲ���Ҫ��Ӧ����ϸ״̬��
            // sequence, so we don't need an ExtendedState.
            // ����,�Ǿ�����Ҫ,������������
            if (maxPrevState != null) // ������ڲ���null��prevState
            {
                final Transition<S> transition = new Transition<S>(maxPrevState, curState);
                // ����״̬ת�Ƶ�ʵ�廯�������� <���ת�Ƹ��ʵ�previous state,��ǰʱ�̵ĺ�ѡ״̬��current state>

                // Ϊ�����ǰʱ�̺�ѡ״̬�㽨��һ�� ��ϸ״̬��Ϣ��ʵ�廯��������
                final ExtendedState<S, O, D> extendedState = new ExtendedState<S, O, D>(curState,
                        lastExtendedStates.get(maxPrevState), observation,
                        transitionDescriptors.get(transition));

                // ��<��ǰʱ�̺�ѡ��state,��Ӧ��ʵ�廯��������> ����result
                result.newExtendedStates.put(curState, extendedState);
            }
        } //end_1

        //��������,�����ǰʱ����k����ѡƥ���
        // ��ôresult.newMessage�л����k��<��ǰʱ�̺�ѡƥ���,��Ӧ��max log����>
        // result.newExtendedStates�л����k��<��ǰʱ�̺�ѡƥ���,��Ӧ��ExtendedState>
        return result;
    }




    /**
     * getter of isBroken
     * Returns whether an HMM occurred in the last time step.
     * ������HMM������û
     * An HMM break means that the probability of all states equals zero.
     * ���˾���ζ������״̬���ʵ���0
     */
    public boolean isBroken()
    {
        return isBroken;
    }

    /**
     * getter of messageHistory
     *  Returns the sequence of intermediate forward messages for each time step.
     *  ǰ��������ÿ��ʱ��step���м�ֵ
     *  Returns null if message history is not kept.
     */
    public List<Map<S, Double>> messageHistory()
    {
        return messageHistory;
    }

    /**
     * another getter for messageHistory
     * ��messageHistory�е�������String��ʽ����
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
     * ���message�ǿյ� or message�еĺ�ѡ״̬���Ӧ��log����ȫΪ0
     * �ͷ���true -> HMM��������
     */
    private boolean hmmBreak(Map<S, Double> message)
    {
        for (double logProbability : message.values())  // ����message�еĸ���ֵ
        {
            // ������ڷ�0����ֵ,��ôHMM����û��
            if (logProbability != Double.NEGATIVE_INFINITY) // �������ֵȡ����֮�󲻵��ڸ�����(���ʲ�Ϊ0)
            {
                return false;                               // HMM����û��
            }
        }
        return true; //����Ͷ�����
    }



    // һ��getter����,��һ�� {<previous state=>current state,��Ӧ��log����>}��get��ָ��previous state=>current state��Ӧ�ĸ���
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
     * ��ǰ�����������,�������ڷ������������״̬����
     * Returns the most likely sequence of states for all time steps. This includes the initial
     * states / initial observation time step. If an HMM break occurred in the last time step t,
     * then the most likely sequence up to t-1 is returned. See also {@link #isBroken()}.
     * ��������ʱ�����Ͽ�����������״̬���С�
     * �����˳�ʼ�ĳ�ʼ״̬ʱ�̡���ʼ�۲�ʱ�̡�
     * ���HMM���ڵ�ǰtʱ�̶���,�Ǿͷ��ؽ�ֹt-1ʱ�̿�����������״̬����
     *
     * <p>Formally, the most likely sequence is argmax p([s_0,] s_1, ..., s_T | o_1, ..., o_T)
     * with respect to s_1, ..., s_T, where s_t is a state candidate at time step t,
     * o_t is the observation at time step t and T is the number of time steps.
     * ��ʽ�Ͻ�,������������״̬������p([s_0,] s_1, ..., s_T | o_1, ..., o_T)����(s_1,s_T)�������Ȼ����
     * �����s_t��tʱ�̵�״̬����,o_t��tʱ�̵Ĺ۲����,T��t��ʾʱ��(����)
     */
    public List<SequenceState<S, O, D>> computeMostLikelySequence()
    {
        if (message == null) // Ӧ���˻����쳣
        {
            // Return empty most likely sequence if there are no time steps or if initial
            // observations caused an HMM break.
            // û��ʱ�� or ��ʼ�۲��������HMM������
            return new ArrayList<SequenceState<S, O, D>>(); //���ؿ�
        } else { //����

            return retrieveMostLikelySequence(); //�����������,��������������state sequence
        }
    }

    /**
     * Retrieves the first state of the current forward message with maximum probability.
     *
     */
    private S mostLikelyState()
    {
        // ������Կ��Գ���
        // �������ζ��:HMM������,messageΪ��,���򱨴��˳�
        // Otherwise an HMM break would have occurred and message would be null.
        assert !message.isEmpty();

        // ��ʱ,message���������ʱ��(Ҳ����T)��k��<��ѡ״̬��,�ۼ�����һʱ�̵���"��"���и���ֵ>


        S result = null; //��ʼ��result,���ڼ�¼һ��״̬��

        double maxLogProbability = Double.NEGATIVE_INFINITY; // ��ʼ��,����������ڼ�¼�����ʵĶ���ֵ

        // Map.Entry<S, Double>��һ��<S, Double>���ͼ�ֵ�Ե���ȡ��
        for (Map.Entry<S, Double> entry : message.entrySet())  // ����message�е�enteySet�����һ����<state,����>��ֵ��
        {
            if (entry.getValue() > maxLogProbability) // ��� ���<state,����>�ĸ��������ֵ��
            {
                result = entry.getKey();              // ��¼���<state,����>�е�state
                maxLogProbability = entry.getValue(); // ��¼���<state,����>�еĸ���ֵ
            }
        }

        // ���result��Ϊ��
        assert result != null; // Otherwise an HMM break would have occurred.

        return result; //����,result�м�¼������Tʱ��message��ӵ�������ʵ�state
    }

    /**
     * Retrieves most likely sequence from the internal back pointer sequence.
     * ���ڲ��ĺ���ָ������,�������������������״̬����
     * ����һ��״̬����
     */
    private List<SequenceState<S, O, D>> retrieveMostLikelySequence()
    {
        // ������Կ��Գ���
        // �������ζ��:HMM������,messageΪ��,���򱨴��˳�
        // Otherwise an HMM break would have occurred and message would be null.
        assert !message.isEmpty();

        final S lastState = mostLikelyState(); //�����������µ��Ǹ�ƥ��״̬��

        // Retrieve most likely state sequence in reverse order
        // ������������״̬����

        final List<SequenceState<S, O, D>> result = new ArrayList<SequenceState<S, O, D>>(); // ��ʼ��result

        ExtendedState<S, O, D> es = lastExtendedStates.get(lastState); // es��lastState��ʵ�廯��������

        while(es != null)
        {
            // ״̬�����еĸ���״̬���������ΪSequenceState����
            final SequenceState<S, O, D> ss = new SequenceState<S, O, D>(es.state, es.observation,
                    es.transitionDescriptor);

            result.add(ss); //��SquenceState����result

            es = es.backPointer; // ���ݵ���һ״̬
        }
        // ���whileѭ��������,result������һ��ʱ�����ϵ���������
        // ���潫����Ϊ˳��
        Collections.reverse(result);

        return result;
    }

}

