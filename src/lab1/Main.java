package lab1;

import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static Graph currentGraph = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("========= Lab1 Directed Graph App =========");
        
        if (args.length > 0) {
            String filePath = args[0];
            try {
                currentGraph = GraphProcessor.createGraphFromFile(filePath);
                System.out.println("Graph loaded from " + filePath);
            } catch (Exception e) {
                System.out.println("Failed to read file: " + e.getMessage());
            }
        }
        
        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Load new text file");
            System.out.println("2. Show directed graph");
            System.out.println("3. Query bridge words");
            System.out.println("4. Generate new text");
            System.out.println("5. Calculate shortest path");
            System.out.println("6. Random walk");
            System.out.println("7. Calculate PageRank");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Enter file path: ");
                    String path = scanner.nextLine().trim();
                    try {
                        currentGraph = GraphProcessor.createGraphFromFile(path);
                        System.out.println("Graph loaded successfully.");
                    } catch (Exception e) {
                        System.out.println("Failed to read file: " + e.getMessage());
                    }
                    break;
                case "2":
                    if (currentGraph != null) {
                        showDirectedGraph(currentGraph);
                        System.out.print("Do you want to save this graph as an image? (y/n): ");
                        String saveImg = scanner.nextLine().trim();
                        if (saveImg.equalsIgnoreCase("y")) {
                            saveGraphAsImage(currentGraph, "graph_output.png");
                        }
                    } else {
                        System.out.println("Please load a file first.");
                    }
                    break;
                case "3":
                    System.out.print("Enter word1: ");
                    String w1 = scanner.nextLine().trim();
                    System.out.print("Enter word2: ");
                    String w2 = scanner.nextLine().trim();
                    System.out.println(queryBridgeWords(w1, w2));
                    break;
                case "4":
                    System.out.print("Enter new text: ");
                    String newText = scanner.nextLine().trim();
                    System.out.println("Generated: " + generateNewText(newText));
                    break;
                case "5":
                    System.out.print("Enter word1: ");
                    String startW = scanner.nextLine().trim();
                    System.out.print("Enter word2 (leave empty for all paths): ");
                    String endW = scanner.nextLine().trim();
                    System.out.println(calcShortestPath(startW, endW));
                    break;
                case "6":
                    System.out.println("Path: " + randomWalk());
                    break;
                case "7":
                    System.out.print("Enter word for PageRank: ");
                    String prWord = scanner.nextLine().trim();
                    System.out.println("PR: " + calPageRank(prWord));
                    break;
                case "0":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    public static void showDirectedGraph(Graph G) {
        System.out.println("--- Directed Graph ---");
        for (String src : G.getWords()) {
            Map<String, Integer> edges = G.getAdjacencyList().get(src);
            if (edges != null && !edges.isEmpty()) {
                System.out.print(src + " -> ");
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                    sb.append(entry.getKey()).append("(weight:").append(entry.getValue()).append("), ");
                }
                String out = sb.toString();
                System.out.println(out.substring(0, out.length() - 2));
            } else {
                System.out.println(src + " -> (no outgoing edges)");
            }
        }
        System.out.println("----------------------");
    }
    
    public static void saveGraphAsImage(Graph G, String filename) {
        System.out.println("Generating DOT file and requesting image from Graphviz API...");
        StringBuilder dot = new StringBuilder("digraph G { ");
        for (String src : G.getWords()) {
            Map<String, Integer> edges = G.getAdjacencyList().get(src);
            if (edges != null && !edges.isEmpty()) {
                for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                    dot.append("    \"").append(src).append("\" -> \"").append(entry.getKey())
                       .append("\" [label=\"").append(entry.getValue()).append("\"]; ");
                }
            } else {
                dot.append("    \"").append(src).append("\"; ");
            }
        }
        dot.append("} ");

        System.out.println("=== DOT STRING ===");
        System.out.println(dot.toString());
        System.out.println("==================");

        try {
            String encodedDot = java.net.URLEncoder.encode(dot.toString(), "UTF-8");
            java.net.URL url = new java.net.URL("https://quickchart.io/graphviz?format=png&graph=" + encodedDot);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode >= 400) {
                java.io.InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    try (java.util.Scanner s = new java.util.Scanner(errorStream).useDelimiter("\\\\A")) {
                        System.out.println("API Error Response: " + (s.hasNext() ? s.next() : ""));
                    }
                }
                throw new Exception("HTTP response code: " + responseCode);
            }

            try (java.io.InputStream in = conn.getInputStream();
                 java.io.FileOutputStream out = new java.io.FileOutputStream(filename)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                System.out.println("Graph saved successfully as " + filename + " in working directory.");
            }
        } catch (Exception e) {
            System.out.println("Failed to generate graph image: " + e.getMessage());
        }
    }
    
    public static String queryBridgeWords(String word1, String word2) {
        if (currentGraph == null) return "Graph not initialized";
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        Set<String> words = currentGraph.getWords();
        if (!words.contains(word1) && !words.contains(word2)) {
            return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        } else if (!words.contains(word1)) {
            return "No \"" + word1 + "\" in the graph!";
        } else if (!words.contains(word2)) {
            return "No \"" + word2 + "\" in the graph!";
        }
        
        List<String> bridges = new ArrayList<>();
        Map<String, Map<String, Integer>> adj = currentGraph.getAdjacencyList();
        if (adj.containsKey(word1)) {
            for (String w3 : adj.get(word1).keySet()) {
                if (adj.containsKey(w3) && adj.get(w3).containsKey(word2)) {
                    bridges.add(w3);
                }
            }
        }
        if (bridges.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else if (bridges.size() == 1) {
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" is: \"" + bridges.get(0) + "\".";
        } else {
            StringBuilder sb = new StringBuilder("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
            for (int i = 0; i < bridges.size(); i++) {
                sb.append("\"").append(bridges.get(i)).append("\"");
                if (i < bridges.size() - 2) {
                    sb.append(", ");
                } else if (i == bridges.size() - 2) {
                    sb.append(", and ");
                }
            }
            sb.append(".");
            return sb.toString();
        }
    }
    
    public static String generateNewText(String inputText) {
        if (currentGraph == null) return "Graph not initialized";
        String[] words = inputText.split("\\s+");
        if (words.length == 0) return "";
        
        Random rand = new Random();
        StringBuilder result = new StringBuilder(words[0]);
        Map<String, Map<String, Integer>> adj = currentGraph.getAdjacencyList();
        
        for (int i = 0; i < words.length - 1; i++) {
            String w1_orig = words[i];
            String w2_orig = words[i+1];
            String w1 = w1_orig.replaceAll("[^a-zA-Z]", "").toLowerCase();
            String w2 = w2_orig.replaceAll("[^a-zA-Z]", "").toLowerCase();
            
            List<String> bridges = new ArrayList<>();
            if (!w1.isEmpty() && !w2.isEmpty() && currentGraph.getWords().contains(w1) && currentGraph.getWords().contains(w2) && adj.containsKey(w1)) {
                for (String w3 : adj.get(w1).keySet()) {
                    if (adj.containsKey(w3) && adj.get(w3).containsKey(w2)) {
                        bridges.add(w3);
                    }
                }
            }
            if (!bridges.isEmpty()) {
                String bridge = bridges.get(rand.nextInt(bridges.size()));
                result.append(" ").append(bridge);
            }
            result.append(" ").append(w2_orig);
        }
        return result.toString();
    }
    
    public static String calcShortestPath(String word1, String word2) {
        if (currentGraph == null) return "Graph not initialized";
        word1 = word1.toLowerCase();
        
        if (!currentGraph.getWords().contains(word1)) {
            return "No \"" + word1 + "\" in the graph!";
        }
        boolean multiTarget = word2.isEmpty();
        if (!multiTarget) {
            word2 = word2.toLowerCase();
            if (!currentGraph.getWords().contains(word2)) {
                return "No \"" + word2 + "\" in the graph!";
            }
        }
        
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        
        for (String node : currentGraph.getWords()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(word1, 0);
        pq.add(word1);
        
        Set<String> visited = new HashSet<>();
        Map<String, Map<String, Integer>> adj = currentGraph.getAdjacencyList();
        
        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (visited.contains(u)) continue;
            visited.add(u);
            
            if (!multiTarget && u.equals(word2)) break;
            
            if (adj.containsKey(u)) {
                for (Map.Entry<String, Integer> neighbor : adj.get(u).entrySet()) {
                    String v = neighbor.getKey();
                    int weight = neighbor.getValue();
                    if (!visited.contains(v) && dist.get(u) != Integer.MAX_VALUE && dist.get(u) + weight < dist.get(v)) {
                        dist.put(v, dist.get(u) + weight);
                        prev.put(v, u);
                        // Re-add to priority queue (simpler than updateKey in Java)
                        pq.add(v);
                    }
                }
            }
        }
        
        if (!multiTarget) {
            if (dist.get(word2) == Integer.MAX_VALUE) {
                return "Unreachable from " + word1 + " to " + word2;
            }
            // Construct path
            List<String> path = new ArrayList<>();
            String curr = word2;
            while (curr != null) {
                path.add(0, curr);
                curr = prev.get(curr);
            }
            return String.join(" -> ", path) + "\n(Length: " + dist.get(word2) + ")";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String target : currentGraph.getWords()) {
                if (!target.equals(word1)) {
                    if (dist.get(target) == Integer.MAX_VALUE) {
                        sb.append(word1).append(" -> ").append(target).append(": Unreachable\n");
                    } else {
                        List<String> path = new ArrayList<>();
                        String curr = target;
                        while (curr != null) {
                            path.add(0, curr);
                            curr = prev.get(curr);
                        }
                        sb.append(String.join(" -> ", path)).append("  (Len: ").append(dist.get(target)).append(")\n");
                    }
                }
            }
            return sb.toString();
        }
    }
    
    public static Double calPageRank(String word) {
        if (currentGraph == null) return 0.0;
        word = word.toLowerCase();
        if (!currentGraph.getWords().contains(word)) return 0.0;
        
        Set<String> words = currentGraph.getWords();
        int N = words.size();
        if (N == 0) return 0.0;
        
        double d = 0.85;
        Map<String, Double> pr = new HashMap<>();
        for (String node : words) {
            pr.put(node, 1.0 / N);
        }
        
        Map<String, Map<String, Integer>> adj = currentGraph.getAdjacencyList();
        
        for (int iter = 0; iter < 100; iter++) {
            Map<String, Double> newPr = new HashMap<>();
            double danglingSum = 0.0;
            for (String node : words) {
                Map<String, Integer> edges = adj.get(node);
                if (edges == null || edges.isEmpty()) {
                    danglingSum += pr.get(node);
                }
            }
            
            for (String node : words) {
                double pr_A = (1 - d) / N + d * (danglingSum / N);
                newPr.put(node, pr_A);
            }
            
            for (String node : words) {
                Map<String, Integer> edges = adj.get(node);
                if (edges != null && !edges.isEmpty()) {
                    int totalWeight = 0;
                    for (int w : edges.values()) totalWeight += w;
                    
                    for (Map.Entry<String, Integer> e : edges.entrySet()) {
                        String target = e.getKey();
                        double transfer = d * pr.get(node) * ((double)e.getValue() / totalWeight);
                        newPr.put(target, newPr.get(target) + transfer);
                    }
                }
            }
            pr = newPr;
        }
        
        return pr.get(word);
    }
    
    public static String randomWalk() {
        if (currentGraph == null) return "Graph not initialized";
        Set<String> words = currentGraph.getWords();
        List<String> nodes = new ArrayList<>(words);
        if (nodes.isEmpty()) return "";
        
        Random rand = new Random();
        String current = nodes.get(rand.nextInt(nodes.size()));
        
        List<String> pathNodes = new ArrayList<>();
        pathNodes.add(current);
        Set<String> visitedEdges = new HashSet<>();
        
        Map<String, Map<String, Integer>> adj = currentGraph.getAdjacencyList();
        
        while (true) {
            Map<String, Integer> edges = adj.get(current);
            if (edges == null || edges.isEmpty()) {
                break;
            }
            List<String> neighbors = new ArrayList<>(edges.keySet());
            String next = neighbors.get(rand.nextInt(neighbors.size()));
            
            String edgeStr = current + "->" + next;
            pathNodes.add(next);
            
            if (visitedEdges.contains(edgeStr)) {
                break;
            }
            visitedEdges.add(edgeStr);
            current = next;
        }
        
        String result = String.join(" ", pathNodes);
        
        try (PrintWriter out = new PrintWriter("random_walk.txt")) {
            out.println(result);
            System.out.println("Random walk saved to random_walk.txt in working directory.");
        } catch (Exception e) {
            System.out.println("Failed to save random walk: " + e.getMessage());
        }
        
        return result;
    }
}
