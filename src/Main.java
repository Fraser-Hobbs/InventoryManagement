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

//----------------------------------------------------------------------------------------------------------------------

//   UI Components

    //    JPanels
    private JPanel Main;
    @SuppressWarnings("unused")
    private JPanel infoPanel;
    @SuppressWarnings("unused")
    private JPanel panel_left;
    @SuppressWarnings("unused")
    private JPanel panel_right;
    @SuppressWarnings("unused")
    private JPanel deviceID;
    @SuppressWarnings("unused")
    private JPanel roomID;
    @SuppressWarnings("unused")
    private JPanel Ram;
    private JPanel Processor;
    @SuppressWarnings("unused")
    private JPanel systemType;
    @SuppressWarnings("unused")
    private JPanel ButtonPanel;

    //  JTextFields, ComboBox's & TextAreas
    private JTextField txt_DeviceID;
    private JTextField txt_roomID;
    private JTextField txt_Ram;
    private JTextField txt_Processor;
    private JTextArea txt_systemType;
    private JTextField txt_Search;
    private JComboBox<String> searchOptions;


    //  JButtons
    private JButton btn_Add;
    private JButton btn_Update;
    private JButton btb_Delete;
    private JButton btn_Clear;

    //  JTable & JScrollPane
    private JTable tbl_Inventory;
    @SuppressWarnings("unused")
    private JScrollPane tbl_Inventory_Scroll;

    //----------------------------------------------------------------------------------------------------------------------
    public Main() {
        connect();
        loadTable(null);

//      Create Search Categories for JComboBox
        String[] searchCategories = {"deviceID", "Processor", "Pro", "systemType", "roomID"};
        for (String category : searchCategories) {
            searchOptions.addItem(category);
        }
//      Set Default Selection for JComboBox
        searchOptions.setSelectedItem(searchCategories[0]);

//      Button - ADD Handler
        btn_Add.addActionListener(e -> {
            String ID = txt_DeviceID.getText();
            String Processor = txt_Processor.getText();
            String Ram = txt_Ram.getText();
            String systemType = txt_systemType.getText();
            String roomID = txt_roomID.getText();
            if (ID.length() < 1) {
                JOptionPane.showMessageDialog(null, "Record Failed To Add\nName Field Needs Completed");
            } else {
                insert(ID, Processor, Ram, systemType, roomID);

                loadTable(null);
            }

        });

//      TABLE -  Double-Click Handler
        tbl_Inventory.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    selectedID = (String) tbl_Inventory.getValueAt(row, 0);
                    search((String) tbl_Inventory.getValueAt(row, 0));

                }
            }
        });

//      SEARCH - Change Handler
        addChangeListener(txt_Search, e -> loadTable(txt_Search.getText()));
        searchOptions.addActionListener(e -> loadTable(txt_Search.getText()));


//      BUTTON - Delete Handler
        btb_Delete.addActionListener(e -> delete(selectedID));
//      BUTTON - Update Handler
        btn_Update.addActionListener(e -> update(selectedID));
