package com.pcsale.util;

import com.pcsale.model.Sale;
import com.pcsale.model.SaleItem;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;

/**
 * ReceiptGenerator - Utility to generate and export receipts
 */
public class ReceiptGenerator {

    private static final String STORE_NAME = "4 Null TECH";
    private static final String STORE_ADDRESS = "2004 Street, Siem Reap City";
    private static final String STORE_PHONE = "(+855)  088 65 404 83";

    public static String generateTextReceipt(Sale sale) {
        StringBuilder sb = new StringBuilder();
        String line = "------------------------------------------\n";
        
        sb.append(centerText(STORE_NAME, 42)).append("\n");
        sb.append(centerText(STORE_ADDRESS, 42)).append("\n");
        sb.append(centerText(STORE_PHONE, 42)).append("\n");
        sb.append(line);
        
        sb.append(String.format("Invoice No: %-20s\n", sale.getInvoiceNo()));
        
        // Format LocalDateTime to String for display
        String dateStr = "";
        if (sale.getSaleDate() != null) {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
            dateStr = sale.getSaleDate().format(dtf);
        }
        
        sb.append(String.format("Date:       %-20s\n", dateStr));
        sb.append(String.format("Cashier:    %-20s\n", sale.getUserName() != null ? sale.getUserName() : SessionManager.getCurrentUser().getFullName()));
        sb.append(line);
        
        sb.append(String.format("%-20s %-5s %-6s %-8s\n", "Item", "Qty", "Price", "Total"));
        sb.append(line);
        
        for (SaleItem item : sale.getItems()) {
            String name = item.getProductName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            sb.append(String.format("%-20s %-5d %-6s %-8s\n", 
                name, 
                item.getQuantity(), 
                Formatter.formatCurrency(item.getUnitPrice().doubleValue()),
                Formatter.formatCurrency(item.getSubtotal().doubleValue())));
        }
        
        sb.append(line);
        sb.append(String.format("%-30s %10s\n", "Subtotal:", Formatter.formatCurrency(sale.getSubtotal().doubleValue())));
        sb.append(String.format("%-30s %10s\n", "Tax:", Formatter.formatCurrency(sale.getTax().doubleValue())));
        sb.append(String.format("%-30s %10s\n", "Discount:", Formatter.formatCurrency(sale.getDiscount().doubleValue())));
        sb.append(line);
        sb.append(String.format("%-30s %10s\n", "TOTAL:", Formatter.formatCurrency(sale.getTotalAmount().doubleValue())));
        sb.append(String.format("%-30s %10s\n", "Amount Paid:", Formatter.formatCurrency(sale.getAmountPaid().doubleValue())));
        sb.append(String.format("%-30s %10s\n", "Change:", Formatter.formatCurrency(sale.getChangeDue().doubleValue())));
        sb.append(line);
        sb.append(String.format("Payment Method: %s\n", sale.getPaymentMethod().toUpperCase()));
        sb.append(line);
        sb.append(centerText("THANK YOU FOR YOUR SUPPORT!", 42)).append("\n");
        sb.append(centerText("Please come again!", 42)).append("\n");
        
        return sb.toString();
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) sb.append(" ");
        sb.append(text);
        return sb.toString();
    }

    public static void exportToTextFile(Component parent, Sale sale) {
        String[] options = {"Text File (.txt)", "HTML File (.html)"};
        int choice = JOptionPane.showOptionDialog(parent, 
            "Select export format:", 
            "Export Receipt", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]);

        if (choice == 0) {
            saveTextReceipt(parent, sale);
        } else if (choice == 1) {
            saveHtmlReceipt(parent, sale);
        }
    }

    private static void saveTextReceipt(Component parent, Sale sale) {
        String receiptContent = generateTextReceipt(sale);
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Text Receipt");
        fileChooser.setSelectedFile(new File("Receipt_" + sale.getInvoiceNo() + ".txt"));
        
        int userSelection = fileChooser.showSaveDialog(parent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter out = new PrintWriter(new FileWriter(fileToSave))) {
                out.println(receiptContent);
                JOptionPane.showMessageDialog(parent, "Receipt saved successfully to " + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Error saving receipt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void saveHtmlReceipt(Component parent, Sale sale) {
        String htmlContent = generateHtmlReceipt(sale);
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save HTML Receipt");
        fileChooser.setSelectedFile(new File("Receipt_" + sale.getInvoiceNo() + ".html"));
        
        int userSelection = fileChooser.showSaveDialog(parent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter out = new PrintWriter(new FileWriter(fileToSave))) {
                out.println(htmlContent);
                JOptionPane.showMessageDialog(parent, "Receipt saved successfully to " + fileToSave.getAbsolutePath() + "\nYou can open this file in any browser and print it to PDF.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Error saving receipt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static String generateHtmlReceipt(Sale sale) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>");
        sb.append("body { font-family: 'Courier New', Courier, monospace; width: 300px; margin: 20px auto; border: 1px solid #ccc; padding: 15px; }");
        sb.append(".center { text-align: center; }");
        sb.append(".bold { font-weight: bold; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin: 10px 0; }");
        sb.append("th { border-bottom: 1px dashed #000; text-align: left; }");
        sb.append(".right { text-align: right; }");
        sb.append(".total-row { border-top: 1px solid #000; }");
        sb.append("</style></head><body>");
        
        sb.append("<div class='center bold'>").append(STORE_NAME).append("</div>");
        sb.append("<div class='center'>").append(STORE_ADDRESS).append("</div>");
        sb.append("<div class='center'>").append(STORE_PHONE).append("</div>");
        sb.append("<hr style='border-top: 1px dashed #000;'>");
        
        sb.append("<div><span class='bold'>Invoice No:</span> ").append(sale.getInvoiceNo()).append("</div>");
        
        String dateStr = "";
        if (sale.getSaleDate() != null) {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
            dateStr = sale.getSaleDate().format(dtf);
        }
        
        sb.append("<div><span class='bold'>Date:</span> ").append(dateStr).append("</div>");
        sb.append("<div><span class='bold'>Cashier:</span> ").append(sale.getUserName() != null ? sale.getUserName() : SessionManager.getCurrentUser().getFullName()).append("</div>");
        
        sb.append("<table>");
        sb.append("<tr><th>Item</th><th>Qty</th><th class='right'>Total</th></tr>");
        
        for (SaleItem item : sale.getItems()) {
            sb.append("<tr>");
            sb.append("<td>").append(item.getProductName()).append("</td>");
            sb.append("<td>").append(item.getQuantity()).append("</td>");
            sb.append("<td class='right'>").append(Formatter.formatCurrency(item.getSubtotal().doubleValue())).append("</td>");
            sb.append("</tr>");
        }
        
        sb.append("<tr class='total-row'><td colspan='2' class='bold'>Subtotal:</td><td class='right'>").append(Formatter.formatCurrency(sale.getSubtotal().doubleValue())).append("</td></tr>");
        sb.append("<tr><td colspan='2'>Tax:</td><td class='right'>").append(Formatter.formatCurrency(sale.getTax().doubleValue())).append("</td></tr>");
        sb.append("<tr><td colspan='2'>Discount:</td><td class='right'>").append(Formatter.formatCurrency(sale.getDiscount().doubleValue())).append("</td></tr>");
        sb.append("<tr class='bold'><td colspan='2' style='font-size: 1.2em;'>TOTAL:</td><td class='right' style='font-size: 1.2em;'>").append(Formatter.formatCurrency(sale.getTotalAmount().doubleValue())).append("</td></tr>");
        sb.append("<tr><td colspan='2'>Amount Paid:</td><td class='right'>").append(Formatter.formatCurrency(sale.getAmountPaid().doubleValue())).append("</td></tr>");
        sb.append("<tr><td colspan='2'>Change:</td><td class='right'>").append(Formatter.formatCurrency(sale.getChangeDue().doubleValue())).append("</td></tr>");
        sb.append("</table>");
        
        sb.append("<div class='bold'>Payment Method: ").append(sale.getPaymentMethod().toUpperCase()).append("</div>");
        sb.append("<hr style='border-top: 1px dashed #000;'>");
        sb.append("<div class='center bold'>THANK YOU FOR YOUR BUSINESS!</div>");
        sb.append("<div class='center'>Please come again!</div>");
        
        sb.append("</body></html>");
        return sb.toString();
    }
}
