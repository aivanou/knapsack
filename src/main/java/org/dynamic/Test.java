package org.dynamic;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Test {
    public static void main(String[] args) throws Exception {

    }

    static void leastTime(List<List<Integer>> edges, int n) {
        Queue<Integer> q = new LinkedList<>();
        Container[] time = new Container[n];
        q.add(1);
        while (!q.isEmpty()) {
            int u = q.poll();
            if (time[u] == null) {
                time[u] = new Container(1);
            }
            for (int v : edges.get(u)) {
                if (time[v] != null) {
                    time[u].n += time[v].n;
                } else {
                    time[v] = time[u];
                    time[u].n += 1;
                    q.add(v);
                }
            }
        }
    }

    static class Container {
        public Container(int n) {
            this.n = n;
        }

        int n = 0;
    }
}
