package net.doge.ui.components;

import javafx.embed.swing.JFXPanel;
import net.doge.constants.SpectrumConstants;
import net.doge.ui.PlayerFrame;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.GeneralPath;

public class SpectrumPanel extends JFXPanel {
    private boolean drawSpectrum;

    private final Stroke stroke = new BasicStroke(3);
    private final int space = 90;
    private PlayerFrame f;

    public SpectrumPanel(PlayerFrame f) {
        this.f = f;
        drawSpectrum = false;

        setOpaque(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SpectrumConstants.BAR_NUM = (getWidth() - space * 2 + SpectrumConstants.BAR_GAP) / (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP);
            }
        });
    }

    public void setDrawSpectrum(boolean drawSpectrum) {
        this.drawSpectrum = drawSpectrum;
        repaint();
    }

    public boolean isDrawSpectrum() {
        return drawSpectrum;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (drawSpectrum) paintSpectrum(g);
        super.paintComponent(g);
    }

    public void paintSpectrum(Graphics g) {
        double[] specs = f.getPlayer().getSpecs();
        int pw = getWidth(), ph = getHeight();
        if (pw == 0 || ph == 0) return;
        int barNum = SpectrumConstants.BAR_NUM, imgX = (pw - SpectrumConstants.BAR_WIDTH * barNum - SpectrumConstants.BAR_GAP * (barNum - 1)) / 2;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(f.getCurrUIStyle().getSpectrumColor());
        g2d.setStroke(stroke);
        int style = f.currSpecStyle;
        for (int i = 0; i < barNum; i++) {
            // 得到频谱高度并绘制
            int sHeight = (int) specs[i];
            switch (style) {
                case SpectrumConstants.GROUND:
                    g2d.fillRoundRect(
                            imgX + i * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP),
                            ph - sHeight,
                            SpectrumConstants.BAR_WIDTH,
                            sHeight,
                            4, 4
                    );
                    break;
                case SpectrumConstants.ABOVE:
                    g2d.fillRoundRect(
                            imgX + i * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP),
                            (ph - sHeight) / 2,
                            SpectrumConstants.BAR_WIDTH,
                            sHeight,
                            4, 4
                    );
                    break;
                case SpectrumConstants.LINE:
                    if (i + 1 >= barNum) return;
                    g2d.drawLine(
                            imgX + SpectrumConstants.BAR_WIDTH / 2 + i * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP),
                            ph - sHeight,
                            imgX + SpectrumConstants.BAR_WIDTH / 2 + (i + 1) * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP),
                            ph - (int) specs[i + 1]
                    );
                    break;
                case SpectrumConstants.CURVE:
                    if (i + 1 >= barNum) return;
                    int p1x = imgX + SpectrumConstants.BAR_WIDTH / 2 + i * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP);
                    int p1y = ph - sHeight;
                    int p2x = imgX + SpectrumConstants.BAR_WIDTH / 2 + (i + 1) * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP);
                    int p2y = ph - (int) specs[i + 1];
                    int p3x = (p1x + p2x) / 2;
                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1x, p1y);
                    path.curveTo(p3x, p1y, p3x, p2y, p2x, p2y);
                    g2d.draw(path);
                    break;
                case SpectrumConstants.HILL:
                    if (i + 1 >= barNum) return;
                    p1x = imgX + SpectrumConstants.BAR_WIDTH / 2 + i * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP);
                    p1y = ph - sHeight;
                    p2x = imgX + SpectrumConstants.BAR_WIDTH / 2 + (i + 1) * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP);
                    p2y = ph - (int) specs[i + 1];
                    Polygon polygon = new Polygon(new int[]{p1x, p1x, p2x, p2x}, new int[]{p1y, ph, ph, p2y}, 4);
                    g2d.fill(polygon);
                    break;
                case SpectrumConstants.WAVE:
                    if (i + 1 >= barNum) return;
                    p1x = imgX + SpectrumConstants.BAR_WIDTH / 2 + i * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP);
                    p1y = ph - sHeight;
                    p2x = imgX + SpectrumConstants.BAR_WIDTH / 2 + (i + 1) * (SpectrumConstants.BAR_WIDTH + SpectrumConstants.BAR_GAP);
                    p2y = ph - (int) specs[i + 1];
                    p3x = (p1x + p2x) / 2;
                    path = new GeneralPath();
                    path.moveTo(p1x, p1y);
                    path.curveTo(p3x, p1y, p3x, p2y, p2x, p2y);
                    path.lineTo(p2x, ph);
                    path.lineTo(p1x, ph);
                    path.lineTo(p1x, p1y);
                    g2d.fill(path);
                    break;
            }

        }
//        d++;
//        graphics.drawImage(spectrumImg, imgX, 0, null);
    }
}
