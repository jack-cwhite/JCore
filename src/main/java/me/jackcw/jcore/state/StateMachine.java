package me.jackcw.jcore.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StateMachine<S>
{
    private final Map<S, List<S>> transitions = new HashMap<>();
    private final Map<S, List<Runnable>> onEnter = new HashMap<>();
    private final Map<S, List<Runnable>> onExit = new HashMap<>();

    private S state;

    private StateMachine(S initial)
    {
        this.state = initial;
    }

    public static <S> StateMachine<S> create(S initial)
    {
        if (initial == null)
            throw new IllegalArgumentException(
                    "Initial state cannot be null"
            );

        return new StateMachine<>(initial);
    }

    public StateMachine<S> allowTransition(S from, S to)
    {
        if (from == null || to == null)
            throw new IllegalArgumentException(
                    "States cannot be null"
            );

        transitions.computeIfAbsent(from, ignored -> new ArrayList<>()).add(to);

        return this;
    }

    public StateMachine<S> onEnter(S state, Runnable callback)
    {
        if (state == null)
            throw new IllegalArgumentException(
                    "State cannot be null"
            );

        if (callback == null)
            throw new IllegalArgumentException(
                    "Callback cannot be null"
            );

        onEnter.computeIfAbsent(state, ignored -> new ArrayList<>()).add(callback);

        return this;
    }

    public StateMachine<S> onExit(S state, Runnable callback)
    {
        if (state == null)
            throw new IllegalArgumentException(
                    "State cannot be null"
            );

        if (callback == null)
            throw new IllegalArgumentException(
                    "Callback cannot be null"
            );

        onExit.computeIfAbsent(state, ignored -> new ArrayList<>()).add(callback);

        return this;
    }

    public boolean canTransition(S to)
    {
        return transitions.getOrDefault(state, List.of()).contains(to);
    }

    public void transition(S to)
    {
        if (!canTransition(to))
            throw new IllegalStateException(
                    "Cannot transition from " + state + " to " + to
            );

        S from = state;

        onExit.getOrDefault(from, List.of()).forEach(Runnable::run);

        state = to;

        onEnter.getOrDefault(to, List.of()).forEach(Runnable::run);
    }

    public S getState()
    {
        return state;
    }
}
