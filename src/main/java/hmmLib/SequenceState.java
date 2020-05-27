package hmmLib;

import java.util.Objects;

/**
 * State of the most likely sequence with additional information.
 *
 * @param <S> the state type            ��ʾ����:״̬����
 * @param <O> the observation type      ��ʾ����:�۲����
 * @param <D> the transition descriptor type ��ʾ����:ת��Desc
 */

/**
 *
 */
// class:״̬����
public class SequenceState<S, O, D> {

    public final S state;  //һ��״̬������

    /**
     * Null if HMM was started with initial state probabilities and state is the initial state.
     * ��HMM�Գ�ʼ״̬���ʿ�ʼ������״̬�ǳ�ʼ״̬ => observation=Null
     */
    public final O observation; //�۲����

    /**
     * Null if transition descriptor was not provided.
     * ���û���ṩtransition descriptor => transitionDescriptor=Null
     */
    public final D transitionDescriptor;

    // ���췽��1
    public SequenceState(S state, O observation, D transitionDescriptor) 
    {
        this.state = state;
        this.observation = observation;
        this.transitionDescriptor = transitionDescriptor;
    }

    // ����toString����
    @Override
    public String toString() 
    {
        return "SequenceState [state=" + state + ", observation=" + observation
                + ", transitionDescriptor=" + transitionDescriptor
                + "]";
    }
    

    // ����hashCode����
    @Override
    public int hashCode() {
        return Objects.hash(state, observation, transitionDescriptor);
    }

    // ���� equals����, �����Ƚ�����״̬���� SequenceState���� �Ƿ����
    @Override
    public boolean equals(Object obj) {
        if (this == obj)    // �������������ָ��ͬһ��ַ,�ǿ϶����
            return true;
        if (obj == null)    // �������һ�������ǿյ�,�ǿ϶������
            return false;
        if (getClass() != obj.getClass()) // �������ͬһ��class,�ǿ϶������
            return false;

        SequenceState<?, ?, ?> other = (SequenceState<?, ?, ?>) obj; // �Ѵ��������������ת����SequenceState����
        // ���αȽϸ����Ե�ֵ
        return Objects.equals(state, other.state) &&
                Objects.equals(observation, other.observation) &&
                Objects.equals(transitionDescriptor, other.transitionDescriptor);
    }

}
