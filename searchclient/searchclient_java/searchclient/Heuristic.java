package searchclient;

import java.util.Comparator;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class Heuristic implements Comparator<State> {
    private final int[] goalRows;
    private final int[] goalCols;
    private final char[] goalChars;

    public Heuristic(State initialState) {
        ArrayList<Integer> gr = new ArrayList<>();
        ArrayList<Integer> gc = new ArrayList<>();
        ArrayList<Character> gg = new ArrayList<>();

        for (int row = 1; row < State.goals.length - 1; row++) {
            for (int col = 1; col < State.goals[row].length - 1; col++) {
                char goal = State.goals[row][col];
                if (('A' <= goal && goal <= 'Z') || ('0' <= goal && goal <= '9')) {
                    gr.add(row); gc.add(col); gg.add(goal);
                }
            }
        }

        goalRows = gr.stream().mapToInt(i -> i).toArray();
        goalCols = gc.stream().mapToInt(i -> i).toArray();
        goalChars = new char[gg.size()];
        for (int i = 0; i < gg.size(); i++) goalChars[i] = gg.get(i);
    }

    public int h(State s) {
        int unsatisfied = 0;
        for (int i = 0; i < goalChars.length; i++) {
            int row = goalRows[i], col = goalCols[i];
            char goal = goalChars[i];

            if ('A' <= goal && goal <= 'Z') {
                if (s.boxes[row][col] != goal) unsatisfied++;
            } else { // '0'..'9'
                int a = goal - '0';
                if (s.agentRows[a] != row || s.agentCols[a] != col) unsatisfied++;
            }
        }
        return unsatisfied;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}
