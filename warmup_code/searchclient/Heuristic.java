package searchclient;

import java.util.Comparator;
import java.util.ArrayDeque;
import java.util.Arrays;


public abstract class Heuristic implements Comparator<State> {
    public enum DomainH { AGENT_GOAL_COUNT, AGENT_MAZE_DISTANCE, BOX_GOAL_COUNT, BOX_MAZE_DISTANCE }

    // change this between runs:
    public static final DomainH DOMAIN_H = DomainH.BOX_MAZE_DISTANCE;

    // ----- BOX GOAL DATA -----
    protected final boolean[] letterHasGoal;        // [26]
    protected final int[][] distBoxToGoal;          // distBoxToGoal[L][idx] = min wall-distance from idx to any goal of letter L
    protected final char[] goalLetterAt;            // goalLetterAt[idx] = 'A'..'Z' if that cell is a box-goal, else 0
    protected final int numBoxGoals;

    protected static final int INF = 1_000_000_000;

    protected final int rows;
    protected final int cols;
    protected final int numAgents;

    // goal position for each agent (if it has one)
    protected final boolean[] hasGoal;
    protected final int[] goalRow;
    protected final int[] goalCol;

    // distToGoal[a][idx] = shortest wall-only distance from cell idx to agent a's goal (or INF)
    // idx = r*cols + c
    protected final int[][] distToGoal;

    // goalAt[idx] = agent index whose goal is at idx, else -1
    protected final int[] goalAt;

    protected final int numGoalAgents;

    // Toggle this: helps greedy a lot; makes heuristic less “A*-clean”
    protected static final boolean USE_BLOCKING_PENALTY = true;

    public Heuristic(State initialState) {

        this.rows = State.walls.length;
        this.cols = State.walls[0].length;
        this.numAgents = initialState.agentRows.length;

        this.hasGoal = new boolean[numAgents];
        this.goalRow = new int[numAgents];
        this.goalCol = new int[numAgents];
        Arrays.fill(this.goalRow, -1);
        Arrays.fill(this.goalCol, -1);

        // Scan static goal map once: find digit goals
        for (int r = 1; r < State.goals.length - 1; r++) {
            for (int c = 1; c < State.goals[r].length - 1; c++) {
                char g = State.goals[r][c];
                if ('0' <= g && g <= '9') {
                    int a = g - '0';
                    if (a >= 0 && a < numAgents) {
                        hasGoal[a] = true;
                        goalRow[a] = r;
                        goalCol[a] = c;
                    }
                }
            }
        }

        int count = 0;
        for (boolean b : hasGoal) if (b) count++;
        this.numGoalAgents = count;

        this.goalAt = new int[rows * cols];
        Arrays.fill(this.goalAt, -1);
        for (int a = 0; a < numAgents; a++) {
            if (hasGoal[a]) {
                int idx = goalRow[a] * cols + goalCol[a];
                goalAt[idx] = a;
            }
        }

        this.distToGoal = new int[numAgents][];
        for (int a = 0; a < numAgents; a++) {
            if (!hasGoal[a]) continue;
            this.distToGoal[a] = bfsDistancesFrom(goalRow[a], goalCol[a]);
        }
        
        this.letterHasGoal = new boolean[26];
        this.distBoxToGoal = new int[26][];
        this.goalLetterAt = new char[rows * cols];

        int boxGoalCount = 0;

        // Collect goal cells for each letter (as indices)
        @SuppressWarnings("unchecked")
        java.util.ArrayList<Integer>[] goalsByLetter = (java.util.ArrayList<Integer>[]) new java.util.ArrayList[26];
        for (int i = 0; i < 26; i++) goalsByLetter[i] = new java.util.ArrayList<>();

        for (int r = 1; r < State.goals.length - 1; r++) {
            for (int c = 1; c < State.goals[r].length - 1; c++) {
                char g = State.goals[r][c];
                if ('A' <= g && g <= 'Z') {
                    int L = g - 'A';
                    int idx = r * cols + c;
                    goalsByLetter[L].add(idx);
                    letterHasGoal[L] = true;
                    goalLetterAt[idx] = g;
                    boxGoalCount++;
                }
            }
        }
        this.numBoxGoals = boxGoalCount;

        // Precompute dist-to-goal per letter using multi-source BFS
        for (int L = 0; L < 26; L++) {
            if (!letterHasGoal[L]) continue;
            this.distBoxToGoal[L] = bfsDistancesFromSources(goalsByLetter[L]);
        }
    }

