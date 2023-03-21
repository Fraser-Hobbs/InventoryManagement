import net.proteanit.sql.DbUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.sql.*;
import java.util.Objects;

public class Main {
//    Global Variables
    boolean firstConnect = true;
    String selectedID = null;
//    UI Components
    private JPanel Main;
    private JTable tbl_Inventory;
    private JTextField txt_ComputerID;
    private JButton btn_Add;
    private JButton btn_Update;
    private JButton btb_Delete;
    private JTextField txt_Search;
    private JPanel infoPanel;
    private JTextArea txt_Software;
    private JTextArea txt_Hardware;
    private JButton btn_Clear;
    private JComboBox<String> searchOptions;
    private JPanel panel_left;
    private JPanel panel_right;
    private JScrollPane tbl_Inventory_Scroll;

    public Main() {
        connect();
        loadTable(null);

//      Create Search Categories for JComboBox
        String[] searchCategories = {"computerID", "Software", "Hardware"};
        for (String category : searchCategories) {
            searchOptions.addItem(category);
        }
//      Set Default Selection for JComboBox
        searchOptions.setSelectedItem(searchCategories[0]);

//      Add Button Handler
        btn_Add.addActionListener(e -> {
            String ID = txt_ComputerID.getText();
            String Software = txt_Software.getText();
            String Hardware = txt_Hardware.getText();
            if (ID.length() < 1) {
                JOptionPane.showMessageDialog(null, "Record Failed To Add\nName Field Needs Completed");
            } else {
                System.out.println("Attempting to Insert Data: ");
                insert(ID, Software, Hardware);

                loadTable(null);
            }

        });

//      Table Double Click Handler
        tbl_Inventory.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    selectedID = (String) tbl_Inventory.getValueAt(row, 0);
                    System.out.println("SelectedID: " + selectedID);
                    search((String) tbl_Inventory.getValueAt(row, 0));

                }
            }
        });
