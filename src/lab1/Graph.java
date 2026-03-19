package lab1;

import java.util.*;

public class Graph {
    private Map<String, Map<String, Integer>> adjacencyList;
    private Set<String> words;

    public Graph() {
        this.adjacencyList = new HashMap<>();
        this.words = new HashSet<>();
    }

    public void addEdge(String src, String dest) {
        words.add(src);
        words.add(dest);
        
        adjacencyList.putIfAbsent(src, new HashMap<>());
        Map<String, Integer> edges = adjacencyList.get(src);
        edges.put(dest, edges.getOrDefault(dest, 0) + 1);
        
        // 保证终点也在邻接表中有一项，即使它没有出边
        adjacencyList.putIfAbsent(dest, new HashMap<>());
    }

    public Map<String, Map<String, Integer>> getAdjacencyList() {
        return adjacencyList;
    }

    public Set<String> getWords() {
        return words;
    }
}
// C4 modification 1
