package com.pcsale.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * SalesChartPanel - Custom panel for displaying sales charts
 */
public class SalesChartPanel extends JPanel {
    
    private String chartTitle;
    private List<Object[]> data;
    private String xAxisLabel;
    private String yAxisLabel;
    private ChartType chartType;
    private Color chartColor;
    
    public enum ChartType {
        BAR, LINE
    }
    
    public SalesChartPanel(String title, List<Object[]> data, String xLabel, String yLabel, ChartType type) {
        this.chartTitle = title;
        this.data = data;
        this.xAxisLabel = xLabel;
        this.yAxisLabel = yLabel;
        this.chartType = type;
        this.chartColor = new Color(52, 152, 219);
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
    }
    
    public void setChartColor(Color color) {
        this.chartColor = color;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (data == null || data.isEmpty()) {
            drawNoData(g);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate margins
        int margin = 50;
        int topMargin = 60;
        int rightMargin = 40;
        int bottomMargin = 80;
        
        int width = getWidth();
        int height = getHeight();
        int chartWidth = width - margin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;
        
        // Draw title
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(chartTitle);
        g2d.drawString(chartTitle, (width - titleWidth) / 2, 30);
        
        // Draw axes
        g2d.setColor(new Color(52, 73, 94));
        g2d.setStroke(new BasicStroke(2));
        
        // Y-axis
        g2d.drawLine(margin, topMargin, margin, height - bottomMargin);
        // X-axis
        g2d.drawLine(margin, height - bottomMargin, width - rightMargin, height - bottomMargin);
        
        // Find max value for scaling
        double maxValue = 0;
        for (Object[] row : data) {
            double value = ((Number) row[2]).doubleValue(); // Assuming total is at index 2
            if (value > maxValue) {
                maxValue = value;
            }
        }
        
        if (maxValue == 0) {
            drawNoData(g);
            return;
        }
        
        // Draw Y-axis labels and grid lines
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        int ySteps = 5;
        double yInterval = maxValue / ySteps;
        
        for (int i = 0; i <= ySteps; i++) {
            double value = yInterval * i;
            int y = height - bottomMargin - (int) ((value / maxValue) * chartHeight);
            
            // Grid line
            g2d.setColor(new Color(220, 220, 220));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0));
            g2d.drawLine(margin, y, width - rightMargin, y);
            
            // Label
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(1));
            String label = String.format("%.0f", value);
            g2d.drawString(label, margin - 35, y + 5);
        }
        
        // Draw Y-axis label
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisLabel, -(height + yAxisLabel.length() * 6) / 2, 20);
        g2d.rotate(Math.PI / 2);
        
        // Draw chart based on type
        if (chartType == ChartType.BAR) {
            drawBarChart(g2d, margin, topMargin, chartWidth, chartHeight, maxValue);
        } else {
            drawLineChart(g2d, margin, topMargin, chartWidth, chartHeight, maxValue);
        }
        
        // Draw X-axis label
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int xLabelWidth = g2d.getFontMetrics().stringWidth(xAxisLabel);
        g2d.drawString(xAxisLabel, margin + (chartWidth - xLabelWidth) / 2, height - 20);
    }
    
    private void drawBarChart(Graphics2D g2d, int margin, int topMargin, int chartWidth, int chartHeight, double maxValue) {
        int barWidth = Math.max(15, (chartWidth - (data.size() * 10)) / data.size());
        int spacing = Math.max(5, (chartWidth - (barWidth * data.size())) / (data.size() + 1));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            double value = ((Number) row[2]).doubleValue();
            int barHeight = (int) ((value / maxValue) * chartHeight);
            
            int x = margin + spacing + (i * (barWidth + spacing));
            int y = getHeight() - topMargin - chartHeight + (chartHeight - barHeight);
            
            // Draw bar with gradient
            GradientPaint gradient = new GradientPaint(
                x, y, chartColor.brighter(),
                x, y + barHeight, chartColor
            );
            g2d.setPaint(gradient);
            g2d.fillRect(x, y, barWidth, barHeight);
            
            // Draw bar border
            g2d.setColor(chartColor.darker());
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(x, y, barWidth, barHeight);
            
            // Draw X-axis label
            g2d.setColor(new Color(100, 100, 100));
            String label;
            if (row[0] instanceof Date) {
                label = dateFormat.format((Date) row[0]);
            } else {
                label = row[0].toString();
            }
            
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            
            // Rotate label if needed
            if (data.size() > 10) {
                g2d.rotate(-Math.PI / 4, x + barWidth / 2, getHeight() - topMargin + 10);
                g2d.drawString(label, x + barWidth / 2, getHeight() - topMargin + 10);
                g2d.rotate(Math.PI / 4, x + barWidth / 2, getHeight() - topMargin + 10);
            } else {
                g2d.drawString(label, x + (barWidth - labelWidth) / 2, getHeight() - topMargin + 20);
            }
            
            // Draw value on top of bar
            if (barHeight > 20) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                String valueStr = String.format("%.0f", value);
                int valueWidth = g2d.getFontMetrics().stringWidth(valueStr);
                g2d.drawString(valueStr, x + (barWidth - valueWidth) / 2, y + 15);
            }
        }
    }
    
    private void drawLineChart(Graphics2D g2d, int margin, int topMargin, int chartWidth, int chartHeight, double maxValue) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        
        int pointSpacing = chartWidth / (data.size() > 1 ? data.size() - 1 : 1);
        
        // Draw line
        g2d.setColor(chartColor);
        g2d.setStroke(new BasicStroke(3));
        
        int[] xPoints = new int[data.size()];
        int[] yPoints = new int[data.size()];
        
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            double value = ((Number) row[2]).doubleValue();
            
            int x = margin + (i * pointSpacing);
            int y = getHeight() - topMargin - chartHeight + (chartHeight - (int) ((value / maxValue) * chartHeight));
            
            xPoints[i] = x;
            yPoints[i] = y;
        }
        
        // Draw line segments
        for (int i = 0; i < data.size() - 1; i++) {
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }
        
        // Draw points and labels
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            double value = ((Number) row[2]).doubleValue();
            
            // Draw point
            g2d.setColor(chartColor.darker());
            g2d.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
            
            // Draw X-axis label
            g2d.setColor(new Color(100, 100, 100));
            String label;
            if (row[0] instanceof Date) {
                label = dateFormat.format((Date) row[0]);
            } else {
                label = row[0].toString();
            }
            
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, xPoints[i] - labelWidth / 2, getHeight() - topMargin + 20);
            
            // Draw value above point
            g2d.setColor(chartColor.darker());
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String valueStr = String.format("%.0f", value);
            int valueWidth = g2d.getFontMetrics().stringWidth(valueStr);
            g2d.drawString(valueStr, xPoints[i] - valueWidth / 2, yPoints[i] - 10);
        }
    }
    
    private void drawNoData(Graphics g) {
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        String message = "No data available";
        FontMetrics fm = g.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        g.drawString(message, (getWidth() - messageWidth) / 2, getHeight() / 2);
    }
}
