package com.pcsale.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SalesChartPanel extends JPanel {

    private String chartTitle;
    private List<Object[]> data;
    private String xAxisLabel;
    private String yAxisLabel;
    private ChartType chartType;
    private Color chartColor;

    // Interactive state
    private int hoveredIndex = -1;
    private Point mousePosition = null;

    // Formatting
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
    private final Font titleFont = new Font("Segoe UI", Font.BOLD, 18);
    private final Font axisFont = new Font("Segoe UI", Font.BOLD, 12);
    private final Font labelFont = new Font("Segoe UI", Font.PLAIN, 11);
    private final Font tooltipFont = new Font("Segoe UI", Font.PLAIN, 12);

    public enum ChartType {
        BAR, LINE
    }

    public SalesChartPanel(String title, List<Object[]> data, String xLabel, String yLabel, ChartType type) {
        this.chartTitle = title;
        this.data = data;
        this.xAxisLabel = xLabel;
        this.yAxisLabel = yLabel;
        this.chartType = type;
        this.chartColor = new Color(52, 152, 219); // Modern Blue

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        // Add Mouse Listener for Hover Effects
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                calculateHoverIndex(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                mousePosition = null;
                repaint();
            }
        };
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
    }

    public void setChartColor(Color color) {
        this.chartColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // High Quality Rendering settings
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (data == null || data.isEmpty()) {
            drawNoData(g2d);
            return;
        }

        // Layout Constants
        int padding = 50;
        int topPadding = 60;
        int bottomPadding = 50;
        int width = getWidth();
        int height = getHeight();
        int chartW = width - (2 * padding);
        int chartH = height - topPadding - bottomPadding;

        // 1. Draw Title
        drawTitle(g2d, width);

        // 2. Calculate Max Value & Scale
        double rawMax = 0;
        for (Object[] row : data) {
            double val = ((Number) row[2]).doubleValue();
            if (val > rawMax) rawMax = val;
        }
        // Add 10% headroom so bars don't touch the top
        double maxScale = (rawMax == 0) ? 10 : rawMax * 1.1;

        // 3. Draw Grid and Y-Axis Labels
        drawGridAndYAxis(g2d, padding, topPadding, chartW, chartH, maxScale);

        // 4. Draw Chart Content
        if (chartType == ChartType.BAR) {
            drawBarChart(g2d, padding, topPadding, chartW, chartH, maxScale);
        } else {
            drawLineChart(g2d, padding, topPadding, chartW, chartH, maxScale);
        }

        // 5. Draw Axes Lines
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(padding, topPadding, padding, topPadding + chartH); // Y Axis
        g2d.drawLine(padding, topPadding + chartH, padding + chartW, topPadding + chartH); // X Axis

        // 6. Draw X/Y Label Text
        drawAxisLabels(g2d, padding, topPadding, chartW, chartH);

        // 7. Draw Tooltip if hovering
        if (hoveredIndex != -1 && mousePosition != null) {
            drawTooltip(g2d, hoveredIndex);
        }
    }

    private void drawTitle(Graphics2D g2d, int width) {
        g2d.setColor(new Color(44, 62, 80));
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(chartTitle, (width - fm.stringWidth(chartTitle)) / 2, 35);
    }

    private void drawGridAndYAxis(Graphics2D g2d, int x, int y, int w, int h, double max) {
        int steps = 5;
        g2d.setFont(labelFont);

        for (int i = 0; i <= steps; i++) {
            int yPos = y + h - (int) ((i * 1.0 / steps) * h);
            double val = (max / steps) * i;

            // Draw grid line
            if (i > 0) { // Don't draw over X axis
                g2d.setColor(new Color(240, 240, 240));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine(x, yPos, x + w, yPos);
            }

            // Draw Text
            g2d.setColor(Color.GRAY);
            String label = String.format("%.0f", val);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(label, x - fm.stringWidth(label) - 8, yPos + 5);
        }
    }

    private void drawAxisLabels(Graphics2D g2d, int x, int y, int w, int h) {
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(axisFont);

        // X Label
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(xAxisLabel, x + (w - fm.stringWidth(xAxisLabel)) / 2, y + h + 40);

        // Y Label (Rotated)
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisLabel, -(y + h / 2 + fm.stringWidth(yAxisLabel) / 2), x - 30);
        g2d.rotate(Math.PI / 2);
    }

    private void drawBarChart(Graphics2D g2d, int xStart, int yStart, int w, int h, double max) {
        int count = data.size();
        int barWidth = (w / count) / 2;
        int spacing = w / count;

        for (int i = 0; i < count; i++) {
            double val = ((Number) data.get(i)[2]).doubleValue();
            int barHeight = (int) ((val / max) * h);
            int xPos = xStart + (i * spacing) + (spacing - barWidth) / 2;
            int yPos = yStart + h - barHeight;

            // Highlight if hovered
            if (i == hoveredIndex) {
                g2d.setColor(chartColor.brighter());
            } else {
                g2d.setColor(chartColor);
            }

            // Draw Rounded Bar
            g2d.fillRoundRect(xPos, yPos, barWidth, barHeight, 10, 10);

            // Draw Date Label
            drawXDateLabel(g2d, i, xPos + barWidth/2, yStart + h + 20);
        }
    }

    private void drawLineChart(Graphics2D g2d, int xStart, int yStart, int w, int h, double max) {
        if (data.size() < 2) return;

        int spacing = w / (data.size() - 1);
        int[] xPoints = new int[data.size()];
        int[] yPoints = new int[data.size()];

        // Calculate points
        for (int i = 0; i < data.size(); i++) {
            double val = ((Number) data.get(i)[2]).doubleValue();
            xPoints[i] = xStart + (i * spacing);
            yPoints[i] = yStart + h - (int) ((val / max) * h);
        }

        // Draw Line
        g2d.setColor(chartColor);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawPolyline(xPoints, yPoints, data.size());

        // Draw Points & Hover effect
        for (int i = 0; i < data.size(); i++) {
            int radius = (i == hoveredIndex) ? 12 : 8; // Make bigger if hovered
            int offset = radius / 2;

            g2d.setColor(Color.WHITE);
            g2d.fillOval(xPoints[i] - offset, yPoints[i] - offset, radius, radius);

            g2d.setColor(chartColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(xPoints[i] - offset, yPoints[i] - offset, radius, radius);

            drawXDateLabel(g2d, i, xPoints[i], yStart + h + 20);
        }
    }

    private void drawXDateLabel(Graphics2D g2d, int index, int xCenter, int yPos) {
        // Skip some labels if too many data points to avoid overlapping
        if (data.size() > 10 && index % 2 != 0) return;

        Object dateObj = data.get(index)[0];
        String dateStr = (dateObj instanceof Date) ? dateFormat.format((Date) dateObj) : dateObj.toString();

        g2d.setColor(Color.GRAY);
        g2d.setFont(labelFont);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(dateStr, xCenter - fm.stringWidth(dateStr) / 2, yPos);
    }

    private void drawTooltip(Graphics2D g2d, int index) {
        Object[] row = data.get(index);
        String date = (row[0] instanceof Date) ? dateFormat.format((Date) row[0]) : row[0].toString();
        String value = row[2].toString();
        String text = date + ": " + value;

        // Calculate Box Size
        g2d.setFont(tooltipFont);
        FontMetrics fm = g2d.getFontMetrics();
        int boxW = fm.stringWidth(text) + 20;
        int boxH = 25;

        int x = mousePosition.x + 10;
        int y = mousePosition.y - 30;

        // Prevent tooltip from going off screen
        if (x + boxW > getWidth()) x = mousePosition.x - boxW - 10;
        if (y < 0) y = mousePosition.y + 20;

        // Draw Shadow
        g2d.setColor(new Color(0,0,0,50));
        g2d.fillRoundRect(x+2, y+2, boxW, boxH, 10, 10);

        // Draw Box
        g2d.setColor(new Color(255, 255, 225)); // Light yellow tooltip style
        g2d.fillRoundRect(x, y, boxW, boxH, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x, y, boxW, boxH, 10, 10);

        // Draw Text
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, x + 10, y + 17);
    }

    // Logic to find which bar/point is closest to mouse
    private void calculateHoverIndex(int mouseX, int mouseY) {
        if (data == null || data.isEmpty()) return;

        int padding = 50;
        int width = getWidth();
        int chartW = width - (2 * padding);
        int oldIndex = hoveredIndex;

        if (chartType == ChartType.BAR) {
            int count = data.size();
            int spacing = chartW / count;

            // Check which "column" the mouse is in
            int relativeX = mouseX - padding;
            if (relativeX >= 0 && relativeX <= chartW) {
                hoveredIndex = relativeX / spacing;
                if (hoveredIndex >= count) hoveredIndex = -1;
            } else {
                hoveredIndex = -1;
            }
        } else {
            // For Line chart, find closest point X
            int count = data.size();
            if (count < 2) return;

            int spacing = chartW / (count - 1);
            int bestIndex = -1;
            int minDist = Integer.MAX_VALUE;

            for (int i = 0; i < count; i++) {
                int px = padding + (i * spacing);
                int dist = Math.abs(mouseX - px);
                if (dist < minDist) {
                    minDist = dist;
                    bestIndex = i;
                }
            }
            // Only highlight if close enough (e.g., within 20px)
            if (minDist < 30) {
                hoveredIndex = bestIndex;
            } else {
                hoveredIndex = -1;
            }
        }
    }

    private void drawNoData(Graphics2D g) {
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(titleFont);
        String msg = "No Data Available";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}