//      BUTTON - Clear Handler
        btn_Clear.addActionListener(e -> clear());
    }


    public static void main(String[] args) {
//      JFrame Setup & Initialisation
        JFrame frame = new JFrame("Inventory Manager");
        frame.setContentPane(new Main().Main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    //    Database - Connect & Create
    private Connection connect() {
        String URL = "jdbc:sqlite:data/Inventory.db";
        Connection conn = null;
        String SQL = """
                CREATE TABLE IF NOT EXISTS Inventory (
                    _ID    INTEGER PRIMARY KEY AUTOINCREMENT default 0 NOT NULL,
                    deviceID        TEXT              not null,
                    Processor  TEXT,
                    Ram  TEXT,
                    systemType TEXT,
                    roomID TEXT
                );""";

        try {
            conn = DriverManager.getConnection(URL);

            if (conn != null && firstConnect) {
//              Database Driver Info
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

    public void insert(String ID, String Processor, String Ram, String systemType, String roomID) {

        if (search(ID)) {
            JOptionPane.showMessageDialog(null, "Device ID Already Exists");
        } else {
            String SQL = "INSERT INTO Inventory(deviceID, Processor, Ram, systemType, roomID) VALUES(?,?,?,?,?)";

            try (Connection conn = this.connect();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, ID);
                stmt.setString(2, Processor);
                stmt.setString(3, Ram);
                stmt.setString(4, systemType);
                stmt.setString(5, roomID);
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
        String deviceID = txt_DeviceID.getText();
        String Processor = txt_Processor.getText();
        String Ram = txt_Ram.getText();
        String systemType = txt_systemType.getText();
        String roomID = txt_roomID.getText();

        if (!Objects.equals(selectedID, deviceID) && search(deviceID) && selectedID != null) {
            JOptionPane.showMessageDialog(null, "Device ID Already In Use!");
        } else {
            if (search(selectedID)) {
                String SQL = "UPDATE Inventory SET deviceID = ?,Processor = ?,Ram = ?, systemType = ?, roomID = ? WHERE deviceID = ?";
                try {
                    Connection conn = this.connect();
                    PreparedStatement stmt = conn.prepareStatement(SQL);
                    stmt.setString(1, deviceID);
                    stmt.setString(2, Processor);
                    stmt.setString(3, Ram);
                    stmt.setString(4, systemType);
                    stmt.setString(5, roomID);
                    stmt.setString(6, selectedID);
                    stmt.executeUpdate();
                    clear();
                    loadTable(null);
                    search(selectedID);
                    JOptionPane.showMessageDialog(null, "Record Updated!");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Record Could Not Be Updated");
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    public void delete(String selectedID) {
        String SQL = "DELETE FROM Inventory WHERE deviceID = ?";

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
        txt_DeviceID.setText("");
        txt_Processor.setText("");
        txt_Ram.setText("");
        txt_systemType.setText("");
        txt_roomID.setText("");
    }

    public boolean search(String searchID) {
        try {

            PreparedStatement stmt = connect().prepareStatement("SELECT deviceID, Processor, Ram, systemType, roomID from Inventory WHERE deviceID LIKE ?");
            stmt.setString(1, searchID);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                String deviceID = resultSet.getString(1);
                String Processor = resultSet.getString(2);
                String Ram = resultSet.getString(3);
                String systemType = resultSet.getString(4);
                String roomID = resultSet.getString(5);
//              If Results found display the fields
                txt_DeviceID.setText(deviceID);
                txt_Processor.setText(Processor);
                txt_Ram.setText(Ram);
                txt_systemType.setText(systemType);
                txt_roomID.setText(roomID);
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
                stmt = connect().prepareStatement("SELECT deviceID, Processor, Ram, systemType, roomID from Inventory WHERE " + searchOptions.getSelectedItem() + " LIKE ? ORDER BY deviceID");
                stmt.setString(1, ("%" + SearchCriteria + "%"));
                ResultSet rs = stmt.executeQuery();
                tbl_Inventory.setModel(DbUtils.resultSetToTableModel(rs));

            } else {
                stmt = connect().prepareStatement("SELECT deviceID, Processor, Ram, systemType, roomID from Inventory ORDER BY deviceID");
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
            String[] colNames = {"Device ID", "Processor", "RAM", "System Type", "Room ID"};
            for (int i = 0; i < tbl_Inventory.getColumnCount(); i++) {
                TableColumn tabCol = colMod.getColumn(i);
                tabCol.setHeaderValue(colNames[i]);
                header.repaint();
            }
            //      Set Table Column Preferred Widths
            tbl_Inventory.getColumnModel().getColumn(0).setPreferredWidth(280);
            tbl_Inventory.getColumnModel().getColumn(1).setPreferredWidth(280);
            tbl_Inventory.getColumnModel().getColumn(2).setPreferredWidth(60);
            tbl_Inventory.getColumnModel().getColumn(3).setPreferredWidth(450);
            tbl_Inventory.getColumnModel().getColumn(4).setPreferredWidth(280);
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

