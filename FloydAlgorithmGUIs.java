import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FloydAlgorithmGUIs {
    private static final int INF = Integer.MAX_VALUE / 2;

    private JFrame frame;
    private JComboBox<String> sourceComboBox;
    private JComboBox<String> destinationComboBox;
    private JTextArea outputTextArea;

    private int[][] graph; // Move graph matrix outside to store it for later use
    private Map<Integer, String> vertexNames; // Map to store vertex names

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new FloydAlgorithmGUIs().initialize();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() throws FileNotFoundException {
        frame = new JFrame("Floyd's Algorithm GUI");
        frame.setSize(700, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // Read vertex names from file
        vertexNames = readVertexNames("vertex_names.txt");

        // Read input from file
        Scanner scanner = new Scanner(new File("input.txt"));
        int edges = scanner.nextInt();

        String[] vertexLabels = new String[vertexNames.size()];
        int vertexIndex = 0;

        for (Map.Entry<Integer, String> entry : vertexNames.entrySet()) {
            vertexLabels[vertexIndex++] = entry.getValue();
        }

        // Source vertex dropdown
        sourceComboBox = new JComboBox<>(vertexLabels);
        frame.add(new JLabel("Source Vertex:"));
        frame.add(sourceComboBox);

        // Destination vertex dropdown
        destinationComboBox = new JComboBox<>(vertexLabels);
        frame.add(new JLabel("Destination Vertex:"));
        frame.add(destinationComboBox);

        // Button to calculate and display shortest distance
        JButton calculateButton = new JButton("Calculate Shortest Distance");
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sourceVertex = sourceComboBox.getSelectedIndex();
                int destinationVertex = destinationComboBox.getSelectedIndex();

                int shortestDistance = floydWarshall(graph, sourceVertex, destinationVertex);

                String sourceName = vertexNames.get(sourceVertex + 1); // Adjust back to 1-based index
                String destinationName = vertexNames.get(destinationVertex + 1); // Adjust back to 1-based index

                outputTextArea.setText("Shortest distance between " + sourceName + " and " + destinationName + ": " + shortestDistance);
            }
        });
        frame.add(calculateButton);

        // Output text area
        outputTextArea = new JTextArea(5, 50);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        frame.add(scrollPane);

        // Read the graph matrix from the file and store it
        int vertices = vertexNames.size();
        graph = new int[vertices][vertices];
        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                graph[i][j] = (i == j) ? 0 : INF;
            }
        }

        for (int i = 0; i < edges; i++) {
            int sourceId = scanner.nextInt();
            int weight = scanner.nextInt();
            int destinationId = scanner.nextInt();

            int sourceVertex = sourceId - 1; // Adjust to zero-based index
            int destinationVertex = destinationId - 1; // Adjust to zero-based index

            if (sourceVertex >= 0 && sourceVertex < vertices && destinationVertex >= 0 && destinationVertex < vertices) {
                graph[sourceVertex][destinationVertex] = weight;
                graph[destinationVertex][sourceVertex] = weight;
            } else {
                // Handle invalid vertex indices
                System.err.println("Invalid vertex indices in input file");
            }
        }

        // Close the scanner after reading the entire file
        scanner.close();

        frame.setVisible(true);
    }

    private static int floydWarshall(int[][] graph, int source, int destination) {
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

        return graph[source][destination];
    }

    private static Map<Integer, String> readVertexNames(String fileName) throws FileNotFoundException {
        Map<Integer, String> vertexNames = new HashMap<>();
        Scanner scanner = new Scanner(new File(fileName));

        while (scanner.hasNext()) {
            int vertexId = scanner.nextInt();
            String vertexName = scanner.next();
            vertexNames.put(vertexId, vertexName);
        }

        scanner.close();
        return vertexNames;
    }
}