//      Listens For Change in Search Field
        addChangeListener(txt_Search, e -> loadTable(txt_Search.getText()));

        searchOptions.addActionListener(e -> loadTable(txt_Search.getText()));

        btb_Delete.addActionListener(e -> delete(selectedID));

        btn_Update.addActionListener(e -> update(selectedID));

        btn_Clear.addActionListener(e -> clear());
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Inventory Manager");

        frame.setContentPane(new Main().Main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private Connection connect() {
        String URL = "jdbc:sqlite:data/Inventory.db";
        Connection conn = null;
        String SQL = """
                CREATE TABLE IF NOT EXISTS Inventory (
                    _ID    INTEGER PRIMARY KEY AUTOINCREMENT default 0 NOT NULL,
                    computerID        TEXT              not null,
                    Software  TEXT,
                    Hardware  TEXT
                );""";

        try {
            conn = DriverManager.getConnection(URL);

            if (conn != null && firstConnect) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Driver name: " + meta.getDriverName());
                System.out.println("Driver version: " + meta.getDriverVersion());
                System.out.println("Product name: " + meta.getDatabaseProductName());
                System.out.println("Product version: " + meta.getDatabaseProductVersion());
                System.out.println("Connected To DataBase.");
                Statement stmt = conn.createStatement();
                {
                    stmt.execute(SQL);
                }
                firstConnect = false;

            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return conn;
    }

    public void insert(String ID, String Software, String Hardware) {

        if (search(ID)) {
            JOptionPane.showMessageDialog(null, "Computer ID Already Exists");
        } else {
            String SQL = "INSERT INTO Inventory(computerID, Software, Hardware) VALUES(?,?,?)";

            try (Connection conn = this.connect();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, ID);
                stmt.setString(2, Software);
                stmt.setString(3, Hardware);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Record Successfully Added!");
                clear();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Record Failed To Add");
                System.out.println(e.getMessage());
            }
        }

    }

    public void update(String selectedID) {
        String computerID = txt_ComputerID.getText();
        String Software = txt_Software.getText();
        String Hardware = txt_Hardware.getText();
        System.out.println("selectedID: " + selectedID + "\n" + "computerID: " + computerID + "\n" + "Software: " + Software + "\n" + "Hardware: " + Hardware + "\n");

        if (!Objects.equals(selectedID, computerID) && search(computerID) && selectedID != null) {
            JOptionPane.showMessageDialog(null, "Computer ID Already In Use!");
        } else {
            if (search(selectedID)) {
                String SQL = "UPDATE Inventory SET computerID = ?,Software = ?,Hardware = ? WHERE computerID = ?";
                System.out.println("selectedID: " + selectedID + "\n" + "computerID: " + computerID + "\n" + "Software: " + Software + "\n" + "Hardware: " + Hardware + "\n");

                try {
                    Connection conn = this.connect();
                    PreparedStatement stmt = conn.prepareStatement(SQL);
                    stmt.setString(1, computerID);
                    stmt.setString(2, Software);
                    stmt.setString(3, Hardware);
                    stmt.setString(4, selectedID);
                    stmt.executeUpdate();
                    loadTable(null);
                    JOptionPane.showMessageDialog(null, "Record Updated!");
                    clear();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Record Could Not Be Updated");
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    public void delete(String selectedID) {
        String SQL = "DELETE FROM Inventory WHERE computerID = ?";

        try {
            Connection conn = this.connect();
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setString(1, selectedID);
            stmt.executeUpdate();
            loadTable(null);
            clear();
            JOptionPane.showMessageDialog(null, "Record Deleted!");


        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }

    public void clear() {
        selectedID = null;
        txt_ComputerID.setText("");
        txt_Software.setText("");
        txt_Hardware.setText("");
    }

    public boolean search(String searchID) {
        try {

            PreparedStatement stmt = connect().prepareStatement("SELECT computerID, Software, Hardware from Inventory WHERE computerID LIKE ?");
            stmt.setString(1, searchID);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                String computerID = resultSet.getString(1);
                String computerSoftware = resultSet.getString(2);
                String computerHardware = resultSet.getString(3);
//              If Results found display the fields
                txt_ComputerID.setText(computerID);
                txt_Software.setText(computerSoftware);
                txt_Hardware.setText(computerHardware);
                stmt.close();
                resultSet.close();
                return true;
            } else {
                return false;
            }


        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }


    }

    public void loadTable(String SearchCriteria) {
        try {
            PreparedStatement stmt;
            if (SearchCriteria != null) {
                stmt = connect().prepareStatement("SELECT computerID, Software, Hardware from Inventory WHERE " + searchOptions.getSelectedItem() + " LIKE ? ORDER BY computerID");
                stmt.setString(1, ("%" + SearchCriteria + "%"));
                ResultSet rs = stmt.executeQuery();
                tbl_Inventory.setModel(DbUtils.resultSetToTableModel(rs));

            } else {
                stmt = connect().prepareStatement("SELECT computerID, Software, Hardware from Inventory ORDER BY computerID");
                ResultSet rs = stmt.executeQuery();
                tbl_Inventory.setModel(DbUtils.resultSetToTableModel(rs));
            }
            tbl_Inventory.setDefaultEditor(Object.class, null);
            tbl_Inventory.getTableHeader().setEnabled(false);


        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally {
            JTableHeader header = tbl_Inventory.getTableHeader();
            TableColumnModel colMod = header.getColumnModel();
            String[] colNames = {"Computer ID", "Software", "Hardware"};
            for (int i = 0; i < tbl_Inventory.getColumnCount(); i++) {
                TableColumn tabCol = colMod.getColumn(i);
                tabCol.setHeaderValue(colNames[i]);
                header.repaint();
            }
        }


    }

    public void addChangeListener(JTextComponent text, ChangeListener changeListener) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(changeListener);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0, lastNotifiedChange = 0;

            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange;
                        changeListener.stateChanged(new ChangeEvent(text));
                    }
                });
            }
        };
        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document d1 = (Document) e.getOldValue();
            Document d2 = (Document) e.getNewValue();
            if (d1 != null) d1.removeDocumentListener(dl);
            if (d2 != null) d2.addDocumentListener(dl);
            dl.changedUpdate(null);
        });
        Document d = text.getDocument();
        if (d != null) d.addDocumentListener(dl);
    }

}

