package com.pcsale.gui;

import com.pcsale.dao.SaleDAO;
import com.pcsale.util.Formatter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * ReportPanel - Sales reports and analytics
 */
public class ReportPanel extends JPanel {
    
    private SaleDAO saleDAO;
    
    public ReportPanel() {
        saleDAO = new SaleDAO();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitle = new JLabel("Sales Reports");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBackground(Color.WHITE);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        summaryPanel.setBackground(new Color(245, 245, 245));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Quick Summary"));
        
        double todayTotal = saleDAO.getTodaySalesTotal();
        summaryPanel.add(createStatLabel("Today's Sales:", Formatter.formatCurrency(todayTotal)));
        
        mainPanel.add(summaryPanel);
        
        // Chart Panel
        List<Object[]> salesData = saleDAO.getDailySalesData(30);
        SalesChartPanel chart = new SalesChartPanel("Last 30 Days Sales", salesData, "Date", "Amount", SalesChartPanel.ChartType.LINE);
        mainPanel.add(chart);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createStatLabel(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 245, 245));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Arial", Font.BOLD, 20));
        v.setForeground(new Color(46, 213, 115));
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }
}
