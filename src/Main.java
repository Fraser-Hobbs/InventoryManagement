import net.proteanit.sql.DbUtils;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;




public class Main {
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
    private JTextArea txt_Comment;
    private JScrollPane tbl_Inventory_Scroll;
    boolean firstConnect = true;


    public static void main(String[] args) {
        // Create GUI
        JFrame frame = new JFrame("Inventory Manager");
        frame.setContentPane(new Main().Main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private Connection connect() {
        String URL = "jdbc:sqlite:Inventory.db";
        Connection conn = null;
        String SQL = "CREATE TABLE IF NOT EXISTS Inventory (\n" +
                "    _ID    INTEGER PRIMARY KEY AUTOINCREMENT default 0 NOT NULL,\n" +
                "    itemName     TEXT              not null,\n" +
                "    itemComment  TEXT,\n" +
                "    itemQuantity INTEGER default 0 not null\n" +
                ");";

        try {
            conn = DriverManager.getConnection(URL);
            if (conn != null && firstConnect == true) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Driver name: " + meta.getDriverName());
                System.out.println("Driver version: " + meta.getDriverVersion());
                System.out.println("Product name: " + meta.getDatabaseProductName());
                System.out.println("Product version: " + meta.getDatabaseProductVersion());
                System.out.println("Connected To DataBase.");
                firstConnect = false;
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return conn;
    }

    public boolean insert (String name, String comment, int quantity) {

        String SQL = "INSERT INTO Inventory(itemName, itemComment, itemQuantity) VALUES(?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, comment);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }


    void loadTable () {
        try (PreparedStatement pstmt = connect().prepareStatement("SELECT _ID, itemName, itemComment, itemQuantity from Inventory")) {

            ResultSet rs = pstmt.executeQuery();
            tbl_Inventory.setModel(DbUtils.resultSetToTableModel(rs));
            tbl_Inventory.setDefaultEditor(Object.class, null);
            tbl_Inventory.getTableHeader().setEnabled(false);

//          Set Column Names
            JTableHeader header= tbl_Inventory.getTableHeader();
            TableColumnModel colMod = header.getColumnModel();
            String[] colNames = {"ID","Name","Comment","Quantity"};
            for (int i = 0; i < tbl_Inventory.getColumnCount(); i++) {
                TableColumn tabCol = colMod.getColumn(i);
                tabCol.setHeaderValue(colNames[i]);
                header.repaint();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Main() {
        connect();
        loadTable();
        btn_Add.addActionListener(e -> {

            String name = txt_Name.getText();
            String comment = txt_Comment.getText();
            int quantity = (int) txt_Quantity.getValue();

            if (name.length() < 1) {
                JOptionPane.showMessageDialog(null, "Record Failed To Add\nName Field Needs Completed");
            } else if (name.length() > 1){
                if (!insert(name, comment, quantity)) {
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


}