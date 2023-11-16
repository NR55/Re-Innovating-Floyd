import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FA_GUI1 {
    private static final int INF = Integer.MAX_VALUE / 2;

    private JFrame frame;
    private JTabbedPane tabbedPane;
    private GraphPanel graphPanel;
    private JPanel selectionPanel;
    private JComboBox<String> sourceComboBox;
    private JComboBox<String> destinationComboBox;
    private JTextArea outputTextArea;

    private int[][] graph;
    private Map<Integer, String> vertexNames;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new FA_GUI1().initialize();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() throws FileNotFoundException {
        frame = new JFrame("Floyd's Algorithm GUI");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();

        // Create the graph panel
        graphPanel = new GraphPanel();

        // Create the selection panel
        selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setLayout(new FlowLayout());

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
        selectionPanel.add(new JLabel("Source Vertex:"));
        selectionPanel.add(sourceComboBox);

        // Destination vertex dropdown
        destinationComboBox = new JComboBox<>(vertexLabels);
        selectionPanel.add(new JLabel("Destination Vertex:"));
        selectionPanel.add(destinationComboBox);

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

                outputTextArea.setText("Shortest distance between " + sourceName + " and " + destinationName + ": "
                        + shortestDistance);

            }
        });
        selectionPanel.add(calculateButton);

        // Output text area
        outputTextArea = new JTextArea(5, 50);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        selectionPanel.add(scrollPane);

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

            if (sourceVertex >= 0 && sourceVertex < vertices && destinationVertex >= 0
                    && destinationVertex < vertices) {
                graph[sourceVertex][destinationVertex] = weight;
                graph[destinationVertex][sourceVertex] = weight;
            } else {
                // Handle invalid vertex indices
                System.err.println("Invalid vertex indices in input file");
            }
        }

        // Close the scanner after reading the entire file
        scanner.close();

        tabbedPane.addTab("Node Selection", selectionPanel);
        tabbedPane.addTab("Graph", graphPanel);
        frame.add(tabbedPane);
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

    private class GraphPanel extends JPanel {
        private static final int CIRCLE_RADIUS = 200;
        private static final int CENTER_X = 300;
        private static final int CENTER_Y = 250;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw the graph edges
            for (int i = 0; i < graph.length; i++) {
                for (int j = 0; j < graph[i].length; j++) {
                    if (graph[i][j] != INF) {
                        double angle1 = 2 * Math.PI * i / vertexNames.size();
                        double angle2 = 2 * Math.PI * j / vertexNames.size();

                        int x1 = (int) (CENTER_X + CIRCLE_RADIUS * Math.cos(angle1));
                        int y1 = (int) (CENTER_Y + CIRCLE_RADIUS * Math.sin(angle1));
                        int x2 = (int) (CENTER_X + CIRCLE_RADIUS * Math.cos(angle2));
                        int y2 = (int) (CENTER_Y + CIRCLE_RADIUS * Math.sin(angle2));

                        g.drawLine(x1, y1, x2, y2); // Draw a line for each edge
                    }
                }
            }

            // Draw the graph nodes (vertices)
            for (int i = 0; i < graph.length; i++) {
                double angle = 2 * Math.PI * i / vertexNames.size();
                int x = (int) (CENTER_X + CIRCLE_RADIUS * Math.cos(angle)) - 10; // Center vertices
                int y = (int) (CENTER_Y + CIRCLE_RADIUS * Math.sin(angle)) - 10; // Center vertices

                g.fillOval(x, y, 20, 20); // Draw a circle for each node
                g.drawString(vertexNames.get(i + 1), x - 5, y - 5); // Display node name
            }
        }
    }
}
