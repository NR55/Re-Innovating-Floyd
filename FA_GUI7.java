import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FA_GUI7 {
    private static final int INF = Integer.MAX_VALUE / 2;

    private JFrame frame;
    private JTabbedPane tabbedPane;
    private GraphPanel graphPanel;
    private JPanel selectionPanel;
    private JComboBox<String> sourceComboBox;
    private JComboBox<String> destinationComboBox;
    private JTextArea outputTextArea;
    private JTable citiesTable;
    private JTable pathsTable;

    private int[][] graph;
    private int[][] shortests;
    private Map<Integer, String> vertexNames;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new FA_GUI7().initialize();
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

        graphPanel = new GraphPanel();

        selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setLayout(new FlowLayout());

        vertexNames = readVertexNames("vertex_names.txt");

        Scanner scanner = new Scanner(new File("input.txt"));
        int edges = scanner.nextInt();

        String[] vertexLabels = new String[vertexNames.size()];
        int vertexIndex = 0;

        for (Map.Entry<Integer, String> entry : vertexNames.entrySet()) {
            vertexLabels[vertexIndex++] = entry.getValue();
        }

        sourceComboBox = new JComboBox<>(vertexLabels);
        selectionPanel.add(new JLabel("Source Vertex:"));
        selectionPanel.add(sourceComboBox);

        destinationComboBox = new JComboBox<>(vertexLabels);
        selectionPanel.add(new JLabel("Destination Vertex:"));
        selectionPanel.add(destinationComboBox);

        JButton calculateButton = new JButton("Calculate Shortest Distance");
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sourceVertex = sourceComboBox.getSelectedIndex();
                int destinationVertex = destinationComboBox.getSelectedIndex();

                // Check if both source and destination vertices are valid
                if (sourceVertex >= 0 && destinationVertex >= 0 &&
                        sourceVertex < vertexNames.size() && destinationVertex < vertexNames.size()) {

                    int shortestDistance = shortests[sourceVertex][destinationVertex];

                    String sourceName = vertexNames.get(sourceVertex + 1);
                    String destinationName = vertexNames.get(destinationVertex + 1);

                    outputTextArea.setText("Shortest distance between " + sourceName + " and " + destinationName + ": "
                            + shortestDistance);
                } else {
                    outputTextArea.setText("Invalid source or destination vertex selected.");
                }
            }
        });

        selectionPanel.add(calculateButton);

        outputTextArea = new JTextArea(5, 50);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        selectionPanel.add(scrollPane);

        int vertices = vertexNames.size();
        graph = new int[vertices][vertices];
        shortests = new int[vertices][vertices];

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                graph[i][j] = (i == j) ? 0 : INF;
                shortests[i][j] = (i == j) ? 0 : INF;
            }
        }

        for (int i = 0; i < edges; i++) {
            int sourceId = scanner.nextInt();
            int weight = scanner.nextInt();
            int destinationId = scanner.nextInt();

            int sourceVertex = sourceId - 1;
            int destinationVertex = destinationId - 1;

            if (sourceVertex >= 0 && sourceVertex < vertices && destinationVertex >= 0
                    && destinationVertex < vertices) {
                graph[sourceVertex][destinationVertex] = weight;
                graph[destinationVertex][sourceVertex] = weight;
            } else {
                System.err.println("Invalid vertex indices in input file");
            }
        }

        scanner.close();

        performFloydWarshall();

        addCitiesTab();
        addPathsTab();

        tabbedPane.addTab("Node Selection", selectionPanel);
        tabbedPane.addTab("Graph", graphPanel);
        tabbedPane.addTab("Cities", new JScrollPane(citiesTable));
        tabbedPane.addTab("Paths", new JScrollPane(pathsTable));

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private void performFloydWarshall() {
        int vertices = graph.length;

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                shortests[i][j] = graph[i][j];
            }
        }

        for (int k = 0; k < vertices; k++) {
            for (int i = 0; i < vertices; i++) {
                for (int j = 0; j < vertices; j++) {
                    if (shortests[i][k] != INF && shortests[k][j] != INF
                            && shortests[i][k] + shortests[k][j] < shortests[i][j]) {
                        shortests[i][j] = shortests[i][k] + shortests[k][j];
                    }
                }
            }
        }
    }

    private Map<Integer, String> readVertexNames(String fileName) throws FileNotFoundException {
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

    private void addCitiesTab() throws FileNotFoundException {
        // Existing code for adding Cities tab remains unchanged...
    }

    private void addPathsTab() throws FileNotFoundException {
        String[] pathsHeaders = { "Source City", "Distance", "Destination City" };
        Object[][] pathsData = readPathsFromFile("input.txt");

        DefaultTableModel pathsTableModel = new DefaultTableModel(pathsData, pathsHeaders) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pathsTable = new JTable(pathsTableModel);
        pathsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pathsTable.setAutoCreateRowSorter(true);
        pathsTable.getTableHeader().setReorderingAllowed(false);

        JButton addPathButton = new JButton("Add Path");
        addPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPathPrompt();
            }
        });

        JPanel pathsPanel = new JPanel(new BorderLayout());
        pathsPanel.add(new JScrollPane(pathsTable), BorderLayout.CENTER);
        pathsPanel.add(addPathButton, BorderLayout.SOUTH);

        tabbedPane.addTab("Paths", pathsPanel);
    }

    private void addPathPrompt() {
        // Code for the "Add Path" prompt window remains unchanged...
    }

    private void addPathToFile(String sourceCity, String destinationCity, String weight) {
        // Code for adding the path to the file remains unchanged...
    }

    private int getKeyFromValue(Map<Integer, String> map, String value) {
        // Code for retrieving key from value in a map remains unchanged...
    }

    private void refreshPathsTab() {
        // Code for refreshing Paths tab remains unchanged...
    }

    private class GraphPanel extends JPanel {
        // Code for the GraphPanel class remains unchanged...
    }
}
