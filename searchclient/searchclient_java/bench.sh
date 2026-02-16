#!/usr/bin/env bash
# java -jar ../server.jar -l ../levels/MAPF00.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_MAPF00_astar.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF01.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_MAPF01_astar.log" -t 180
java -jar ../server.jar -l ../levels/MAPF02.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_MAPF02_astar.log" #-t 180
java -jar ../server.jar -l ../levels/MAPF02C.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_MAPF02C_astar.log" #-t 180
java -jar ../server.jar -l ../levels/MAPF03.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_MAPF03_astar.log" #-t 180
java -jar ../server.jar -l ../levels/MAPF03C.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_MAPF03C_astar.log" #-t 180

# java -jar ../server.jar -l ../levels/MAPF00.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_MAPF00_greedy.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF01.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_MAPF01_greedy.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF02.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_MAPF02_greedy.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF02C.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_MAPF02C_greedy.log" -t 180
java -jar ../server.jar -l ../levels/MAPF03.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_MAPF03_greedy.log" #-t 180
java -jar ../server.jar -l ../levels/MAPF03C.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_MAPF03C_greedy.log" #-t 180

# java -jar ../server.jar -l ../levels/MAPFslidingpuzzle.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_slidingpuzzle_astar.log" -t 180
# java -jar ../server.jar -l ../levels/MAPFslidingpuzzle.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_slidingpuzzle_greedy.log" -t 180
java -jar ../server.jar -l ../levels/MAPFreorder2.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_reorder2_astar.log" #-t 180
java -jar ../server.jar -l ../levels/MAPFreorder2.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_reorder_greedy.log" #-t 180
# java -jar ../server.jar -l ../levels/BFSfriendly.lvl -c "java searchclient.SearchClient -astar" -o "benchmark_BFSfrinedly_astar.log" -t 180
# java -jar ../server.jar -l ../levels/BFSfriendly.lvl -c "java searchclient.SearchClient -greedy" -o "benchmark_BFSfriendly_greedy.log" -t 180