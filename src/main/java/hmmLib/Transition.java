package hmmLib;

import java.util.Objects;

/**
 * Represents the transition between two consecutive candidates.
 * 这个类,用来表示两个连续的candidate之间的状态转移
 *
 * @param <S> the state type
 */
public class Transition<S> {

    public final S fromCandidate;
    public final S toCandidate;

    // fromCandidate >==transition==> toCandidate

    // 构造方法
    public Transition(S fromCandidate, S toCandidate) 
    {
        this.fromCandidate = fromCandidate;
        this.toCandidate = toCandidate;
    }


    @Override
    public int hashCode() {
        return Objects.hash(fromCandidate, toCandidate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        Transition<S> other = (Transition<S>) obj;
        return Objects.equals(fromCandidate, other.fromCandidate) && Objects.equals(toCandidate,
                other.toCandidate);
    }

    @Override
    public String toString() {
        return "Transition [fromCandidate=" + fromCandidate + ", toCandidate="
                + toCandidate + "]";
    }


}

