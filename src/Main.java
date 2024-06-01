import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.io.File;


import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class Main {
    private static final int FRAME_WIDTH = 1300;
    private static final int FRAME_HEIGHT = 800;
    private static final int PANEL_GAP = 30;

    private static final String URL = "URL";
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String SELECT_ALL_QUERY = "SELECT * FROM excel_data";
    private static final String INSERT_ORDER_QUERY = "INSERT INTO orders (total) VALUES ( ?)";
    private static final String INSERT_ORDER_ITEM_QUERY = "INSERT INTO order_items (order_item ,item_id , quantity) VALUES (? , ? , ?)";
    private static final String UPDATE_INVENTORY_QUERY = "UPDATE inventory SET quantity = quantity - ? WHERE item_id = ?";


    private static JPanel panelContainer;


    private static JButton cartButton;
    private static JLabel orderCountLabel;
    private static List<Object[]> cartItems = new ArrayList<>();
    private static int cartCount = 0;
    private static int orderCount = 0;

    public static List<Object[]> fetchAllItems() {
        List<Object[]> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_QUERY)) {
            while (rs.next()) {
                Object[] itemData = new Object[4];
                itemData[0] = rs.getInt("id");
                itemData[1] = rs.getString("item");
                itemData[2] = rs.getString("size");
                itemData[3] = rs.getDouble("price");
                items.add(itemData);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch all items: " + e.getMessage());
        }
        return items;
    }
    public static List<Object[]> populateCache() {
        List<Object[]> items = new ArrayList<>();

        String query = "SELECT inventory.item_id, inventory.quantity, excel_data.item, excel_data.size " +
                "FROM inventory " +
                "INNER JOIN excel_data ON inventory.item_id = excel_data.id";

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                int quantity = rs.getInt("quantity");
                String itemName = rs.getString("item");
                String size = rs.getString("size");

                Object[] item = {itemId, itemName, size, quantity};
                items.add(item);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return items;
    }
    public static List<Object[]> fetchItemsByName(String name) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT * FROM excel_data WHERE item LIKE '%" + name + "%'  " ;
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Object[] itemData = new Object[4];
                itemData[0] = rs.getInt("id");
                itemData[1] = rs.getString("item");
                itemData[2] = rs.getString("size");
                itemData[3] = rs.getDouble("price");
                items.add(itemData);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch items by name: " + e.getMessage());
        }
        return items;
    }
    public static List<Object[]> fetchItemsBySize(String num) {
        List<Object[]> items = new ArrayList<>();
        String query = "SELECT * FROM excel_data WHERE size LIKE '%" + num + "%'  " ;
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Object[] itemData = new Object[4];
                itemData[0] = rs.getInt("id");
                itemData[1] = rs.getString("item");
                itemData[2] = rs.getString("size");
                itemData[3] = rs.getDouble("price");
                items.add(itemData);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch items by name: " + e.getMessage());
        }
        return items;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        JPanel buttonPanel1 = new JPanel();
        JPanel buttonPanel = new JPanel();

        JTextField search = new JTextField();
        JButton searchBtn = new JButton("Search");
        search.setMaximumSize(new Dimension(150, 20));
        search.setPreferredSize(new Dimension(150, 20));
        search.setMinimumSize(new Dimension(150, 20));
        searchBtn.addActionListener(e -> {
            String sr = search.getText();
            List<Object[]> Search = fetchItemsByName(sr);
            displayItems(Search);
        });


        JButton allItemsButton = new JButton("All Items");
        JButton store = new JButton("Store");
        JButton pprButton = new JButton("PPR");
        JButton pvcButton = new JButton("PVC");
        JButton peButton = new JButton("PE");
        JButton gsButton = new JButton("GS");
        JButton staffaButton = new JButton("STAFA");
        JButton sizebtn = new JButton("32");
        JButton sizebtn2 = new JButton("25");

        cartButton = new JButton("Cart: " + cartCount);
        orderCountLabel = new JLabel("Orders placed: " + orderCount);

        buttonPanel1.add(allItemsButton);
        buttonPanel.add(pprButton);
        buttonPanel.add(pvcButton);
        buttonPanel.add(peButton);
        buttonPanel.add(gsButton);
        buttonPanel.add(staffaButton);
        buttonPanel.add(sizebtn);
        buttonPanel.add(sizebtn2);
        buttonPanel.add(search);
        buttonPanel.add(searchBtn);
        buttonPanel1.add(cartButton);
        buttonPanel1.add(orderCountLabel);
        buttonPanel1.add(store);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(buttonPanel1);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(buttonPanel);
        frame.add(topPanel, BorderLayout.NORTH);

        panelContainer = new JPanel();
        panelContainer.setLayout(new GridLayout(0, 5, 30, PANEL_GAP));

        JScrollPane scrollPane = new JScrollPane(panelContainer);
        frame.add(scrollPane, BorderLayout.CENTER);

        store.addActionListener(
                e -> {
                    List<Object[]> storeitems = populateCache();
                    displayItems(storeitems);
                }
        );

        allItemsButton.addActionListener(e -> {
            List<Object[]> allItems = fetchAllItems();
            displayItems(allItems);
        });

        sizebtn.addActionListener(e -> {
            List<Object[]> pprItems = fetchItemsBySize("32");
            displayItems(pprItems);
        });
        sizebtn2.addActionListener(e -> {
            List<Object[]> pprItems = fetchItemsBySize("25");
            displayItems(pprItems);
        });

        pprButton.addActionListener(e -> {
            List<Object[]> pprItems = fetchItemsByName("ppr");
            displayItems(pprItems);
        });
        pvcButton.addActionListener(e -> {
            List<Object[]> pprItems = fetchItemsByName("pvc");
            displayItems(pprItems);
        });
        peButton.addActionListener(e -> {
            List<Object[]> pprItems = fetchItemsByName("pe");
            displayItems(pprItems);
        });
        gsButton.addActionListener(
                e -> {
            List<Object[]> pprItems = fetchItemsByName("gs");
            displayItems(pprItems);
        });

        pvcButton.addActionListener(
                e -> {
            List<Object[]> pvcItems = fetchItemsByName("pe");
            displayItems(pvcItems);
        });

        staffaButton.addActionListener(e -> {
            List<Object[]> staffaItems = fetchItemsByName("stafa");
            displayItems(staffaItems);
        });

        cartButton.addActionListener(e -> displayCart());

        List<Object[]> allItems = fetchAllItems();

        displayItems(allItems);

        frame.setVisible(true);
    }

    public static void displayItems(List<Object[]> items) {
        panelContainer.removeAll();
        for (Object[] item : items) {

            Font font = new Font("Arial", Font.BOLD, 20); // Create a new Font object
            Font font2 = new Font("Arial",  Font.BOLD, 18); // Create a new Font object

            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
            itemPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            itemPanel.setPreferredSize(new Dimension(200, 150));
            itemPanel.setMaximumSize(new Dimension(200, 150));
            itemPanel.setMinimumSize(new Dimension(200, 150));

            JLabel nameLabel = new JLabel("" + item[1]);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameLabel.setFont(font);
            itemPanel.add(nameLabel);

            JLabel sizeLabel = new JLabel("" + item[2]);
            sizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sizeLabel.setFont(font2); // Make size label bold

            itemPanel.add(sizeLabel);

            JLabel priceLabel = new JLabel("$ " + item[3]);
            priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemPanel.add(priceLabel);

            JTextField quantityField = new JTextField(1);
            quantityField.setMaximumSize(new Dimension(150, 20));
            int[] quantity = {0};

            // Add action listener to the quantityField
            quantityField.addActionListener(e -> {
                addItem(item, quantityField);
                quantityField.setText(""); // Clear the input field after adding the item
            });
            itemPanel.add(quantityField);

            JButton btn = new JButton("Add");
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> {

                addItem(item, quantityField);
                quantityField.setText(""); // Clear the input field after adding the item
            });

            itemPanel.add(btn);

            panelContainer.add(itemPanel);
        }
        panelContainer.revalidate();
        panelContainer.repaint();
    }

    // Method to add item to cart
    private static void addItem(Object[] item, JTextField quantityField) {
        int newQuantity = Integer.parseInt(quantityField.getText());
        String id = item[0].toString();
        String itemName = item[1].toString();
        String size = item[2].toString();
        String price = item[3].toString();
        boolean itemExists = false;

        for (Object[] cartItem : cartItems) {
            if (cartItem[0].equals(id)) {
                cartItem[4] = (int) cartItem[4] + newQuantity; // Increment the quantity
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            Object[] itemData = new Object[6];
            itemData[0] = id;
            itemData[1] = itemName;
            itemData[2] = size;
            itemData[3] = price;
            itemData[4] = newQuantity;// Initial quantity
            itemData[5] = false;
            cartCount += 1;
            cartItems.add(itemData);
        }


        cartButton.setText("Cart: " + cartCount);
    }


    public static void displayCart() {
        JFrame cartFrame = new JFrame("Cart");
        cartFrame.setSize(1000, 800);
        cartFrame.setLayout(new BorderLayout());

        JPanel cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));

        JLabel totalLabel = new JLabel();

        double totalSum = 0;

        for (Object[] item : cartItems) {
            JPanel itemPanel = new JPanel(new GridLayout(1, 5,10,20));

            itemPanel.setPreferredSize(new Dimension(800, 50));
            itemPanel.setMaximumSize(new Dimension(800, 50));
            itemPanel.setMinimumSize(new Dimension(800, 50));
            String itemName = item[1].toString();
            String itemSize = item[2].toString();
            double itemPrice = Double.parseDouble(item[3].toString());
            int itemQuantity = Integer.parseInt(item[4].toString());
            double itemTotalSum = itemPrice * itemQuantity;

            totalSum += itemTotalSum;

            JLabel nameLabel = new JLabel(itemName);
            nameLabel.setFont(new Font("Arial",  Font.BOLD, 15));
            JLabel sizeLabel = new JLabel(itemSize);
            sizeLabel.setFont(new Font("Arial",  Font.BOLD, 20));
            JLabel priceLabel = new JLabel("$ " + itemPrice);
            JTextField quantityField = new JTextField(String.valueOf(itemQuantity));

            quantityField.setMaximumSize(new Dimension(50, 20));
            JLabel totalSumLabel = new JLabel("$" + itemTotalSum);

            quantityField.getDocument().addDocumentListener(new DocumentListener() {
                private void updateQuantity() {
                    try {
                        int newQuantity = Integer.parseInt(quantityField.getText());
                        item[4] = newQuantity;
                        double newItemTotalSum = itemPrice * newQuantity;
                        totalSumLabel.setText("Total: $" + newItemTotalSum);
                        updateTotalLabel(totalLabel);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid quantity. Please enter a number.");
                    }
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateQuantity();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateQuantity();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateQuantity();
                }
            });
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                cartItems.remove(item);
                cartCount--;
                updateCartButton();
                cartFrame.dispose(); // Close the current cart frame
                displayCart(); // Re-display cart
            });
            JTextField disscountF = new JTextField();
            disscountF.setMaximumSize(new Dimension(50, 20));

            JButton discountButton = new JButton("Discount");
            discountButton.addActionListener(e -> {
                double originalItemPrice = Double.parseDouble(item[3].toString()); // Use original price for calculations
                String disNum = disscountF.getText();
                applyDiscount(item, originalItemPrice, priceLabel, totalSumLabel, totalLabel,disNum);
                discountButton.setEnabled(false); // Disable the discount button after applying the discount
            });
            itemPanel.add(nameLabel);
            itemPanel.add(sizeLabel);
            itemPanel.add(priceLabel);
            itemPanel.add(quantityField);
            itemPanel.add(totalSumLabel);
            itemPanel.add(deleteButton);
            itemPanel.add(disscountF);
            itemPanel.add(discountButton);
            cartPanel.add(itemPanel);
        }

        totalLabel.setText("Total: $" + totalSum);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 100, 0));
        buttonPanel.setPreferredSize(new Dimension(50, 50));

        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(e -> {
            double totalSum1 = 0;
            for (Object[] item : cartItems) {
                totalSum1 += (Double.parseDouble(item[3].toString()) * Integer.parseInt(item[4].toString()));
            }
            totalLabel.setText("Total: $" + totalSum1);
            JOptionPane.showMessageDialog(null, "Order has been placed!");

            try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
                conn.setAutoCommit(false);

                long orderId = -1;
                try (PreparedStatement insertOrderStmt = conn.prepareStatement(INSERT_ORDER_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                    int total = (int) totalSum1;
                    insertOrderStmt.setDouble(1, total);
                    insertOrderStmt.executeUpdate();

                    try (ResultSet generatedKeys = insertOrderStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            orderId = generatedKeys.getLong(1);
                        } else {
                            throw new SQLException("Failed to retrieve the order ID.");
                        }
                    }
                }

                try (PreparedStatement insertOrderItemStmt = conn.prepareStatement(INSERT_ORDER_ITEM_QUERY);
                     PreparedStatement updateInventoryStmt = conn.prepareStatement(UPDATE_INVENTORY_QUERY)) {
                    for (Object[] item : cartItems) {
                        int itemId = Integer.parseInt(item[0].toString());
                        int quantity = Integer.parseInt(item[4].toString());

                        insertOrderItemStmt.setLong(1, orderId);
                        insertOrderItemStmt.setInt(2, itemId);
                        insertOrderItemStmt.setInt(3, quantity);
                        insertOrderItemStmt.addBatch();

                        updateInventoryStmt.setInt(1, quantity);
                        updateInventoryStmt.setInt(2, itemId);
                        updateInventoryStmt.addBatch();
                    }

                    insertOrderItemStmt.executeBatch();
                    updateInventoryStmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException ex) {
                System.err.println("Failed to process order: " + ex.getMessage());
                try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback transaction: " + rollbackEx.getMessage());
                }
            }

            orderCount++;
            orderCountLabel.setText("Orders placed: " + orderCount);
            cartItems.clear();
            cartCount = 0;
            cartFrame.dispose();

            writeItemsToExcel( cartItems ,"cart.xls");
        });

        JButton clearCartButton = new JButton("Clear Cart");
        clearCartButton.addActionListener(e -> {
            cartItems.clear();
            cartCount = 0;
            cartFrame.dispose();
            updateCartButton();

        });


        JButton creatExcel = new JButton("Creat Excel");
        creatExcel.addActionListener(e->{
            writeItemsToExcel( cartItems ,"cart.xls");
        });
        buttonPanel.add(placeOrderButton);
        buttonPanel.add(creatExcel);
        buttonPanel.add(clearCartButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        cartFrame.add(new JScrollPane(cartPanel), BorderLayout.CENTER);
        cartFrame.add(bottomPanel, BorderLayout.SOUTH);

        cartFrame.setVisible(true);

    }

    private static void updateTotalLabel(JLabel totalLabel) {
        double totalSum = 0;
        for (Object[] item : cartItems) {
            double itemPrice = Double.parseDouble(item[3].toString());
            int itemQuantity = Integer.parseInt(item[4].toString());
            totalSum += itemPrice * itemQuantity;
        }
        totalLabel.setText("Total: $" + totalSum);
    }
    private static void applyDiscount(Object[] item, double itemPrice, JLabel priceLabel, JLabel totalSumLabel, JLabel totalLabel,String disnum) {
        boolean discountApplied = (boolean) item[5]; // Check if discount is already applied
        System.out.println(disnum);
        if (discountApplied) {
            return; // If discount is already applied, do nothing
        }
        int discount = Integer.parseInt(disnum);
        double discountedPrice = itemPrice - discount;
        String discountedPriceString = String.valueOf(discountedPrice); // Convert discounted price to String
        item[3] = discountedPriceString; // Update the item price in cartItems as String
        int itemQuantity = Integer.parseInt(item[4].toString());
        double newItemTotalSum = discountedPrice * itemQuantity;
        priceLabel.setText("$" + discountedPriceString); // Update price label with discounted price
        totalSumLabel.setText("$ " + newItemTotalSum); // Update total sum label with new item total sum
        updateTotalLabel(totalLabel);
        item[5] = true; // Mark discount as applied

    }




    public static void writeItemsToExcel(List<Object[]> cartItems, String filePath) {
        try {
            // Create a new Excel workbook
            WritableWorkbook workbook = Workbook.createWorkbook(new File(filePath));


            // Create a new sheet in the workbook
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);
            sheet.setColumnView(0,30);
            sheet.setColumnView(3,10);
            // Write headers
            sheet.addCell(new Label(0, 0, "Item Name"));
            sheet.addCell(new Label(1, 0, "Size"));
            sheet.addCell(new Label(2, 0, "Price"));
            sheet.addCell(new Label(3, 0, "Quantity"));
            sheet.addCell(new Label(4, 0, "Sum"));

            // Write data from cartItems to the sheet
            int row = 1;
            double totalSum = 0; // Variable to store the total sum of all item sums
            for (Object[] item : cartItems) {
                double quantity = Double.parseDouble((String) item[3]);
                double price = ((Integer) item[4]).doubleValue();
                double sum = quantity * price; // Quantity * Price
                totalSum += sum; // Add the current item sum to the total sum

                for (int col = 1; col < item.length; col++) {
                    if (col == 5) { // If it's the 'Sum' column
                        sheet.addCell(new Number(col-1, row, sum));
                    } else {
                        sheet.addCell(new Label(col -1, row, String.valueOf(item[col])));
                    }
                }
                row++;
            }

            // Write the total sum at the end of the list
            sheet.addCell(new Label(3, row, "Total Sum:"));
            sheet.addCell(new Number(4, row, totalSum));

            // Write and close the workbook
            workbook.write();
            workbook.close();
            JOptionPane.showMessageDialog(null,"Created succ");
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }


    private static void updateCartButton() {
        cartButton.setText("Cart: " + cartCount);
    }

}
