import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FA_GUI9 {
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
    int edges;
    private Map<Integer, String> vertexNames;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new FA_GUI9().initialize();
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
        edges = countLines("input.txt");

        String[] vertexLabels = new String[vertexNames.size()];
        int vertexIndex = 0;

        for (Map.Entry<Integer, String> entry : vertexNames.entrySet()) {
            vertexLabels[vertexIndex++] = entry.getValue();
        }

        int vertices = vertexNames.size();
        graph = new int[vertices][vertices];
        shortests = new int[vertices][vertices];

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < i; j++) {
                graph[i][j] = graph[j][i] = shortests[i][j] = shortests[j][i] = INF;
            }
            graph[i][i] = shortests[i][i] = 0;
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
                    if (shortestDistance == INF) {
                        outputTextArea
                                .setText("No valid path between " + sourceName + " and " + destinationName + ".");
                    } else {
                        outputTextArea
                                .setText("Shortest distance between " + sourceName + " and " + destinationName + ": "
                                        + shortestDistance);
                    }
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

        scanner.close();

        performFloydWarshall();

        tabbedPane.addTab("Node Selection", selectionPanel);
        tabbedPane.addTab("Graph", graphPanel);
        addCitiesTab();
        addPathsTab();
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

        LocalDateTime startTime = LocalDateTime.now();
        for (int k = 0; k < vertices; k++) {
            for (int i = 1; i < vertices; i++) {
                for (int j = 0; j < i; j++) {
                    if (shortests[i][k] != INF && shortests[k][j] != INF
                            && shortests[i][k] + shortests[k][j] < shortests[i][j]) {
                        shortests[i][j] = shortests[i][k] + shortests[k][j];
                        shortests[j][i] = shortests[i][j];
                    }
                }
            }
        }
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        System.out.println("Time Difference: " + duration.toNanos() + "ns");
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
        String[] citiesHeaders = { "ID", "City Name", "Rename" };
        Object[][] citiesData = readCitiesFromFile("vertex_names.txt");

        DefaultTableModel citiesTableModel = new DefaultTableModel(citiesData, citiesHeaders) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2;
            }
        };

        citiesTable = new JTable(citiesTableModel);
        citiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        citiesTable.setAutoCreateRowSorter(true);
        citiesTable.getTableHeader().setReorderingAllowed(false);
        JButton addCityButton = new JButton("Add City");
        addCityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCityName = JOptionPane.showInputDialog(frame, "Enter new city name:");

                if (newCityName != null && !newCityName.trim().isEmpty()) {
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("vertex_names.txt", true));
                        int newCityId = citiesTable.getRowCount() + 1;
                        writer.write(newCityId + " " + newCityName);
                        writer.newLine();
                        writer.close();
                        frame.dispose();
                        SwingUtilities.invokeLater(() -> {
                            try {
                                new FA_GUI9().initialize();
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            }
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        TableColumn renameColumn = citiesTable.getColumnModel().getColumn(2);
        renameColumn.setCellRenderer(new ButtonRenderer());
        renameColumn.setCellEditor(new ButtonEditor(new JTextField(), frame));

        JPanel citiesPanel = new JPanel(new BorderLayout());
        JScrollPane citiesScrollPane = new JScrollPane(citiesTable);
        citiesPanel.add(citiesScrollPane, BorderLayout.CENTER);
        citiesPanel.add(addCityButton, BorderLayout.SOUTH);
        tabbedPane.addTab("Cities", citiesPanel);
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
                String[] options = vertexNames.values().toArray(new String[0]);
                String source = (String) JOptionPane.showInputDialog(frame,
                        "Select Source City:", "Add New Path",
                        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                String destination = (String) JOptionPane.showInputDialog(frame,
                        "Select Destination City:", "Add New Path",
                        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                String distanceInput = JOptionPane.showInputDialog(frame, "Enter the distance:");

                if (source != null && destination != null && distanceInput != null) {
                    int sourceID = getKeyByValue(vertexNames, source);
                    int destinationID = getKeyByValue(vertexNames, destination);

                    if (sourceID != -1 && destinationID != -1 && sourceID != destinationID) {
                        try {
                            int distance = Integer.parseInt(distanceInput);
                            BufferedWriter writer = new BufferedWriter(new FileWriter("input.txt", true));
                            writer.write(sourceID + " " + distance + " " + destinationID);
                            writer.newLine();
                            writer.close();

                            DefaultTableModel model = (DefaultTableModel) pathsTable.getModel();
                            Object[] newRow = { source, distance, destination };
                            model.addRow(newRow);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame,
                                    "Invalid distance format. Please enter a valid number.");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else if (sourceID == destinationID) {
                        JOptionPane.showMessageDialog(frame, "Source and destination cities cannot be the same.");
                    }
                }
                frame.dispose();
                SwingUtilities.invokeLater(() -> {
                    try {
                        new FA_GUI9().initialize();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                });
            }
        });

        JPanel pathsPanel = new JPanel(new BorderLayout());
        JScrollPane pathsScrollPane = new JScrollPane(pathsTable);
        pathsPanel.add(pathsScrollPane, BorderLayout.CENTER);
        pathsPanel.add(addPathButton, BorderLayout.SOUTH);
        tabbedPane.addTab("Paths", pathsPanel);
    }

    private Object[][] readPathsFromFile(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        Object[][] data = new Object[edges][3];

        for (int i = 0; i < edges; i++) {
            int sourceID = scanner.nextInt();
            int weight = scanner.nextInt();
            int destinationID = scanner.nextInt();

            String sourceCity = vertexNames.get(sourceID);
            String destinationCity = vertexNames.get(destinationID);

            data[i][0] = sourceCity;
            data[i][1] = weight;
            data[i][2] = destinationCity;
        }

        scanner.close();
        return data;
    }

    private Object[][] readCitiesFromFile(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        int rows = countLines(fileName);
        Object[][] data = new Object[rows][3];
        int index = 0;

        while (scanner.hasNext()) {
            int id = scanner.nextInt();
            String cityName = scanner.nextLine().trim();
            data[index][0] = id;
            data[index][1] = cityName;
            data[index][2] = "Rename";
            index++;
        }

        scanner.close();
        return data;
    }

    private int countLines(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        int lines = 0;

        while (scanner.hasNextLine()) {
            scanner.nextLine();
            lines++;
        }

        scanner.close();
        return lines;
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText(value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private String label;
        private JButton renameButton;
        private JButton deleteButton;
        private JFrame parentFrame;

        public ButtonEditor(JTextField textField, JFrame frame) {
            super(textField);
            this.parentFrame = frame;

            setClickCountToStart(1);

            renameButton = new JButton("Rename");
            renameButton.addActionListener(e -> {
                String cityName = JOptionPane.showInputDialog(parentFrame, "Enter new city name:");
                if (cityName != null && !cityName.trim().isEmpty()) {
                    int selectedRow = citiesTable.getSelectedRow();
                    citiesTable.getModel().setValueAt(cityName, selectedRow, 1);
                    updateCityNameInFile(selectedRow + 1, cityName);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            label = (value == null) ? "" : value.toString();
            return label.equals("Rename") ? renameButton : deleteButton;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

    private void updateCityNameInFile(int cityId, String newName) {
        String fileName = "vertex_names.txt";
        Path filePath = Paths.get(fileName);

        try {
            List<String> lines = Files.readAllLines(filePath);

            if (cityId > 0 && cityId <= lines.size()) {
                lines.set(cityId - 1, cityId + " " + newName);
                Files.write(filePath, lines);
                System.out.println("City name updated successfully in the file.");
                frame.dispose();
                frame = new JFrame("Floyd's Algorithm GUI");
                initialize();
            } else {
                System.err.println("City ID is out of range.");
            }
        } catch (IOException e) {
            System.err.println("Error occurred while updating city name in the file: " + e.getMessage());
        }
    }

    private int getKeyByValue(Map<Integer, String> map, String value) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private class GraphPanel extends JPanel {
        private static final int CIRCLE_RADIUS = 200;
        private static final int CENTER_X = 300;
        private static final int CENTER_Y = 250;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawGraph(g);
        }

        private void drawGraph(Graphics g) {
            for (int i = 0; i < graph.length; i++) {
                for (int j = 0; j < graph[i].length; j++) {
                    if (graph[i][j] != INF) {
                        double angle1 = 2 * Math.PI * i / vertexNames.size();
                        double angle2 = 2 * Math.PI * j / vertexNames.size();

                        int x1 = (int) (CENTER_X + CIRCLE_RADIUS * Math.cos(angle1));
                        int y1 = (int) (CENTER_Y + CIRCLE_RADIUS * Math.sin(angle1));
                        int x2 = (int) (CENTER_X + CIRCLE_RADIUS * Math.cos(angle2));
                        int y2 = (int) (CENTER_Y + CIRCLE_RADIUS * Math.sin(angle2));

                        g.drawLine(x1, y1, x2, y2);

                        int midX = (x1 + x2) / 2;
                        int midY = (y1 + y2) / 2;

                        // Highlight the distance numbers
                        g.setColor(Color.RED); // Set your preferred color
                        g.setFont(new Font("Arial", Font.BOLD, 12)); // Set your preferred font

                        // Display the highlighted distance near the midpoint
                        String distanceText = String.valueOf(graph[i][j]);
                        g.drawString(distanceText, midX, midY);

                        // Reset color and font to default
                        g.setColor(Color.BLACK);
                        g.setFont(g.getFont().deriveFont(Font.PLAIN));
                    }
                }
            }

            for (int i = 0; i < graph.length; i++) {
                double angle = 2 * Math.PI * i / vertexNames.size();
                int x = (int) (CENTER_X + CIRCLE_RADIUS * Math.cos(angle)) - 10;
                int y = (int) (CENTER_Y + CIRCLE_RADIUS * Math.sin(angle)) - 10;

                g.fillOval(x, y, 20, 20);
                g.drawString(vertexNames.get(i + 1), x - 5, y - 5);
            }
        }
    }
}
