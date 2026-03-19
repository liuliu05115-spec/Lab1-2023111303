package lab1;

import java.io.*;
import java.util.*;

public class GraphProcessor {
    public static Graph createGraphFromFile(String filePath) throws IOException {
        Graph graph = new Graph();
        
        // 读取文件内容
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append(" ");
            }
        }
        
        String input = contentBuilder.toString();
        
        // 替换所有非字母（A-Z, a-z）的字符为空格，并将整段文本转为小写
        String cleanText = input.replaceAll("[^a-zA-Z]", " ").toLowerCase();
        
        // 按照一个或多个空格进行分割
        String[] wordsArray = cleanText.split("\\s+");
        List<String> validWords = new ArrayList<>();
        for (String w : wordsArray) {
            if (!w.isEmpty()) {
                validWords.add(w);
            }
        }
        
        // 根据相邻单词构建有向图及权值
        for (int i = 0; i < validWords.size() - 1; i++) {
            String word1 = validWords.get(i);
            String word2 = validWords.get(i + 1);
            graph.addEdge(word1, word2);
        }
        
        return graph;
    }
}