    // Multi-source BFS: starts from all goal cells for a given letter
    private int[] bfsDistancesFromSources(java.util.ArrayList<Integer> sources) {
        int[] dist = new int[rows * cols];
        Arrays.fill(dist, INF);

        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int s : sources) {
            dist[s] = 0;
            q.add(s);
        }

        while (!q.isEmpty()) {
            int idx = q.poll();
            int r = idx / cols;
            int c = idx % cols;
            int nd = dist[idx] + 1;

            // up
            if (r > 0 && !State.walls[r - 1][c]) {
                int nidx = (r - 1) * cols + c;
                if (dist[nidx] == INF) { dist[nidx] = nd; q.add(nidx); }
            }
            // down
            if (r + 1 < rows && !State.walls[r + 1][c]) {
                int nidx = (r + 1) * cols + c;
                if (dist[nidx] == INF) { dist[nidx] = nd; q.add(nidx); }
            }
            // left
            if (c > 0 && !State.walls[r][c - 1]) {
                int nidx = r * cols + (c - 1);
                if (dist[nidx] == INF) { dist[nidx] = nd; q.add(nidx); }
            }
            // right
            if (c + 1 < cols && !State.walls[r][c + 1]) {
                int nidx = r * cols + (c + 1);
                if (dist[nidx] == INF) { dist[nidx] = nd; q.add(nidx); }
            }
        }

