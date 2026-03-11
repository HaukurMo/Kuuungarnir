#!/usr/bin/env bash
# java -jar ../server.jar -l ../levels/SAD1.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_SAD1_greedy.log" -t 180
# java -jar ../server.jar -l ../levels/SAD2.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_SAD2_greedy.log" -t 180
# java -jar ../server.jar -l ../levels/SAD3.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_SAD3_greedy.log" #-t 180
java -jar ../server.jar -l ../levels/SAFirefly.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_SAD3C_greedy.log" #-t 180
java -jar ../server.jar -l ../levels/SACrunch.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_MAPF03_greedy.log" #-t 180
# java -jar ../server.jar -l ../levels/MAPF03C.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_MAPF03C_greedy.log" #-t 180

# java -jar ../server.jar -l ../levels/SAD1.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_SAD1_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/SAD2.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_SAD2_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/SAD3.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_SAD3_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/SAFirefly.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_SAD3C_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/SACrunch.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_MAPF03_dfs.log" #-t 180
# java -jar ../server.jar -l ../levels/MAPF03C.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_MAPF03C_dfs.log" #-t 180

# java -jar ../server.jar -l ../levels/MAPFslidingpuzzle.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_slidingpuzzle_greedy.log" -t 180
# java -jar ../server.jar -l ../levels/MAPFslidingpuzzle.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_slidingpuzzle_dfs.log" -t 180
# java -jar ../server.jar -l ../levels/MAPFreorder2.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_reorder2_greedy.log" #-t 180
# java -jar ../server.jar -l ../levels/MAPFreorder2.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_reorder_dfs.log" #-t 180
# java -jar ../server.jar -l ../levels/greedyfriendly.lvl -c "java searchclient.SearchClient -greedy" # -o "benchmark_greedyfrinedly_greedy.log" -t 180
# java -jar ../server.jar -l ../levels/greedyfriendly.lvl -c "java searchclient.SearchClient -dfs" # -o "benchmark_greedyfriendly_dfs.log" -t 180