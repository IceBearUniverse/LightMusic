package net.doge.ui.components.dialog;

import net.coobird.thumbnailator.Thumbnails;
import net.doge.constants.BlurType;
import net.doge.constants.Colors;
import net.doge.constants.Fonts;
import net.doge.models.UIStyle;
import net.doge.ui.PlayerFrame;
import net.doge.ui.components.CustomLabel;
import net.doge.utils.ImageUtils;
import net.doge.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @Author yzx
 * @Description 自定义淡入淡出式对话框
 * @Date 2021/1/5
 */
public class TipDialog extends JDialog {
    private TipDialog THIS = this;
    private Dimension size;
    private Font font = Fonts.NORMAL_MEDIUM;
    private Color themeColor;
    private int ms;
    private boolean closing;

    private PlayerFrame f;
    private String message = "";
    private CustomLabel messageLabel = new CustomLabel(message);
    private UndergroundPanel mainPanel = new UndergroundPanel();

    // 最大阴影透明度
    private final int TOP_OPACITY = 30;
    // 阴影大小像素
    private final int pixels = 10;

    public boolean isNotEmpty() {
        return StringUtils.isNotEmpty(StringUtils.removeHTMLLabel(message));
    }

    public void setMessage(String message) {
        this.message = message;
        messageLabel.setText(message);
        repaint();
    }

    public void setMs(int ms) {
        this.ms = ms;
    }

    // the auto closing option window constructor
    public TipDialog(PlayerFrame f, String message, int ms) {
        this(f);
        setMessage(message);
        this.ms = ms;
    }

    public TipDialog(PlayerFrame f, int ms) {
        this(f);
        this.ms = ms;
    }

    public TipDialog(PlayerFrame f, String message) {
        this(f);
        setMessage(message);
        this.ms = 1000;
        // 视频播放界面的对话框需要置顶
        setAlwaysOnTop(true);
    }

    public TipDialog(PlayerFrame f) {
        super(f);
        this.f = f;
        setUndecorated(true);
    }

    public void updateSize() {
        FontMetrics metrics = messageLabel.getFontMetrics(font);
        int sWidth = metrics.stringWidth(StringUtils.removeHTMLLabel(message));
        int sHeight = metrics.getHeight();
        size = new Dimension(sWidth + 60 + 2 * pixels, sHeight + 40 + 2 * pixels);
        setSize(size);
    }

    public void updateView() {
        // 设置主题色
        themeColor = f.getCurrUIStyle().getLabelColor();
        updateSize();
        // Dialog 背景透明
        setBackground(Colors.TRANSLUCENT);
        setLocationRelativeTo(null);
        updateBlur();

        messageLabel.setForeground(themeColor);
        messageLabel.setFont(font);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(messageLabel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    public void updateBlur() {
        BufferedImage bufferedImage;
        if (f.blurType != BlurType.OFF && f.getPlayer().loadedMusic()) {
            bufferedImage = f.getPlayer().getMusicInfo().getAlbumImage();
            if (bufferedImage == f.getDefaultAlbumImage()) bufferedImage = ImageUtils.eraseTranslucency(bufferedImage);
            if (f.blurType == BlurType.MC)
                bufferedImage = ImageUtils.dyeRect(1, 1, ImageUtils.getAvgRGB(bufferedImage));
            else if (f.blurType == BlurType.LG)
                bufferedImage = ImageUtils.toGradient(bufferedImage);
        } else {
            UIStyle style = f.getCurrUIStyle();
            bufferedImage = style.getImg();
        }
        doBlur(bufferedImage);
    }

    private Timer timer;
    private Timer closeWait;

    private void initTimer() {
        if (timer != null) return;
        timer = new Timer(2, e -> {
            // 渐隐效果
            float opacity = getOpacity();
            if (closing) opacity = Math.max(0, opacity - 0.02f);
            else opacity = Math.min(1, opacity + 0.02f);
            setOpacity(opacity);
            if (closing && opacity <= 0 || !closing && opacity >= 1) {
                timer.stop();
                if (closing) {
                    f.currDialogs.remove(THIS);
                    dispose();
                } else if (ms > 0) {
                    closeWait.start();
                }
            }
        });
        // 停留时间
        closeWait = new Timer(ms, ev -> {
            close();
            closeWait.stop();
        });
    }

    public void showDialog() {
        initTimer();
        updateView();
        f.currDialogs.add(this);
        setOpacity(0);
        setVisible(true);
        closing = false;
        timer.start();
    }

    public void close() {
        closing = true;
        timer.start();
    }

    private void doBlur(BufferedImage bufferedImage) {
        int dw = getWidth(), dh = getHeight();
        try {
            // 截取中间的一部分(有的图片是长方形)
            if (f.blurType == BlurType.CV) bufferedImage = ImageUtils.cropCenter(bufferedImage);
            // 处理成 100 * 100 大小
            if (f.gsOn) bufferedImage = ImageUtils.width(bufferedImage, 100);
            // 消除透明度
            bufferedImage = ImageUtils.eraseTranslucency(bufferedImage);
            // 高斯模糊并暗化
            if (f.gsOn) bufferedImage = ImageUtils.doBlur(bufferedImage);
            if (f.darkerOn) bufferedImage = ImageUtils.darker(bufferedImage);
            // 放大至窗口大小
            bufferedImage = dw > dh ? ImageUtils.width(bufferedImage, dw) : ImageUtils.height(bufferedImage, dh);
            // 裁剪中间的一部分
            if (f.blurType == BlurType.CV || f.blurType == BlurType.OFF) {
                int iw = bufferedImage.getWidth(), ih = bufferedImage.getHeight();
                bufferedImage = Thumbnails.of(bufferedImage)
                        .scale(1f)
                        .sourceRegion(dw > dh ? 0 : (iw - dw) / 2, dw > dh ? (ih - dh) / 2 : 0, dw, dh)
                        .outputQuality(0.1)
                        .asBufferedImage();
            } else {
                bufferedImage = ImageUtils.forceSize(bufferedImage, dw, dh);
            }
            // 设置圆角
            bufferedImage = ImageUtils.setRadius(bufferedImage, 10);
            mainPanel.setBackgroundImage(bufferedImage);
            repaint();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private class UndergroundPanel extends JPanel {
        private BufferedImage backgroundImage;

        public UndergroundPanel() {
            // 阴影边框
            Border border = BorderFactory.createEmptyBorder(pixels, pixels, pixels, pixels);
            setBorder(BorderFactory.createCompoundBorder(getBorder(), border));
        }

        public void setBackgroundImage(BufferedImage backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            // 避免锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            if (backgroundImage != null) {
//            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2d.drawImage(backgroundImage, pixels, pixels, getWidth() - 2 * pixels, getHeight() - 2 * pixels, this);
            }
//            g2d.setColor(ImageUtils.getAvgRGB(f.getGlobalPanel().getBackgroundImage()));
//            g2d.fillRoundRect(pixels, pixels, getWidth() - 2 * pixels, getHeight() - 2 * pixels, 10, 10);

            // 画边框阴影
            for (int i = 0; i < pixels; i++) {
                g2d.setColor(new Color(0, 0, 0, ((TOP_OPACITY / pixels) * i)));
                g2d.drawRoundRect(i, i, getWidth() - ((i * 2) + 1), getHeight() - ((i * 2) + 1), 10, 10);
            }
        }
    }
}