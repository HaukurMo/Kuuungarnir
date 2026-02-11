#!/usr/bin/env bash
# java -jar ../server.jar -l ../levels/MAPF00.lvl -c "java searchclient.SearchClient -bfs" -o "benchmark_MAPF00_bfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF01.lvl -c "java searchclient.SearchClient -bfs" -o "benchmark_MAPF01_bfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF02.lvl -c "java searchclient.SearchClient -bfs" -o "benchmark_MAPF02_bfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF02C.lvl -c "java searchclient.SearchClient -bfs" -o "benchmark_MAPF02C_bfs.log" -t 180
java -jar ../server.jar -l ../levels/MAPF03.lvl -c "java searchclient.SearchClient -bfs" #-o "benchmark_MAPF03_bfs.log"
java -jar ../server.jar -l ../levels/MAPF03C.lvl -c "java searchclient.SearchClient -bfs" #-o "benchmark_MAPF03C_bfs.log"

# java -jar ../server.jar -l ../levels/MAPF00.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_MAPF00_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF01.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_MAPF01_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF02.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_MAPF02_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF02C.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_MAPF02C_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF03.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_MAPF03_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPF03C.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_MAPF03C_dfs.log" -t 180

# java -jar ../server.jar -l ../levels/MAPFslidingpuzzle.lvl -c "java searchclient.SearchClient -bfs" -o "benchmark_slidingpuzzle_bfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPFslidingpuzzle.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_slidingpuzzle_dfs.log" -t 180
java -jar ../server.jar -l ../levels/MAPFreorder2.lvl -c "java searchclient.SearchClient -bfs" #-o "benchmark_reorder2_bfs.log"
# java -jar ../server.jar -l ../levels/MAPFreorder2.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_reorder_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/BFSfriendly.lvl -c "java searchclient.SearchClient -bfs" -o "benchmark_BFSfrinedly_bfs.log" -t 180
# java -jar ../server.jar -l ../levels/BFSfriendly.lvl -c "java searchclient.SearchClient -dfs" -o "benchmark_BFSfriendly_dfs.log" -t 180