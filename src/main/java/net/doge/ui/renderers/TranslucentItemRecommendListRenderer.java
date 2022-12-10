package net.doge.ui.renderers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.doge.constants.Fonts;
import net.doge.constants.ImageConstants;
import net.doge.constants.NetMusicSource;
import net.doge.constants.SimplePath;
import net.doge.models.*;
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
public class TranslucentItemRecommendListRenderer extends DefaultListCellRenderer {
    // 属性不能用 font，不然重复！
    private Font customFont = Fonts.NORMAL;
    // 前景色
    private Color foreColor;
    // 选中的颜色
    private Color selectedColor;
    private boolean drawBg;
    private int hoverIndex = -1;

    private ImageIcon playlistIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "playlistItem.png"), ImageConstants.profileWidth));
    private ImageIcon albumIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "albumItem.png"), ImageConstants.profileWidth));
    private ImageIcon artistIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "artistItem.png"), ImageConstants.profileWidth));
    private ImageIcon radioIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "radioItem.png"), ImageConstants.profileWidth));
    private ImageIcon mvIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "mvItem.png"), ImageConstants.profileWidth));
    private ImageIcon rankingIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "rankingItem.png"), ImageConstants.profileWidth));
    private ImageIcon userIcon = new ImageIcon(ImageUtils.width(ImageUtils.read(SimplePath.ICON_PATH + "userItem.png"), ImageConstants.profileWidth));

    private ImageIcon playlistSIcon;
    private ImageIcon albumSIcon;
    private ImageIcon artistSIcon;
    private ImageIcon radioSIcon;
    private ImageIcon mvSIcon;
    private ImageIcon rankingSIcon;
    private ImageIcon userSIcon;

    public void setForeColor(Color foreColor) {
        this.foreColor = foreColor;
        playlistIcon = ImageUtils.dye(playlistIcon, foreColor);
        albumIcon = ImageUtils.dye(albumIcon, foreColor);
        artistIcon = ImageUtils.dye(artistIcon, foreColor);
        radioIcon = ImageUtils.dye(radioIcon, foreColor);
        mvIcon = ImageUtils.dye(mvIcon, foreColor);
        rankingIcon = ImageUtils.dye(rankingIcon, foreColor);
        userIcon = ImageUtils.dye(userIcon, foreColor);
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
        playlistSIcon = ImageUtils.dye(playlistIcon, selectedColor);
        albumSIcon = ImageUtils.dye(albumIcon, selectedColor);
        artistSIcon = ImageUtils.dye(artistIcon, selectedColor);
        radioSIcon = ImageUtils.dye(radioIcon, selectedColor);
        mvSIcon = ImageUtils.dye(mvIcon, selectedColor);
        rankingSIcon = ImageUtils.dye(rankingIcon, selectedColor);
        userSIcon = ImageUtils.dye(userIcon, selectedColor);
    }

    public void setDrawBg(boolean drawBg) {
        this.drawBg = drawBg;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        list.setFixedCellWidth(list.getVisibleRect().width - 10);
        
        if (value instanceof NetPlaylistInfo) {
            NetPlaylistInfo netPlaylistInfo = (NetPlaylistInfo) value;

            CustomPanel outerPanel = new CustomPanel();
            CustomLabel iconLabel = new CustomLabel();
            CustomLabel nameLabel = new CustomLabel();
            CustomLabel creatorLabel = new CustomLabel();
            CustomLabel playCountLabel = new CustomLabel();
            CustomLabel trackCountLabel = new CustomLabel();

            iconLabel.setHorizontalTextPosition(LEFT);
            iconLabel.setIconTextGap(40);
            iconLabel.setIcon(netPlaylistInfo.hasCoverImgThumb() ? new ImageIcon(netPlaylistInfo.getCoverImgThumb()) : isSelected ? playlistSIcon : playlistIcon);

            outerPanel.setForeground(isSelected ? selectedColor : foreColor);
            iconLabel.setForeground(isSelected ? selectedColor : foreColor);
            nameLabel.setForeground(isSelected ? selectedColor : foreColor);
            creatorLabel.setForeground(isSelected ? selectedColor : foreColor);
            playCountLabel.setForeground(isSelected ? selectedColor : foreColor);
            trackCountLabel.setForeground(isSelected ? selectedColor : foreColor);

            iconLabel.setFont(customFont);
            nameLabel.setFont(customFont);
            creatorLabel.setFont(customFont);
            playCountLabel.setFont(customFont);
            trackCountLabel.setFont(customFont);

            GridLayout layout = new GridLayout(1, 5);
            layout.setHgap(15);
            outerPanel.setLayout(layout);

            outerPanel.add(iconLabel);
            outerPanel.add(nameLabel);
            outerPanel.add(creatorLabel);
            outerPanel.add(trackCountLabel);
            outerPanel.add(playCountLabel);

            final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
            String source = StringUtils.textToHtml(NetMusicSource.names[netPlaylistInfo.getSource()]);
            String name = netPlaylistInfo.hasName() ? StringUtils.textToHtml(StringUtils.wrapLineByWidth(netPlaylistInfo.getName(), maxWidth)) : "";
            String creator = netPlaylistInfo.hasCreator() ? StringUtils.textToHtml(StringUtils.wrapLineByWidth(netPlaylistInfo.getCreator(), maxWidth)) : "";
            String playCount = netPlaylistInfo.hasPlayCount() ? StringUtils.formatNumber(netPlaylistInfo.getPlayCount()) : "";
            String trackCount = netPlaylistInfo.hasTrackCount() ? netPlaylistInfo.getTrackCount() + " 歌曲" : "";

            iconLabel.setText(source);
            nameLabel.setText(name);
            creatorLabel.setText(creator);
            playCountLabel.setText(playCount);
            trackCountLabel.setText(trackCount);

            Dimension ps = iconLabel.getPreferredSize();
            Dimension ps2 = nameLabel.getPreferredSize();
            Dimension ps3 = creatorLabel.getPreferredSize();
            int ph = Math.max(ps.height, Math.max(ps2.height, ps3.height));
            Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 12, 46));
            outerPanel.setPreferredSize(d);

            outerPanel.setDrawBg(isSelected || hoverIndex == index);

            return outerPanel;
        } else if (value instanceof NetAlbumInfo) {
            NetAlbumInfo netAlbumInfo = (NetAlbumInfo) value;

            CustomPanel outerPanel = new CustomPanel();
            CustomLabel iconLabel = new CustomLabel();
            CustomLabel nameLabel = new CustomLabel();
            CustomLabel artistLabel = new CustomLabel();
            CustomLabel songNumLabel = new CustomLabel();
            CustomLabel publishTimeLabel = new CustomLabel();

            iconLabel.setHorizontalTextPosition(LEFT);
            iconLabel.setIconTextGap(40);
            iconLabel.setIcon(netAlbumInfo.hasCoverImgThumb() ? new ImageIcon(netAlbumInfo.getCoverImgThumb()) : isSelected ? albumSIcon : albumIcon);

            outerPanel.setForeground(isSelected ? selectedColor : foreColor);
            iconLabel.setForeground(isSelected ? selectedColor : foreColor);
            nameLabel.setForeground(isSelected ? selectedColor : foreColor);
            artistLabel.setForeground(isSelected ? selectedColor : foreColor);
            songNumLabel.setForeground(isSelected ? selectedColor : foreColor);
            publishTimeLabel.setForeground(isSelected ? selectedColor : foreColor);

            iconLabel.setFont(customFont);
            nameLabel.setFont(customFont);
            artistLabel.setFont(customFont);
            songNumLabel.setFont(customFont);
            publishTimeLabel.setFont(customFont);

            GridLayout layout = new GridLayout(1, 5);
            layout.setHgap(15);
            outerPanel.setLayout(layout);

            outerPanel.add(iconLabel);
            outerPanel.add(nameLabel);
            outerPanel.add(artistLabel);
            outerPanel.add(songNumLabel);
            outerPanel.add(publishTimeLabel);

            final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
            String source = StringUtils.textToHtml(NetMusicSource.names[netAlbumInfo.getSource()]);
            String name = StringUtils.textToHtml(StringUtils.wrapLineByWidth(netAlbumInfo.getName(), maxWidth));
            String artist = netAlbumInfo.hasArtist() ? StringUtils.textToHtml(StringUtils.wrapLineByWidth(netAlbumInfo.getArtist(), maxWidth)) : "";
            String songNum = netAlbumInfo.hasSongNum() ? netAlbumInfo.isPhoto() ? netAlbumInfo.getSongNum() + " 图片" : netAlbumInfo.getSongNum() + " 歌曲" : "";
            String publishTime = netAlbumInfo.hasPublishTime() ? netAlbumInfo.getPublishTime() + " 发行" : "";

            iconLabel.setText(source);
            nameLabel.setText(name);
            artistLabel.setText(artist);
            songNumLabel.setText(songNum);
            publishTimeLabel.setText(publishTime);

            Dimension ps = iconLabel.getPreferredSize();
            Dimension ps2 = nameLabel.getPreferredSize();
            Dimension ps3 = artistLabel.getPreferredSize();
            int ph = Math.max(ps.height, Math.max(ps2.height, ps3.height));
            Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 12, 46));
            outerPanel.setPreferredSize(d);
            list.setFixedCellWidth(list.getVisibleRect().width - 10);

            outerPanel.setDrawBg(isSelected || hoverIndex == index);

            return outerPanel;
        } else if (value instanceof NetArtistInfo) {
            NetArtistInfo netArtistInfo = (NetArtistInfo) value;

            CustomPanel outerPanel = new CustomPanel();
            CustomLabel iconLabel = new CustomLabel();
            CustomLabel nameLabel = new CustomLabel();
            CustomLabel songNumLabel = new CustomLabel();
            CustomLabel albumNumLabel = new CustomLabel();
            CustomLabel mvNumLabel = new CustomLabel();

            iconLabel.setHorizontalTextPosition(LEFT);
            iconLabel.setIconTextGap(40);
            iconLabel.setIcon(netArtistInfo.hasCoverImgThumb() ? new ImageIcon(netArtistInfo.getCoverImgThumb()) : isSelected ? artistSIcon : artistIcon);

            outerPanel.setForeground(isSelected ? selectedColor : foreColor);
            iconLabel.setForeground(isSelected ? selectedColor : foreColor);
            nameLabel.setForeground(isSelected ? selectedColor : foreColor);
            songNumLabel.setForeground(isSelected ? selectedColor : foreColor);
            albumNumLabel.setForeground(isSelected ? selectedColor : foreColor);
            mvNumLabel.setForeground(isSelected ? selectedColor : foreColor);

            iconLabel.setFont(customFont);
            nameLabel.setFont(customFont);
            songNumLabel.setFont(customFont);
            albumNumLabel.setFont(customFont);
            mvNumLabel.setFont(customFont);

            GridLayout layout = new GridLayout(1, 5);
            layout.setHgap(15);
            outerPanel.setLayout(layout);

            outerPanel.add(iconLabel);
            outerPanel.add(nameLabel);
            outerPanel.add(songNumLabel);
            outerPanel.add(albumNumLabel);
            outerPanel.add(mvNumLabel);

            final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
            String source = StringUtils.textToHtml(NetMusicSource.names[netArtistInfo.getSource()]);
            String name = netArtistInfo.hasName() ? StringUtils.textToHtml(StringUtils.wrapLineByWidth(netArtistInfo.getName(), maxWidth)) : "";
            String songNum = netArtistInfo.hasSongNum() ? netArtistInfo.fromME() ? netArtistInfo.getSongNum() + " 电台" : netArtistInfo.getSongNum() + " 歌曲" : "";
            String albumNum = netArtistInfo.hasAlbumNum() ? netArtistInfo.getAlbumNum() + " 专辑" : "";
            String mvNum = netArtistInfo.hasMvNum() ? netArtistInfo.getMvNum() + " MV" : "";

            iconLabel.setText(source);
            nameLabel.setText(name);
            songNumLabel.setText(songNum);
            albumNumLabel.setText(albumNum);
            mvNumLabel.setText(mvNum);

            Dimension ps = iconLabel.getPreferredSize();
            Dimension ps2 = nameLabel.getPreferredSize();
            int ph = Math.max(ps.height, ps2.height);
            Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 12, 46));
            outerPanel.setPreferredSize(d);
            list.setFixedCellWidth(list.getVisibleRect().width - 10);

            outerPanel.setDrawBg(isSelected || hoverIndex == index);

            return outerPanel;
        } else if (value instanceof NetRadioInfo) {
            NetRadioInfo netRadioInfo = (NetRadioInfo) value;

            CustomPanel outerPanel = new CustomPanel();
            CustomLabel iconLabel = new CustomLabel();
            CustomLabel nameLabel = new CustomLabel();
            CustomLabel dCustomLabel = new CustomLabel();
            CustomLabel categoryLabel = new CustomLabel();
            CustomLabel trackCountLabel = new CustomLabel();
            CustomLabel playCountLabel = new CustomLabel();
//        CustomLabel createTimeLabel = new CustomLabel();

            iconLabel.setHorizontalTextPosition(LEFT);
            iconLabel.setIconTextGap(25);
            iconLabel.setIcon(netRadioInfo.hasCoverImgThumb() ? new ImageIcon(netRadioInfo.getCoverImgThumb()) : isSelected ? radioSIcon : radioIcon);

            outerPanel.setForeground(isSelected ? selectedColor : foreColor);
            iconLabel.setForeground(isSelected ? selectedColor : foreColor);
            nameLabel.setForeground(isSelected ? selectedColor : foreColor);
            dCustomLabel.setForeground(isSelected ? selectedColor : foreColor);
            categoryLabel.setForeground(isSelected ? selectedColor : foreColor);
            trackCountLabel.setForeground(isSelected ? selectedColor : foreColor);
            playCountLabel.setForeground(isSelected ? selectedColor : foreColor);
//        createTimeLabel.setForeground(isSelected ? selectedColor : foreColor);

            iconLabel.setFont(customFont);
            nameLabel.setFont(customFont);
            dCustomLabel.setFont(customFont);
            categoryLabel.setFont(customFont);
            trackCountLabel.setFont(customFont);
            playCountLabel.setFont(customFont);
//        createTimeLabel.setFont(customFont);

            GridLayout layout = new GridLayout(1, 5);
            layout.setHgap(15);
            outerPanel.setLayout(layout);

            outerPanel.add(iconLabel);
            outerPanel.add(nameLabel);
            outerPanel.add(dCustomLabel);
            outerPanel.add(categoryLabel);
            outerPanel.add(trackCountLabel);
            outerPanel.add(playCountLabel);
//        outerPanel.add(createTimeLabel);

            final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
            String source = StringUtils.textToHtml(NetMusicSource.names[netRadioInfo.getSource()]);
            String name = StringUtils.textToHtml(StringUtils.wrapLineByWidth(netRadioInfo.getName(), maxWidth));
            String dj = StringUtils.textToHtml(StringUtils.wrapLineByWidth(netRadioInfo.hasDj() ? netRadioInfo.getDj() : "", maxWidth));
            String category = netRadioInfo.hasCategory() ? netRadioInfo.getCategory() : "";
            String trackCount = netRadioInfo.hasTrackCount() ? netRadioInfo.getTrackCount() + " 节目" : "";
            String playCount = netRadioInfo.hasPlayCount() ? StringUtils.formatNumber(netRadioInfo.getPlayCount()) : "";
//        String createTime = netRadioInfo.hasCreateTime() ? netRadioInfo.getCreateTime() : "";

            iconLabel.setText(source);
            nameLabel.setText(name);
            dCustomLabel.setText(dj);
            categoryLabel.setText(category);
            trackCountLabel.setText(trackCount);
            playCountLabel.setText(playCount);
//        createTimeLabel.setText(createTime);

            Dimension ps = iconLabel.getPreferredSize();
            Dimension ps2 = nameLabel.getPreferredSize();
            Dimension ps3 = dCustomLabel.getPreferredSize();
            int ph = Math.max(ps.height, Math.max(ps2.height, ps3.height));
            Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 12, 46));
            outerPanel.setPreferredSize(d);
            list.setFixedCellWidth(list.getVisibleRect().width - 10);

            outerPanel.setDrawBg(isSelected || hoverIndex == index);

            return outerPanel;
        } else if (value instanceof NetMvInfo) {
            NetMvInfo netMvInfo = (NetMvInfo) value;

            CustomPanel outerPanel = new CustomPanel();
            CustomLabel iconLabel = new CustomLabel();
            CustomLabel nameLabel = new CustomLabel();
            CustomLabel artistLabel = new CustomLabel();
            CustomLabel durationLabel = new CustomLabel();
            CustomLabel playCountLabel = new CustomLabel();
            CustomLabel pubTimeLabel = new CustomLabel();

            iconLabel.setHorizontalTextPosition(LEFT);
            iconLabel.setIconTextGap(10);
            iconLabel.setIcon(netMvInfo.hasCoverImgThumb() ? new ImageIcon(netMvInfo.getCoverImgThumb()) : isSelected ? mvSIcon : mvIcon);

            outerPanel.setForeground(isSelected ? selectedColor : foreColor);
            iconLabel.setForeground(isSelected ? selectedColor : foreColor);
            nameLabel.setForeground(isSelected ? selectedColor : foreColor);
            artistLabel.setForeground(isSelected ? selectedColor : foreColor);
            durationLabel.setForeground(isSelected ? selectedColor : foreColor);
            playCountLabel.setForeground(isSelected ? selectedColor : foreColor);
            pubTimeLabel.setForeground(isSelected ? selectedColor : foreColor);

            iconLabel.setFont(customFont);
            nameLabel.setFont(customFont);
            artistLabel.setFont(customFont);
            durationLabel.setFont(customFont);
            playCountLabel.setFont(customFont);
            pubTimeLabel.setFont(customFont);

            GridLayout layout = new GridLayout(1, 5);
            layout.setHgap(15);
            outerPanel.setLayout(layout);

            outerPanel.add(iconLabel);
            outerPanel.add(nameLabel);
            outerPanel.add(artistLabel);
            outerPanel.add(durationLabel);
            outerPanel.add(playCountLabel);
            outerPanel.add(pubTimeLabel);

            final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
            String source = StringUtils.textToHtml(NetMusicSource.names[netMvInfo.getSource()]);
            String name = StringUtils.textToHtml(StringUtils.wrapLineByWidth(netMvInfo.getName(), maxWidth));
            String artist = StringUtils.textToHtml(StringUtils.wrapLineByWidth(netMvInfo.getArtist(), maxWidth));
            String duration = netMvInfo.hasDuration() ? TimeUtils.format(netMvInfo.getDuration()) : "--:--";
            String playCount = netMvInfo.hasPlayCount() ? StringUtils.formatNumber(netMvInfo.getPlayCount()) : "";
            String pubTime = netMvInfo.hasPubTime() ? netMvInfo.getPubTime() : "";

            iconLabel.setText(source);
            nameLabel.setText(name);
            artistLabel.setText(artist);
            durationLabel.setText(duration);
            playCountLabel.setText(playCount);
            pubTimeLabel.setText(pubTime);

            Dimension ps = iconLabel.getPreferredSize();
            Dimension ps2 = nameLabel.getPreferredSize();
            Dimension ps3 = artistLabel.getPreferredSize();
            int ph = Math.max(ps.height, Math.max(ps2.height, ps3.height));
            Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 12, 46));
            outerPanel.setPreferredSize(d);
            list.setFixedCellWidth(list.getVisibleRect().width - 10);

            outerPanel.setDrawBg(isSelected || hoverIndex == index);

            return outerPanel;
        } else if (value instanceof NetRankingInfo) {
            NetRankingInfo netRankingInfo = (NetRankingInfo) value;

            CustomPanel outerPanel = new CustomPanel();
            CustomLabel iconLabel = new CustomLabel();
            CustomLabel nameLabel = new CustomLabel();
            CustomLabel playCountLabel = new CustomLabel();
            CustomLabel updateFreLabel = new CustomLabel();
            CustomLabel updateTimeLabel = new CustomLabel();

            iconLabel.setHorizontalTextPosition(LEFT);
            iconLabel.setIconTextGap(40);
            iconLabel.setIcon(netRankingInfo.hasCoverImgThumb() ? new ImageIcon(netRankingInfo.getCoverImgThumb()) : isSelected ? rankingSIcon : rankingIcon);

            outerPanel.setForeground(isSelected ? selectedColor : foreColor);
            iconLabel.setForeground(isSelected ? selectedColor : foreColor);
            nameLabel.setForeground(isSelected ? selectedColor : foreColor);
            playCountLabel.setForeground(isSelected ? selectedColor : foreColor);
            updateFreLabel.setForeground(isSelected ? selectedColor : foreColor);
            updateTimeLabel.setForeground(isSelected ? selectedColor : foreColor);

            iconLabel.setFont(customFont);
            nameLabel.setFont(customFont);
            playCountLabel.setFont(customFont);
            updateFreLabel.setFont(customFont);
            updateTimeLabel.setFont(customFont);

            GridLayout layout = new GridLayout(1, 5);
            layout.setHgap(15);
            outerPanel.setLayout(layout);

            outerPanel.add(iconLabel);
            outerPanel.add(nameLabel);
            outerPanel.add(playCountLabel);
            outerPanel.add(updateFreLabel);
            outerPanel.add(updateTimeLabel);

            final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
            String source = StringUtils.textToHtml(NetMusicSource.names[netRankingInfo.getSource()]);
            String name = StringUtils.textToHtml(StringUtils.wrapLineByWidth(netRankingInfo.getName(), maxWidth));
            String playCount = netRankingInfo.hasPlayCount() ? StringUtils.formatNumber(netRankingInfo.getPlayCount()) : "";
            String updateFre = netRankingInfo.hasUpdateFre() ? netRankingInfo.getUpdateFre() : "";
            String updateTime = netRankingInfo.hasUpdateTime() ? netRankingInfo.getUpdateTime() + " 更新" : "";

            iconLabel.setText(source);
            nameLabel.setText(name);
            playCountLabel.setText(playCount);
            updateFreLabel.setText(updateFre);
            updateTimeLabel.setText(updateTime);

            Dimension ps = iconLabel.getPreferredSize();
            Dimension ps2 = nameLabel.getPreferredSize();
            int ph = Math.max(ps.height, ps2.height);
            Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 12, 46));
            outerPanel.setPreferredSize(d);
            list.setFixedCellWidth(list.getVisibleRect().width - 10);

            outerPanel.setDrawBg(isSelected || hoverIndex == index);

            return outerPanel;
        } else if (value instanceof NetUserInfo) {
            NetUserInfo netUserInfo = (NetUserInfo) value;

            CustomPanel outerPanel = new CustomPanel();
            CustomLabel avatarLabel = new CustomLabel();
            CustomLabel nameLabel = new CustomLabel();
            CustomLabel genderLabel = new CustomLabel();
//        CustomLabel birthdayLabel = new CustomLabel();
//        CustomLabel areaLabel = new CustomLabel();
            CustomLabel followLabel = new CustomLabel();
            CustomLabel followedLabel = new CustomLabel();
            CustomLabel playlistCountLabel = new CustomLabel();

            avatarLabel.setHorizontalTextPosition(LEFT);
            avatarLabel.setIconTextGap(25);
            avatarLabel.setIcon(netUserInfo.hasAvatarThumb() ? new ImageIcon(netUserInfo.getAvatarThumb()) : isSelected ? userSIcon : userIcon);

            outerPanel.setForeground(isSelected ? selectedColor : foreColor);
            avatarLabel.setForeground(isSelected ? selectedColor : foreColor);
            nameLabel.setForeground(isSelected ? selectedColor : foreColor);
            genderLabel.setForeground(isSelected ? selectedColor : foreColor);
//        birthdayLabel.setForeground(isSelected ? selectedColor : foreColor);
//        areaLabel.setForeground(isSelected ? selectedColor : foreColor);
            followLabel.setForeground(isSelected ? selectedColor : foreColor);
            followedLabel.setForeground(isSelected ? selectedColor : foreColor);
            playlistCountLabel.setForeground(isSelected ? selectedColor : foreColor);

            avatarLabel.setFont(customFont);
            nameLabel.setFont(customFont);
            genderLabel.setFont(customFont);
//        birthdayLabel.setFont(customFont);
//        areaLabel.setFont(customFont);
            followLabel.setFont(customFont);
            followedLabel.setFont(customFont);
            playlistCountLabel.setFont(customFont);

            GridLayout layout = new GridLayout(1, 5);
            layout.setHgap(15);
            outerPanel.setLayout(layout);

            outerPanel.add(avatarLabel);
            outerPanel.add(nameLabel);
            outerPanel.add(genderLabel);
//        outerPanel.add(birthdayLabel);
//        outerPanel.add(areaLabel);
            outerPanel.add(playlistCountLabel);
            outerPanel.add(followLabel);
            outerPanel.add(followedLabel);

            final int maxWidth = (list.getVisibleRect().width - 10 - (outerPanel.getComponentCount() - 1) * layout.getHgap()) / outerPanel.getComponentCount();
            String source = StringUtils.textToHtml(NetMusicSource.names[netUserInfo.getSource()]);
            String name = StringUtils.textToHtml(StringUtils.wrapLineByWidth(netUserInfo.getName(), maxWidth));
            String gender = netUserInfo.hasGender() ? netUserInfo.getGender() : "";
//        String birthday = netUserInfo.hasBirthday() ? netUserInfo.getBirthday() : "";
//        String area = netUserInfo.hasArea() ? netUserInfo.getArea() : "";
            String playlistCount = netUserInfo.hasPlaylistCount() ? netUserInfo.getPlaylistCount() + " 歌单"
                    : netUserInfo.hasRadioCount() ? netUserInfo.getRadioCount() + " 电台，" + netUserInfo.getProgramCount() + " 节目"
                    : netUserInfo.hasProgramCount() ? netUserInfo.getProgramCount() + " 节目"
                    : "";
            String follow = netUserInfo.hasFollow() ? StringUtils.formatNumberWithoutSuffix(netUserInfo.getFollow()) + " 关注" : "";
            String followed = netUserInfo.hasFollowed() ? StringUtils.formatNumberWithoutSuffix(netUserInfo.getFollowed()) + " 粉丝" : "";

            avatarLabel.setText(source);
            nameLabel.setText(name);
            genderLabel.setText(gender);
//        birthdayLabel.setText(birthday);
//        areaLabel.setText(area);
            playlistCountLabel.setText(playlistCount);
            followLabel.setText(follow);
            followedLabel.setText(followed);

            Dimension ps = avatarLabel.getPreferredSize();
            Dimension ps2 = nameLabel.getPreferredSize();
            int ph = Math.max(ps.height, ps2.height);
            Dimension d = new Dimension(list.getVisibleRect().width - 10, Math.max(ph + 12, 46));
            outerPanel.setPreferredSize(d);
            list.setFixedCellWidth(list.getVisibleRect().width - 10);

            outerPanel.setDrawBg(isSelected || hoverIndex == index);

            return outerPanel;
        }

        return this;
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
