package hmmLib;

import java.util.Objects;

/**
 * State of the most likely sequence with additional information.
 *
 * @param <S> the state type            表示类型:状态变量
 * @param <O> the observation type      表示类型:观测变量
 * @param <D> the transition descriptor type 表示类型:转移Desc
 */

/**
 *
 */
// class:状态序列
public class SequenceState<S, O, D> {

    public final S state;  //一个状态变量？

    /**
     * Null if HMM was started with initial state probabilities and state is the initial state.
     * 当HMM以初始状态概率开始，并且状态是初始状态 => observation=Null
     */
    public final O observation; //观测变量

    /**
     * Null if transition descriptor was not provided.
     * 如果没有提供transition descriptor => transitionDescriptor=Null
     */
    public final D transitionDescriptor;

    // 构造方法1
    public SequenceState(S state, O observation, D transitionDescriptor) 
    {
        this.state = state;
        this.observation = observation;
        this.transitionDescriptor = transitionDescriptor;
    }

    // 重载toString方法
    @Override
    public String toString() 
    {
        return "SequenceState [state=" + state + ", observation=" + observation
                + ", transitionDescriptor=" + transitionDescriptor
                + "]";
    }
    

    // 重载hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(state, observation, transitionDescriptor);
    }

    // 重载 equals方法, 用来比较两个状态序列 SequenceState对象 是否相等
    @Override
    public boolean equals(Object obj) {
        if (this == obj)    // 如果两个变量名指向同一地址,那肯定相等
            return true;
        if (obj == null)    // 如果其中一个对象是空的,那肯定不相等
            return false;
        if (getClass() != obj.getClass()) // 如果不是同一个class,那肯定不相等
            return false;

        SequenceState<?, ?, ?> other = (SequenceState<?, ?, ?>) obj; // 把传进来的这个对象转化成SequenceState类型
        // 依次比较各属性的值
        return Objects.equals(state, other.state) &&
                Objects.equals(observation, other.observation) &&
                Objects.equals(transitionDescriptor, other.transitionDescriptor);
    }

}
