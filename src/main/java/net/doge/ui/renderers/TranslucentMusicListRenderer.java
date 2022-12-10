package net.doge.ui.renderers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.doge.constants.Fonts;
import net.doge.constants.ImageConstants;
import net.doge.constants.NetMusicSource;
import net.doge.constants.SimplePath;
import net.doge.models.AudioFile;
import net.doge.models.MusicPlayer;
import net.doge.models.NetMusicInfo;
import net.doge.ui.components.CustomLabel;
import net.doge.ui.components.CustomPanel;
import net.doge.utils.ImageUtils;
import net.doge.utils.StringUtils;
import net.doge.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @Author yzx
 * @Description
 * @Date 2020/12/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslucentMusicListRenderer extends DefaultListCellRenderer {
    // 属性不能用 font，不然重复！
    private Font customFont = Fonts.NORMAL;
    // 前景色
    private Color foreColor;
    // 选中的颜色
    private Color selectedColor;
    private boolean drawBg;
    private int hoverIndex = -1;

    private MusicPlayer player;
    private ImageIcon musicIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "musicItem.png"), ImageConstants.smallWidth));
    private ImageIcon musicMvIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "musicMvItem.png"), ImageConstants.smallWidth));
    private ImageIcon programIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "programItem.png"), ImageConstants.smallWidth));
    private ImageIcon playingIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "playingItem.png"), ImageConstants.smallWidth));

    private ImageIcon musicSIcon;
    private ImageIcon musicMvSIcon;
    private ImageIcon programSIcon;
    private ImageIcon playingSIcon;

    public TranslucentMusicListRenderer(MusicPlayer player) {
        this.player = player;
    }

    public void setForeColor(Color foreColor) {
        this.foreColor = foreColor;
        musicIcon = ImageUtils.dye(musicIcon, foreColor);
        musicMvIcon = ImageUtils.dye(musicMvIcon, foreColor);
        programIcon = ImageUtils.dye(programIcon, foreColor);
        playingIcon = ImageUtils.dye(playingIcon, foreColor);
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
        musicSIcon = ImageUtils.dye(musicIcon, selectedColor);
        musicMvSIcon = ImageUtils.dye(musicMvIcon, selectedColor);
        programSIcon = ImageUtils.dye(programIcon, selectedColor);
        playingSIcon = ImageUtils.dye(playingIcon, selectedColor);
    }

    public void setDrawBg(boolean drawBg) {
        this.drawBg = drawBg;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (!(value instanceof AudioFile) && !(value instanceof NetMusicInfo)) return this;

        AudioFile file = null;
        NetMusicInfo netMusicInfo = null;
        boolean isFile = value instanceof AudioFile;
        if (isFile) file = (AudioFile) value;
        else netMusicInfo = (NetMusicInfo) value;

        CustomPanel outerPanel = new CustomPanel();
        CustomLabel iconLabel = new CustomLabel();
        CustomLabel nameLabel = new CustomLabel();
        CustomLabel artistLabel = new CustomLabel();
        CustomLabel albumNameLabel = new CustomLabel();
        CustomLabel durationLabel = new CustomLabel();

        if (isFile) {
            // 播放中的文件图标不同
            if (!player.isPlayingFile(file)) iconLabel.setIcon(isSelected ? musicSIcon : musicIcon);
            else iconLabel.setIcon(isSelected ? playingSIcon : playingIcon);
        } else {
            // 播放中的文件图标不同
            if (!player.isPlayingNetMusic(netMusicInfo)) {
                if (netMusicInfo.hasMv()) iconLabel.setIcon(isSelected ? musicMvSIcon : musicMvIcon);
                else if (netMusicInfo.isProgram()) iconLabel.setIcon(isSelected ? programSIcon : programIcon);
                else iconLabel.setIcon(isSelected ? musicSIcon : musicIcon);
            } else iconLabel.setIcon(isSelected ? playingSIcon : playingIcon);
        }

        iconLabel.setIconTextGap(15);
        iconLabel.setHorizontalTextPosition(LEFT);

        outerPanel.setForeground(isSelected ? selectedColor : foreColor);
        iconLabel.setForeground(isSelected ? selectedColor : foreColor);
        nameLabel.setForeground(isSelected ? selectedColor : foreColor);
        artistLabel.setForeground(isSelected ? selectedColor : foreColor);
        albumNameLabel.setForeground(isSelected ? selectedColor : foreColor);
        durationLabel.setForeground(isSelected ? selectedColor : foreColor);

        iconLabel.setFont(customFont);
        nameLabel.setFont(customFont);
        artistLabel.setFont(customFont);
        albumNameLabel.setFont(customFont);
        durationLabel.setFont(customFont);

        GridLayout layout = new GridLayout(1, 5);
        layout.setHgap(15);
        outerPanel.setLayout(layout);

        outerPanel.add(iconLabel);
        outerPanel.add(nameLabel);
        outerPanel.add(artistLabel);
        outerPanel.add(albumNameLabel);
        outerPanel.add(durationLabel);

        final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
        String source = StringUtils.textToHtml(isFile ? "  " : NetMusicSource.names[netMusicInfo.getSource()]);
        String name = StringUtils.textToHtml(StringUtils.wrapLineByWidth(isFile ? file.hasSongName() ? file.getSongName() : file.toString() : netMusicInfo.getName(), maxWidth));
        String artist = StringUtils.textToHtml(StringUtils.wrapLineByWidth(isFile ? file.hasArtist() ? file.getArtist() : ""
                : netMusicInfo.hasArtist() ? netMusicInfo.getArtist() : "", maxWidth));
        String albumName = StringUtils.textToHtml(StringUtils.wrapLineByWidth(isFile ? (file.hasAlbum() ? file.getAlbum() : "")
                : netMusicInfo.hasAlbumName() ? netMusicInfo.getAlbumName() : "", maxWidth));
        String duration = StringUtils.textToHtml(isFile ? file.hasDuration() ? TimeUtils.format(file.getDuration()) : "--:--"
                : netMusicInfo.hasDuration() ? TimeUtils.format(netMusicInfo.getDuration()) : "--:--");

        iconLabel.setText(source);
        nameLabel.setText(name);
        artistLabel.setText(artist);
        albumNameLabel.setText(albumName);
        durationLabel.setText(duration);

        Dimension ps = nameLabel.getPreferredSize();
        Dimension ps2 = artistLabel.getPreferredSize();
        Dimension ps3 = albumNameLabel.getPreferredSize();
        int ph = Math.max(ps.height, Math.max(ps2.height, ps3.height));
        Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 10, 46));
        outerPanel.setPreferredSize(d);
        // 设置 list 元素宽度防止 outerPanel 设置最佳大小时不改变大小！
        list.setFixedCellWidth(list.getVisibleRect().width - 10);

        outerPanel.setDrawBg(isSelected || hoverIndex == index);

        return outerPanel;
    }

    @Override
    public void paintComponent(Graphics g) {
        // 画背景
        if (drawBg) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getForeground());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            // 注意这里不能用 getVisibleRect ！！！
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        super.paintComponent(g);
    }
}
