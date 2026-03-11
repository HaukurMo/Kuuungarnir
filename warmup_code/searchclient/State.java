package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State
{
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public int[] agentRows;
    public int[] agentCols;
    public static Color[] agentColors;

    /*
        The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
               Col 0  Col 1  Col 2  Col 3
        Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
        Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
        Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
        ...

        For example, this.walls[2] is an array of booleans for the third row.
        this.walls[row][col] is true if there's a wall at (row, col).

        this.boxes and this.char are two-dimensional arrays of chars.
        this.boxes[1][2]='A' means there is an A box at (1,2).
        If there is no box at (1,2), we have this.boxes[1][2]=0 (null character).
        Simiarly for goals.

    */
    public static boolean[][] walls;
    public char[][] boxes;
    public static char[][] goals;

    /*
        The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes,
        this.boxColor[1] is the color of B boxes, etc.
    */
    public static Color[] boxColors;

    public final State parent;
    public final Action[] jointAction;
    private final int g;

    private int hash = 0;


    // Constructs an initial state.
    // Arguments are not copied, and therefore should not be modified after being passed in.
    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
                 char[][] boxes, Color[] boxColors, char[][] goals
    )
    {
        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.agentColors = agentColors;
        this.walls = walls;
        this.boxes = boxes;
        this.boxColors = boxColors;
        this.goals = goals;
        this.parent = null;
        this.jointAction = null;
        this.g = 0;
    }


    // Constructs the state resulting from applying jointAction in parent.
    // Precondition: Joint action must be applicable and non-conflicting in parent state.
    private State(State parent, Action[] jointAction)
    {
        // Copy parent
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.boxes = new char[parent.boxes.length][];
        for (int i = 0; i < parent.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
        }

        // Set own parameters
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.g = parent.g + 1;

        // Apply each action
        int numAgents = this.agentRows.length;
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];

            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.agentRows[agent] = agentRow + action.agentRowDelta;
                    this.agentCols[agent] = agentCol + action.agentColDelta;
                    break;

                case Push:
                {
                    // box is in front of agent (agentDelta)
                    int boxFromRow = agentRow + action.agentRowDelta;
                    int boxFromCol = agentCol + action.agentColDelta;
                    char box = this.boxes[boxFromRow][boxFromCol];

                    // box moves (boxDelta)
                    int boxToRow = boxFromRow + action.boxRowDelta;
                    int boxToCol = boxFromCol + action.boxColDelta;

                    // agent moves into old box cell
                    this.agentRows[agent] = boxFromRow;
                    this.agentCols[agent] = boxFromCol;

                    // move box
                    this.boxes[boxFromRow][boxFromCol] = 0;
                    this.boxes[boxToRow][boxToCol] = box;
                    break;
                }

                case Pull:
                {
                    // agent moves (agentDelta)
                    int agentToRow = agentRow + action.agentRowDelta;
                    int agentToCol = agentCol + action.agentColDelta;

                    // box is adjacent to agent (boxDelta)
                    int boxFromRow = agentRow - action.boxRowDelta;
                    int boxFromCol = agentCol - action.boxColDelta;
                    char box = this.boxes[boxFromRow][boxFromCol];

                    // agent moves first (conceptually)
                    this.agentRows[agent] = agentToRow;
                    this.agentCols[agent] = agentToCol;

                    // box moves into agent's old cell
                    this.boxes[boxFromRow][boxFromCol] = 0;
                    this.boxes[agentRow][agentCol] = box;
                    break;
                }
            }
        }
    }

    public int g()
    {
        return this.g;
    }

    public boolean isGoalState()
    {
        for (int row = 1; row < this.goals.length - 1; row++)
        {
            for (int col = 1; col < this.goals[row].length - 1; col++)
            {
                char goal = this.goals[row][col];

                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
                {
                    return false;
                }
                else if ('0' <= goal && goal <= '9' &&
                         !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<State> getExpandedStates()
    {
        int numAgents = this.agentRows.length;

        // Determine list of applicable actions for each individual agent.
        Action[][] applicableActions = new Action[numAgents][];
        for (int agent = 0; agent < numAgents; ++agent)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values())
            {
                if (this.isApplicable(agent, action))
                {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }

        // Iterate over joint actions, check conflict and generate child states.
        Action[] jointAction = new Action[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);
        while (true)
        {
            for (int agent = 0; agent < numAgents; ++agent)
            {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }

            if (!this.isConflicting(jointAction))
            {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent)
            {
                if (actionsPermutation[agent] < applicableActions[agent].length - 1)
                {
                    ++actionsPermutation[agent];
                    break;
                }
                else
                {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1)
                    {
                        done = true;
                    }
                }
            }

            // Last permutation?
            if (done)
            {
                break;
            }
        }

        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    private boolean isApplicable(int agent, Action action)
    {
        int agentRow = this.agentRows[agent];
        int agentCol = this.agentCols[agent];
        Color agentColor = State.agentColors[agent];

        switch (action.type)
        {
            case NoOp:
                return true;

            case Move:
            {
                int destRow = agentRow + action.agentRowDelta;
                int destCol = agentCol + action.agentColDelta;
                return this.cellIsFree(destRow, destCol);
            }

            case Push:
            {
                // box must be in front of agent
                int boxRow = agentRow + action.agentRowDelta;
                int boxCol = agentCol + action.agentColDelta;
                char box = this.boxes[boxRow][boxCol];

                if (!(box >= 'A' && box <= 'Z'))
                    return false;

                // color must match
                if (State.boxColors[box - 'A'] != agentColor)
                    return false;

                // box destination must be free
                int boxDestRow = boxRow + action.boxRowDelta;
                int boxDestCol = boxCol + action.boxColDelta;
                return this.cellIsFree(boxDestRow, boxDestCol);
            }

            case Pull:
            {
                // agent destination must be free
                int agentDestRow = agentRow + action.agentRowDelta;
                int agentDestCol = agentCol + action.agentColDelta;
                if (!this.cellIsFree(agentDestRow, agentDestCol))
                    return false;

                // box must be adjacent (in boxDelta direction)
                int boxRow = agentRow - action.boxRowDelta;
                int boxCol = agentCol - action.boxColDelta;
                char box = this.boxes[boxRow][boxCol];

                if (!(box >= 'A' && box <= 'Z'))
                    return false;

                // color must match
                if (State.boxColors[box - 'A'] != agentColor)
                    return false;

                // box moves into agent's current cell (which becomes free)
                return true;
            }
        }

        return false; // unreachable
    }

    private boolean isConflicting(Action[] jointAction)
    {
        int numAgents = this.agentRows.length;

        int[] destinationRows = new int[numAgents]; // agent destination row
        int[] destinationCols = new int[numAgents]; // agent destination col

        boolean[] movesBox = new boolean[numAgents];

        int[] boxFromRows = new int[numAgents]; // box moved FROM (row)
        int[] boxFromCols = new int[numAgents]; // box moved FROM (col)
        int[] boxToRows   = new int[numAgents]; // box moved TO   (row)
        int[] boxToCols   = new int[numAgents]; // box moved TO   (col)

        // Collect destinations and box moves (NO state mutation!)
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];

            switch (action.type)
            {
                case NoOp:
                    // not used (we skip NoOp later), but safe defaults are fine
                    destinationRows[agent] = agentRow;
                    destinationCols[agent] = agentCol;
                    movesBox[agent] = false;
                    break;

                case Move:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    movesBox[agent] = false;
                    break;

                case Push:
                {
                    // box is in front of agent
                    int boxRow = agentRow + action.agentRowDelta;
                    int boxCol = agentCol + action.agentColDelta;

                    // agent moves into old box cell
                    destinationRows[agent] = boxRow;
                    destinationCols[agent] = boxCol;

                    movesBox[agent] = true;
                    boxFromRows[agent] = boxRow;
                    boxFromCols[agent] = boxCol;
                    boxToRows[agent] = boxRow + action.boxRowDelta;
                    boxToCols[agent] = boxCol + action.boxColDelta;
                    break;
                }

                case Pull:
                {
                    // agent moves
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;

                    movesBox[agent] = true;

                    // box ends in agent's old cell
                    boxToRows[agent] = agentRow;
                    boxToCols[agent] = agentCol;

                    // IMPORTANT: box starts BEHIND agent relative to box movement
                    boxFromRows[agent] = agentRow - action.boxRowDelta;
                    boxFromCols[agent] = agentCol - action.boxColDelta;
                    break;
                }
            }
        }

        // Pairwise conflict checks
        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            if (jointAction[a1].type == ActionType.NoOp) continue;

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                if (jointAction[a2].type == ActionType.NoOp) continue;

                // 1) two agents to same cell?
                if (destinationRows[a1] == destinationRows[a2] &&
                    destinationCols[a1] == destinationCols[a2])
                {
                    return true;
                }

                // 2) two boxes to same cell?
                if (movesBox[a1] && movesBox[a2] &&
                    boxToRows[a1] == boxToRows[a2] &&
                    boxToCols[a1] == boxToCols[a2])
                {
                    return true;
                }

                // 3) same box moved by two agents?
                if (movesBox[a1] && movesBox[a2] &&
                    boxFromRows[a1] == boxFromRows[a2] &&
                    boxFromCols[a1] == boxFromCols[a2])
                {
                    return true;
                }

                // 4) agent ends where a box ends?
                if (movesBox[a1] &&
                    destinationRows[a2] == boxToRows[a1] &&
                    destinationCols[a2] == boxToCols[a1])
                {
                    return true;
                }

                if (movesBox[a2] &&
                    destinationRows[a1] == boxToRows[a2] &&
                    destinationCols[a1] == boxToCols[a2])
                {
                    return true;
                }
            }
        }

        return false;
    }
    private boolean cellIsFree(int row, int col)
    {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    private char agentAt(int row, int col)
    {
        for (int i = 0; i < this.agentRows.length; i++)
        {
            if (this.agentRows[i] == row && this.agentCols[i] == col)
            {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Action[][] extractPlan()
    {
        Action[][] plan = new Action[this.g][];
        State state = this;
        while (state.jointAction != null)
        {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row)
            {
                for (int col = 0; col < this.boxes[row].length; ++col)
                {
                    char c = this.boxes[row][col];
                    if (c != 0)
                    {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) &&
               Arrays.equals(this.agentCols, other.agentCols) &&
               Arrays.equals(this.agentColors, other.agentColors) &&
               Arrays.deepEquals(this.walls, other.walls) &&
               Arrays.deepEquals(this.boxes, other.boxes) &&
               Arrays.equals(this.boxColors, other.boxColors) &&
               Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++)
        {
            for (int col = 0; col < this.walls[row].length; col++)
            {
                if (this.boxes[row][col] > 0)
                {
                    s.append(this.boxes[row][col]);
                }
                else if (this.walls[row][col])
                {
                    s.append("+");
                }
                else if (this.agentAt(row, col) != 0)
                {
                    s.append(this.agentAt(row, col));
                }
                else
                {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
