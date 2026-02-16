#!/usr/bin/env bash
# extract_benchmarks.sh
set -euo pipefail

IN_LOG="${1:-run.log}"
OUT_CSV="${2:-benchmarks_extracted_astar_greedy.csv}"
TIMEOUT_SEC="${TIMEOUT_SEC:-180}"

awk -v timeout="$TIMEOUT_SEC" '
BEGIN {
  print "Level,Strategy,States Generated,Time/s,Solution length"
  reset()
}

function reset() {
  level=""; strat=""; gen=""; time_s=""; sol=""; timed_out=0
}

function emit() {
  if (level=="" || strat=="") return
  g = (gen=="" ? "NA" : gen)
  t = (time_s=="" ? "NA" : time_s)
  s = (sol=="" ? "NA" : sol)
  print level "," strat "," g "," t "," s
  reset()
}

# Start of a run
/^\[server\]\[info\] Running client on level:/ {
  # flush previous run if any
  emit()

  line=$0
  sub(/^.*Running client on level:[[:space:]]*/, "", line)

  # Normalize Windows backslashes to slashes, then extract basename without .lvl
  gsub(/\\/, "/", line)
  sub(/^.*\//, "", line)
  sub(/\.lvl.*$/, "", line)

  level=line
  next
}

# Strategy line
/^Starting best-first search using A\* evaluation\./ { strat="astar"; next }
/^Starting best-first search using greedy evaluation\./   { strat="greedy"; next }

# Timeout marker
/\[client\]\[info\] Client timed out\./ { timed_out=1; next }

# Client summary line with #Generated
/^#Expanded:/ {
  line=$0
  if (match(line, /#Generated:[[:space:]]*([0-9,]+)/, m)) {
    gen=m[1]; gsub(/,/, "", gen)
  }
  next
}

# Solution length (fallback if Actions used not present for some reason)
/Found solution of length/ {
  if (match($0, /Found solution of length[[:space:]]*([0-9,]+)/, m)) {
    tmp=m[1]; gsub(/,/, "", tmp)
    if (sol=="") sol=tmp
  }
  next
}

# Prefer server’s Actions used (this matches your table’s “Solution length”)
/\[server\]\[info\] Actions used:/ {
  if (match($0, /Actions used:[[:space:]]*([0-9,]+)/, m)) {
    sol=m[1]; gsub(/,/, "", sol)
  }
  next
}

# End of run: server time to solve => emit a row
/\[server\]\[info\] Time to solve:/ {
  if (match($0, /Time to solve:[[:space:]]*([0-9.]+)/, m)) {
    time_s=m[1]
  }
  # If timed out and server reports 0.000, use timeout seconds
  if (timed_out==1 && (time_s=="" || time_s=="0.000" || time_s=="0.0" || time_s=="0")) {
    time_s=timeout
  }
  emit()
  next
}

END {
  # flush if file ended unexpectedly mid-run
  emit()
}
' "$IN_LOG" > "$OUT_CSV"

echo "Wrote: $OUT_CSV"