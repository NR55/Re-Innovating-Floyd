import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FloydAlgorithm {

    private static final int INF = Integer.MAX_VALUE / 2;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(new File("input.txt"));
            int vertices = scanner.nextInt();

            int[][] graph = new int[vertices][vertices];
            for (int i = 0; i < vertices; i++) {
                for (int j = 0; j < vertices; j++) {
                    int weight = scanner.nextInt();
                    graph[i][j] = (weight == -1) ? INF : weight;
                }
            }

            floydWarshall(graph);

            System.out.println("Shortest distances between all pairs of vertices:");
            for (int i = 0; i < vertices; i++) {
                for (int j = 0; j < vertices; j++) {
                    System.out.print(graph[i][j] + " ");
                }
                System.out.println();
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void floydWarshall(int[][] graph) {
        int vertices = graph.length;

        for (int k = 0; k < vertices; k++) {
            for (int i = 0; i < vertices; i++) {
                for (int j = 0; j < vertices; j++) {
                    if (graph[i][k] != INF && graph[k][j] != INF && graph[i][k] + graph[k][j] < graph[i][j]) {
                        graph[i][j] = graph[i][k] + graph[k][j];
                    }
                }
            }
        }
    }
}
