package socket_project_client;

import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;


public class ClientNameBoldRenderer extends DefaultListCellRenderer {
    
    private DefaultListModel<String> userListModel;
    private Font boldFont;
    
    public ClientNameBoldRenderer(DefaultListModel<String> userListModel) {
        this.userListModel = userListModel;
        Map<TextAttribute, Object> fontAttributes = new HashMap<>();
        fontAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        boldFont = getFont().deriveFont(fontAttributes);
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value != null && value.equals(ProjectClient.getInstance().getUsername())) {
            renderer.setFont(boldFont); // 현재 사용자의 이름을 bold로 설정
        } else {
            renderer.setFont(renderer.getFont().deriveFont(Font.PLAIN)); // 다른 이름을 plain으로 설정
        }
        
        return renderer;
    }
}