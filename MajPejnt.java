import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class MajPejnt extends JFrame {

    enum Tool {PENCIL, RECT, ELLIPSE, LINE, POLYGON}

    static class ShapeRecord {
        Shape shape;
        Color color;
        Stroke stroke;
        boolean filled;
        ShapeRecord(Shape s, Color c, Stroke st, boolean f){ shape = s; color = c; stroke = st; filled = f; }
    }

    private final DrawPanel jPanel2;
    private final JComboBox<String> jComboBox1;
    private boolean draw_figure = false;
    private int first_mouse_x = 0, first_mouse_y = 0;
    private Color brushColor = Color.BLACK;
    private int currentStroke = 1;

    public MajPejnt(){
        super("MajPejnt");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000,700);
        setLocationRelativeTo(null);

        jPanel2 = new DrawPanel();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        top.add(new JLabel("KOLOR"));
        JPanel colorSwatch = new JPanel();
        colorSwatch.setPreferredSize(new Dimension(24,24));
        colorSwatch.setBackground(brushColor);
        colorSwatch.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        colorSwatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        colorSwatch.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                Color c = JColorChooser.showDialog(MajPejnt.this,"Wybierz kolor",brushColor);
                if(c!=null){ brushColor = c; colorSwatch.setBackground(c); jPanel2.repaint(); }
            }
        });
        top.add(colorSwatch);

        top.add(new JLabel("Narzędzie"));
        jComboBox1 = new JComboBox<>(new String[]{"ołówek","prostokąt","koło","linia","wielokąt"});
        jComboBox1.setSelectedIndex(1);
        top.add(jComboBox1);

        top.add(new JLabel("Grubość"));
        JComboBox<String> cbStroke = new JComboBox<>(new String[]{"1px","2px","3px","4px","5px"});
        cbStroke.setSelectedIndex(0);
        cbStroke.addActionListener(e -> {
            String s = (String)cbStroke.getSelectedItem();
            if(s!=null && s.endsWith("px")){
                try{ currentStroke = Integer.parseInt(s.replace("px","")); } catch(Exception ignored){}
            }
        });
        top.add(cbStroke);

        JButton clear = new JButton("Wyczyść");
        clear.addActionListener(e -> jPanel2.clearAll());
        top.add(clear);

        JButton save = new JButton("Zapisz");
        save.addActionListener(e -> jPanel2.saveToPNG());
        top.add(save);

        add(top, BorderLayout.NORTH);
        add(jPanel2, BorderLayout.CENTER);
    }

    private class DrawPanel extends JPanel {
        private final java.util.List<ShapeRecord> shapes = new ArrayList<>();
        private BufferedImage backgroundImage = null;
        private Path2D currentPath = null;
        private final java.util.List<Point> polygonPoints = new ArrayList<>();
        private final int POLY_CLOSE_DIST = 8;
        private int currentMouseX = 0, currentMouseY = 0;

        DrawPanel(){
            setBackground(Color.WHITE);
            MouseAdapter ma = new MouseAdapter(){
                @Override public void mouseClicked(MouseEvent e){
                    if(!SwingUtilities.isLeftMouseButton(e)) return;
                    int tool = jComboBox1.getSelectedIndex();
                    if(tool == 4){ handlePolygonClick(e.getPoint()); return; }
                }
                @Override public void mousePressed(MouseEvent e){
                    if(!SwingUtilities.isLeftMouseButton(e)) return;
                    int tool = jComboBox1.getSelectedIndex();
                    if(tool != 4){
                        draw_figure = true;
                        first_mouse_x = e.getX();
                        first_mouse_y = e.getY();
                        currentMouseX = first_mouse_x;
                        currentMouseY = first_mouse_y;
                        if(tool == 0){ currentPath = new Path2D.Double(); currentPath.moveTo(first_mouse_x, first_mouse_y); }
                    }
                }
                @Override public void mouseReleased(MouseEvent e){
                    if(!SwingUtilities.isLeftMouseButton(e)) return;
                    int tool = jComboBox1.getSelectedIndex();
                    if(draw_figure && tool != 4){
                        finalizeShape(e.getPoint());
                        repaint();
                    }
                }
                @Override public void mouseDragged(MouseEvent e){
                    updateMouseForPreview(e.getX(), e.getY());
                    if(draw_figure && jComboBox1.getSelectedIndex() == 0 && currentPath != null){
                        currentPath.lineTo(e.getX(), e.getY());
                    }
                    repaint();
                }
                @Override public void mouseMoved(MouseEvent e){
                    updateMouseForPreview(e.getX(), e.getY());
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);

            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancel");
            getActionMap().put("cancel", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e){
                draw_figure = false; polygonPoints.clear(); currentPath = null; repaint();
            }});
        }

        private void updateMouseForPreview(int mx, int my){
            currentMouseX = mx;
            currentMouseY = my;
        }

        private void finalizeShape(Point end){
            if(first_mouse_x == end.x && first_mouse_y == end.y) { resetDrawing(); return; }
            Shape s = null;
            int tool = jComboBox1.getSelectedIndex();
            boolean filled = false;
            switch(tool){
                case 1 -> {
                    int x = Math.min(first_mouse_x, end.x);
                    int y = Math.min(first_mouse_y, end.y);
                    int w = Math.abs(first_mouse_x - end.x);
                    int h = Math.abs(first_mouse_y - end.y);
                    s = new Rectangle2D.Double(x,y,w,h);
                    filled = true;
                }
                case 2 -> {
                    int x = Math.min(first_mouse_x, end.x);
                    int y = Math.min(first_mouse_y, end.y);
                    int w = Math.abs(first_mouse_x - end.x);
                    int h = Math.abs(first_mouse_y - end.y);
                    s = new Ellipse2D.Double(x,y,w,h);
                    filled = true;
                }
                case 3 -> s = new Line2D.Double(first_mouse_x, first_mouse_y, end.x, end.y);
                case 0 -> { if(currentPath != null) s = (Shape) currentPath.clone(); }
            }
            if(s != null){
                Stroke st = new BasicStroke(currentStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                shapes.add(new ShapeRecord(s, brushColor, st, filled));
            }
            resetDrawing();
        }

        private void handlePolygonClick(Point p){
            if(polygonPoints.isEmpty()){
                polygonPoints.add(p);
                draw_figure = true;
            } else {
                Point first = polygonPoints.get(0);
                double dx = p.x - first.x; double dy = p.y - first.y;
                double dist2 = dx*dx + dy*dy;
                if(dist2 <= POLY_CLOSE_DIST * POLY_CLOSE_DIST && polygonPoints.size() >= 3){
                    Path2D poly = new Path2D.Double();
                    Point fp = polygonPoints.get(0);
                    poly.moveTo(fp.x, fp.y);
                    for(int i=1;i<polygonPoints.size();i++){ Point q = polygonPoints.get(i); poly.lineTo(q.x,q.y); }
                    poly.closePath();
                    shapes.add(new ShapeRecord(poly, brushColor, new BasicStroke(currentStroke), true));
                    polygonPoints.clear();
                    resetDrawing();
                    repaint();
                    return;
                } else {
                    polygonPoints.add(p);
                }
            }
            repaint();
        }

        private void resetDrawing(){
            draw_figure = false;
            currentPath = null;
            first_mouse_x = first_mouse_y = 0;
        }

        void clearAll(){ shapes.clear(); polygonPoints.clear(); resetDrawing(); repaint(); }

        void saveToPNG(){
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("PNG Image","png"));
            if(fc.showSaveDialog(MajPejnt.this) != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();
            if(!f.getName().toLowerCase().endsWith(".png")) f = new File(f.getParentFile(), f.getName()+".png");
            try{
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setColor(Color.WHITE); g2.fillRect(0,0,getWidth(),getHeight());
                if(backgroundImage != null) g2.drawImage(backgroundImage,0,0,null);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for(ShapeRecord sr : shapes){
                    g2.setStroke(sr.stroke);
                    if(sr.filled){ g2.setColor(sr.color); g2.fill(sr.shape); }
                    g2.setColor(sr.color); g2.draw(sr.shape);
                }
                g2.dispose();
                ImageIO.write(img, "PNG", f);
                JOptionPane.showMessageDialog(MajPejnt.this, "Zapisano: "+f.getAbsolutePath());
            } catch(Exception ex){ JOptionPane.showMessageDialog(MajPejnt.this, "Błąd zapisu: "+ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE); }
        }

        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(backgroundImage != null) g2.drawImage(backgroundImage,0,0,this);

            for(ShapeRecord sr : shapes){
                g2.setStroke(sr.stroke);
                if(sr.filled){ g2.setColor(sr.color); g2.fill(sr.shape); }
                g2.setColor(sr.color);
                g2.draw(sr.shape);
            }

            if(!polygonPoints.isEmpty()){
                g2.setStroke(new BasicStroke(currentStroke));
                g2.setColor(brushColor);
                for(int i=0;i<polygonPoints.size()-1;i++){
                    Point a = polygonPoints.get(i); Point b = polygonPoints.get(i+1);
                    g2.drawLine(a.x,a.y,b.x,b.y);
                }
                Point last = polygonPoints.get(polygonPoints.size()-1);
                Point mouse = getMousePosition();
                if(mouse != null) g2.drawLine(last.x,last.y,mouse.x,mouse.y);
                Point fp = polygonPoints.get(0);
                g2.setColor(brushColor); g2.fillOval(fp.x-4, fp.y-4, 8, 8);
            }

            if(currentPath != null && draw_figure && jComboBox1.getSelectedIndex() == 0){
                g2.setStroke(new BasicStroke(currentStroke));
                g2.setColor(brushColor);
                g2.draw(currentPath);
            }

            if(draw_figure && (jComboBox1.getSelectedIndex() == 1 || jComboBox1.getSelectedIndex() == 2)){
                int tool = jComboBox1.getSelectedIndex();
                int x = Math.min(first_mouse_x, currentMouseX);
                int y = Math.min(first_mouse_y, currentMouseY);
                int w = Math.abs(currentMouseX - first_mouse_x);
                int h = Math.abs(currentMouseY - first_mouse_y);
                Shape preview = (tool == 1)
                        ? new Rectangle2D.Double(x,y,w,h)
                        : new Ellipse2D.Double(x,y,w,h);
                g2.setColor(new Color(brushColor.getRed(), brushColor.getGreen(), brushColor.getBlue(), 100));
                g2.fill(preview);
                g2.setColor(brushColor);
                g2.setStroke(new BasicStroke(currentStroke));
                g2.draw(preview);
            }

            g2.dispose();
        }

        @Override public Dimension getPreferredSize(){ return new Dimension(800,600); }
    }

    public static void main(String[] args){
        try{ UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
        SwingUtilities.invokeLater(() -> { MajPejnt m = new MajPejnt(); m.setVisible(true); });
    }
}