        return dist;
    }

    // Wall-only BFS distances from (sr, sc) to all cells (ignores agents/boxes)
    private int[] bfsDistancesFrom(int sr, int sc) {
        int[] dist = new int[rows * cols];
        Arrays.fill(dist, INF);

        ArrayDeque<Integer> q = new ArrayDeque<>();
        int startIdx = sr * cols + sc;
        dist[startIdx] = 0;
        q.add(startIdx);

        while (!q.isEmpty()) {
            int idx = q.poll();
            int r = idx / cols;
            int c = idx % cols;
            int nd = dist[idx] + 1;

            // 4-neighbors
            // up
            if (r > 0 && !State.walls[r - 1][c]) {
                int nidx = (r - 1) * cols + c;
                if (dist[nidx] == INF) {
                    dist[nidx] = nd;
                    q.add(nidx);
                }
            }
            // down
            if (r + 1 < rows && !State.walls[r + 1][c]) {
                int nidx = (r + 1) * cols + c;
                if (dist[nidx] == INF) {
                    dist[nidx] = nd;
                    q.add(nidx);
                }
            }
            // left
            if (c > 0 && !State.walls[r][c - 1]) {
                int nidx = r * cols + (c - 1);
                if (dist[nidx] == INF) {
                    dist[nidx] = nd;
                    q.add(nidx);
                }
            }
            // right
            if (c + 1 < cols && !State.walls[r][c + 1]) {
                int nidx = r * cols + (c + 1);
                if (dist[nidx] == INF) {
                    dist[nidx] = nd;
                    q.add(nidx);
                }
            }
        }

        return dist;
    }

    public int h(State s) {
        return switch (DOMAIN_H) {
            case AGENT_GOAL_COUNT    -> hGoalCount(s);
            case AGENT_MAZE_DISTANCE -> hMazeDistance(s);
            case BOX_GOAL_COUNT      -> hBoxGoalCount(s);
            case BOX_MAZE_DISTANCE   -> hBoxesMazeBetter(s);
        };
    }

    private int hBoxGoalCount(State s) {
        int miss = 0;
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {
                char g = State.goals[r][c];
                if ('A' <= g && g <= 'Z') {
                    if (s.boxes[r][c] != g) miss++;
                }
            }
        }
        return miss;
    }

    private int hBoxesMazeBetter(State s) {
        // If you have no box goals at all, return 0
        if (numBoxGoals == 0) return 0;

        // Count movers per color (do this once in constructor and store it if you want)
        int numColors = State.boxColors.length; // or Color.values().length if Color is enum
        int[] movers = new int[numColors];
        for (int a = 0; a < numAgents; a++) {
            movers[s.agentColors[a].ordinal()]++;
        }

        long[] sumByColor = new long[numColors];
        int[] maxByColor = new int[numColors];

        int blockers = 0;

        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {
                char box = s.boxes[r][c];
                if (box == 0) continue;                 // no box
                if (!(box >= 'A' && box <= 'Z')) continue;

                // already satisfied?
                if (State.goals[r][c] == box) continue;

                // deadlock: corner (two perpendicular walls) and not on its goal
                if (isCorner(r, c)) return INF;

                int letter = box - 'A';
                int idx = r * cols + c;

                int d = distBoxToGoal[letter][idx];     // <-- your precomputed distance-to-goal for that letter
                if (d >= INF / 2) return INF;

                int colorId = State.boxColors[letter].ordinal();
                sumByColor[colorId] += d;
                if (d > maxByColor[colorId]) maxByColor[colorId] = d;

                // blocking: box on some other goal cell
                char g = State.goals[r][c];
                if (g >= 'A' && g <= 'Z' && g != box) blockers++;
            }
        }

        int h = 0;
        for (int colorId = 0; colorId < numColors; colorId++) {
            if (sumByColor[colorId] == 0) continue;

            int k = movers[colorId];
            if (k == 0) return INF; // boxes of this color exist but no agent can move them

            int workLB = (int)((sumByColor[colorId] + k - 1) / k); // ceil(sum/k)
            h = Math.max(h, Math.max(maxByColor[colorId], workLB));
        }

        // optional: small extra guidance for greedy / WA*
        h += blockers;

        return h;
    }

    private boolean isCorner(int r, int c) {
        boolean up = State.walls[r - 1][c];
        boolean down = State.walls[r + 1][c];
        boolean left = State.walls[r][c - 1];
        boolean right = State.walls[r][c + 1];
        return (up && left) || (up && right) || (down && left) || (down && right);
    }

    private int hGoalCount(State s) {
        if (numGoalAgents == 0) return 0;

        int miss = 0;
        for (int a = 0; a < numAgents; a++) {
            if (!hasGoal[a]) continue;
            if (s.agentRows[a] != goalRow[a] || s.agentCols[a] != goalCol[a]) miss++;
        }
        return miss;
    }

    private int hMazeDistance(State s) {
        if (numGoalAgents == 0) return 0;

        long sum = 0;
        int maxd = 0;

        for (int a = 0; a < numAgents; a++) {
            if (!hasGoal[a]) continue;

            int idx = s.agentRows[a] * cols + s.agentCols[a];
            int d = distToGoal[a][idx];

            if (d >= INF / 2) return INF;

            sum += d;
            if (d > maxd) maxd = d;
        }

        int hpar = (int) ((sum + numGoalAgents - 1) / numGoalAgents);
        int h = Math.max(maxd, hpar);

        if (USE_BLOCKING_PENALTY) {
            int blockers = 0;
            for (int i = 0; i < numAgents; i++) {
                int idx = s.agentRows[i] * cols + s.agentCols[i];
                int g = goalAt[idx];
                if (g != -1 && g != i) blockers++;
            }
            h += blockers;
        }

        return h;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return Integer.compare(this.f(s1), this.f(s2));
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
