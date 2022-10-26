package net.doge.ui.renderers;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import net.doge.constants.Fonts;
import net.doge.models.Statement;
import net.doge.ui.components.StringTwoColor;
import net.doge.ui.componentui.LabelUI;
import net.doge.ui.componentui.ListUI;
import net.doge.utils.StringUtils;
import lombok.Data;

import javax.swing.*;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;

/**
 * @Author yzx
 * @Description
 * @Date 2020/12/7
 */
@Data
public class TranslucentLrcListRenderer extends DefaultListCellRenderer {
    private Font defaultFont;
    private Font highlightFont;
    // 走过的歌词颜色
    private Color foregroundColor;
    // 未走的歌词颜色
    private Color backgroundColor;
    // 高亮文字
    private StringTwoColor stc;
    // 比例
    private double ratio;
    private int row;
    private int[] rows;

    private int thresholdWidth = 700;

    private LabelUI highlightLabelUI;
    private LabelUI normalLabelUI;

    public TranslucentLrcListRenderer() {
        highlightLabelUI = new LabelUI(1);
        normalLabelUI = new LabelUI(0.5f);
    }

    public void setRow(int row) {
        this.ratio = 0;
        this.row = row;
    }

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        JLabel label = (JLabel) component;
        Statement statement = (Statement) value;

        label.setUI(index != row ? normalLabelUI : highlightLabelUI);
        setForeground(backgroundColor);
        // 所有标签透明
        label.setOpaque(false);
        setText(StringUtils.textToHtml(StringUtils.wrapLineByWidth(statement.toString(), thresholdWidth)));
        if (rows == null) {
            // 高亮的行的样式
            if (index == row) {
                setFont(highlightFont);
                if (stc == null || !stc.getTxt().equals(getText()) || !stc.getC1().equals(foregroundColor) || !stc.getC2().equals(backgroundColor))
                    stc = new StringTwoColor(this, foregroundColor, backgroundColor, ratio, false, thresholdWidth);
                else stc.setRatio(ratio);
                setIcon(stc.getImageIcon());
                setText("");
            }
            // 其他行的样式
            else {
                setFont(defaultFont);
            }
            // 设置 list 对应行的高度
            ((ListUI) list.getUI()).setCellHeight(index, getPreferredSize().height);
        } else {
            for (int i = 0; i < rows.length; i++) {
                if (index == rows[i]) {
                    setFont(getFont().deriveFont((float) (getFont().getSize() + 8)));
                }
            }
        }
        return this;
    }
}
