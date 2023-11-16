import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FloydAlgorithmGUI {
    private static final int INF = Integer.MAX_VALUE / 2;

    private JFrame frame;
    private JComboBox<String> sourceComboBox;
    private JComboBox<String> destinationComboBox;
    private JTextArea outputTextArea;

    private int[][] graph; // Move graph matrix outside to store it for later use

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new FloydAlgorithmGUI().initialize();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() throws FileNotFoundException {
        frame = new JFrame("Floyd's Algorithm GUI");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // Read input from file
        Scanner scanner = new Scanner(new File("input.txt"));
        int vertices = scanner.nextInt();

        String[] vertexLabels = new String[vertices];
        for (int i = 0; i < vertices; i++) {
            vertexLabels[i] = String.valueOf(i + 1);
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

                outputTextArea.setText("Shortest distance from Vertex " + (sourceVertex + 1) + " to Vertex " +
                        (destinationVertex + 1) + ": " + shortestDistance);
            }
        });
        frame.add(calculateButton);

        // Output text area
        outputTextArea = new JTextArea(5, 30);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        frame.add(scrollPane);

        // Read the graph matrix from the file and store it
        graph = new int[vertices][vertices];
        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                int weight = scanner.nextInt();
                graph[i][j] = (weight == -1) ? INF : weight;
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
}
