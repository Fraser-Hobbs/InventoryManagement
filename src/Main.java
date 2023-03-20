import net.proteanit.sql.DbUtils;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.sql.*;


public class Main {
    boolean firstConnect = true;
    private JPanel Main;
    private JTable tbl_Inventory;
    private JTextField txt_Name;
    private JButton btn_Add;
    private JButton btn_Delete;
    private JButton btn_Update;
    private JButton btn_Search;
    private JTextField txt_Search;
    private JPanel infoPanel;
    private JPanel buttonPanel;
    private JSpinner txt_Quantity;
    private JTextArea txt_Software;
    private JTextArea txt_Hardware;
    private JScrollPane tbl_Inventory_Scroll;


    public Main() {
        connect();
        loadTable();
        btn_Add.addActionListener(e -> {

            String ID = txt_Name.getText();
            String Software = txt_Software.getText();
            String Hardware = txt_Hardware.getText();

            if (ID.length() < 1) {
                JOptionPane.showMessageDialog(null, "Record Failed To Add\nName Field Needs Completed");
            } else if (ID.length() > 1) {
                if (!insert(ID, Software, Hardware)) {
                    JOptionPane.showMessageDialog(null, "Record Failed To Add");
                    loadTable();
                } else {
                    JOptionPane.showMessageDialog(null, "Record Successfully Added!");
                    loadTable();
                }
            }


        });
        btn_Search.addActionListener(e -> {

            try {
                String id = txt_Search.getText();

                PreparedStatement pstmt = connect().prepareStatement("SELECT itemName, itemComment, itemQuantity from Inventory WHERE id = ?");
                pstmt.setString(1, id);


            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        });
    }

    public static void main(String[] args) {
        // Create GUI
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
                    computerID     INTEGER              not null,
                    computerSoftware  TEXT,
                    computerHardware  TEXT
                );""";

        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Conn: " + conn);

            if (conn != null && firstConnect) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Driver name: " + meta.getDriverName());
                System.out.println("Driver version: " + meta.getDriverVersion());
                System.out.println("Product name: " + meta.getDatabaseProductName());
                System.out.println("Product version: " + meta.getDatabaseProductVersion());
                System.out.println("Connected To DataBase.");
                Statement stmt = conn.createStatement(); {
                    stmt.execute(SQL);
                }
                firstConnect = false;

            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return conn;
    }

    public boolean insert(String ID, String Software, String Hardware) {

        String SQL = "INSERT INTO Inventory(computerID, computerSoftware, computerHardware) VALUES(?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, ID);
            pstmt.setString(2, Software);
            pstmt.setString(3, Hardware);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }

    void loadTable() {
        try (PreparedStatement pstmt = connect().prepareStatement("SELECT _ID, computerID, computerSoftware, computerHardware from Inventory")) {

            ResultSet rs = pstmt.executeQuery();
            tbl_Inventory.setModel(DbUtils.resultSetToTableModel(rs));
            tbl_Inventory.setDefaultEditor(Object.class, null);
            tbl_Inventory.getTableHeader().setEnabled(false);

//          Set Column Names
            JTableHeader header = tbl_Inventory.getTableHeader();
            TableColumnModel colMod = header.getColumnModel();
            String[] colNames = {"ID", "Computer ID", "Software", "Hardware"};
            for (int i = 0; i < tbl_Inventory.getColumnCount(); i++) {
                TableColumn tabCol = colMod.getColumn(i);
                tabCol.setHeaderValue(colNames[i]);
                header.repaint();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}