package me.jackcw.jcore.state;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest
{
    private enum TestState
    {
        WAITING,
        STARTING,
        RUNNING,
        ENDED
    }

    @Test
    void createRejectsNullInitialState()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> StateMachine.create(null)
        );
    }

    @Test
    void startsInInitialState()
    {
        StateMachine<TestState> machine = StateMachine.create(TestState.WAITING);

        assertEquals(TestState.WAITING, machine.getState());
    }

    @Test
    void allowsRegisteredTransition()
    {
        StateMachine<TestState> machine = StateMachine.create(TestState.WAITING)
                .allowTransition(TestState.WAITING, TestState.STARTING);

        machine.transition(TestState.STARTING);

        assertEquals(TestState.STARTING, machine.getState());
    }

    @Test
    void rejectsUnregisteredTransition()
    {
        StateMachine<TestState> machine = StateMachine.create(TestState.WAITING);

        assertThrows(
                IllegalStateException.class,
                () -> machine.transition(TestState.ENDED)
        );
    }

    @Test
    void canTransitionReflectsRegisteredTransitions()
    {
        StateMachine<TestState> machine = StateMachine.create(TestState.WAITING)
                .allowTransition(TestState.WAITING, TestState.STARTING);

        assertTrue(machine.canTransition(TestState.STARTING));
        assertFalse(machine.canTransition(TestState.ENDED));
    }

    @Test
    void firesExitThenEnterHooksInOrder()
    {
        List<String> events = new ArrayList<>();

        StateMachine<TestState> machine = StateMachine.create(TestState.WAITING)
                .allowTransition(TestState.WAITING, TestState.STARTING)
                .onExit(TestState.WAITING, () -> events.add("exit-waiting"))
                .onEnter(TestState.STARTING, () -> events.add("enter-starting"));

        machine.transition(TestState.STARTING);

        assertEquals(List.of("exit-waiting", "enter-starting"), events);
    }

    @Test
    void supportsMultipleTransitionsFromSameState()
    {
        StateMachine<TestState> machine = StateMachine.create(TestState.WAITING)
                .allowTransition(TestState.WAITING, TestState.STARTING)
                .allowTransition(TestState.WAITING, TestState.ENDED);

        assertTrue(machine.canTransition(TestState.STARTING));
        assertTrue(machine.canTransition(TestState.ENDED));
    }

    @Test
    void supportsMultipleHooksForSameState()
    {
        List<String> events = new ArrayList<>();

        StateMachine<TestState> machine = StateMachine.create(TestState.WAITING)
                .allowTransition(TestState.WAITING, TestState.STARTING)
                .onEnter(TestState.STARTING, () -> events.add("first"))
                .onEnter(TestState.STARTING, () -> events.add("second"));

        machine.transition(TestState.STARTING);

        assertEquals(List.of("first", "second"), events);
    }
}
