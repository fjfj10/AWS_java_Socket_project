package socket_project_client;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Label;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import socket_project_client.dto.RequestBodyDto;
import socket_project_client.dto.SendMessage;
import lombok.Getter;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.Font;

@Getter
public class ProjectClient extends JFrame {
	
	private static ProjectClient instance;
	public static ProjectClient getInstance() {
		if(instance == null) {
			instance = new ProjectClient();
		}
		return instance;
	}
	
	
	private String username;
	private Socket socket;
	
	private CardLayout mainCardLayout;
	private JPanel mainCardPanel;

	private JPanel chattingRoomListPanel;
	private JScrollPane roomListScrollPanel;
	private DefaultListModel<String> roomListModel;
	private JList roomList;
	
	
	private JPanel chattingRoomPanel;
	private JTextField messageTextField;
	private JTextArea chattingTextArea;	
	private JScrollPane userListScrollPane;
	private DefaultListModel<String> userListModel;
	private JList userList;
	private JButton exitButton;
	private JLabel sendListLabel;
	private JLabel clientNameLabel;
	private JLabel roomNameLabel;
	private JLabel userImageLabel;
			

	/*GUIClient 생성*/
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProjectClient frame = ProjectClient.getInstance();
					frame.setVisible(true);
					frame.clientNameLabel.setText("<< 접속자: "+ frame.username + " >>");	
					ClientReceiver clientReceiver = new ClientReceiver();					
					clientReceiver.start();

					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("connection", frame.username); 
					ClientSender.getInstance().send(requestBodyDto);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public ProjectClient() {
		
		username = JOptionPane.showInputDialog(chattingRoomPanel, "ID를 입력하세요");							
		
		if(Objects.isNull(username)) {
			System.exit(0);
		}
		if(username.isBlank()) {
			System.exit(0);
		}
		try {
			socket = new Socket("127.0.0.1", 8000);        
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 587, 586);

		/*<<카드레이아웃을 사용하는 패널 생성>>*/
		mainCardLayout = new CardLayout();
		mainCardPanel = new JPanel();
		mainCardPanel.setLayout(mainCardLayout);
		setContentPane(mainCardPanel);
		
		/*<<roomList를 표시하는 패널>>*/
		chattingRoomListPanel = new JPanel();
		chattingRoomListPanel.setLayout(null);
		chattingRoomListPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainCardPanel.add(chattingRoomListPanel, "chattingRoomListPanel");
		/*<방만들기 버튼>*/
		JButton createRoomButton = new JButton("방만들기");
		createRoomButton.setBounds(12, 5, 111, 59);
		createRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				String roomName = JOptionPane.showInputDialog(chattingRoomListPanel, "방제목을 입력하세요.");
				if(Objects.isNull(roomName)) {
					return;
				}
				if(roomName.isBlank()) {
					JOptionPane.showMessageDialog(chattingRoomListPanel, "방제목을 입력하세요.", "방만들기 실패", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				for(int i = 0; i < roomListModel.size(); i++) {
					if(roomListModel.get(i).equals(roomName)) {
						JOptionPane.showMessageDialog(chattingRoomListPanel, "이미 존재하는 방제목입니다.", "방만들기 실패", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("createRoom", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				roomNameLabel.setText("방이름: " + roomName);
				mainCardLayout.show(mainCardPanel, "chattingRoomPanel");
				requestBodyDto = new RequestBodyDto<String>("join", roomName);
				ClientSender.getInstance().send(requestBodyDto);
			}			
		});
		chattingRoomListPanel.add(createRoomButton);
		
		/*<roomList가 표시되는 곳>*/
		roomListScrollPanel = new JScrollPane();
		roomListScrollPanel.setBounds(10, 74, 549, 463);
		chattingRoomListPanel.add(roomListScrollPanel);
		
		/*<더블클릭했을때 방으로 입장하는 기능>*/
		roomListModel = new DefaultListModel<String>();
		roomList = new JList(roomListModel);
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					String roomName = roomListModel.get(roomList.getSelectedIndex());
					roomNameLabel.setText("방이름: " + roomName);
					mainCardLayout.show(mainCardPanel, "chattingRoomPanel");
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("join", roomName);
					ClientSender.getInstance().send(requestBodyDto);
				}
			}
		});
		roomListScrollPanel.setViewportView(roomList);
		/*<Label에 Client 본인의 이름표시>*/
		clientNameLabel = new JLabel();
		clientNameLabel.setBounds(116, 10, 133, 30);
		clientNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		chattingRoomListPanel.add(clientNameLabel);
		
		/*<웃음 이미지 삽입>*/
		userImageLabel = new JLabel();
		userImageLabel.setBounds(362, 5, 60, 41);
		ImageIcon imageIcon = new ImageIcon(System.getProperty("user.dir") + "\\images\\smile.png");
		// 크기 조정
		Image image = imageIcon.getImage();
		Image resizedImage = image.getScaledInstance(userImageLabel.getWidth(), userImageLabel.getHeight(), Image.SCALE_SMOOTH);  
		imageIcon = new ImageIcon(resizedImage);// 조정된 이미지로 다시 설정
		userImageLabel.setIcon(imageIcon);
		chattingRoomListPanel.add(userImageLabel);
		
		/*<<chattingRoomPanel 생성>>*/
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chattingRoomPanel.setLayout(null);
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");
	
		/*<Text 입력과 출력(Client간의 대화 표시)부분>*/
		JScrollPane chattingTextAreaScrollPanel = new JScrollPane();
		chattingTextAreaScrollPanel.setBounds(12, 10, 337, 468);
		chattingRoomPanel.add(chattingTextAreaScrollPanel);
		chattingTextArea = new JTextArea();
		chattingTextArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret) chattingTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		chattingTextAreaScrollPanel.setViewportView(chattingTextArea);
		
		/*<chattingRoomPanel의 방이름 표시>*/
		roomNameLabel = new JLabel();
		roomNameLabel.setHorizontalAlignment(JLabel.CENTER);
		chattingTextAreaScrollPanel.setColumnHeaderView(roomNameLabel);
		
		chattingTextArea = new JTextArea();
		chattingTextArea.setFont(new Font("맑은 고딕", Font.BOLD, 30));
		chattingTextAreaScrollPanel.setViewportView(chattingTextArea);
		chattingTextArea.setEditable(false);
		
		/*<메세지 보내는 곳>*/
		messageTextField = new JTextField();
		messageTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					                     
					SendMessage sendmessage = SendMessage.builder().fromUsername(username).toUsername(sendListLabel.getText()).messageBody(messageTextField.getText()).build();
					
					RequestBodyDto<SendMessage> requestBodyDto = new RequestBodyDto<>("sendMessage", sendmessage);
					
					ClientSender.getInstance().send(requestBodyDto);
					messageTextField.setText("");
					sendListLabel.setText("전체");
				}
			}
		});
		messageTextField.setBounds(101, 488, 458, 49);
		chattingRoomPanel.add(messageTextField);
		messageTextField.setColumns(10);
		
		/*<접속자 목록 표시>*/
		userListScrollPane = new JScrollPane();
		userListScrollPane.setBounds(361, 50, 198, 428);
		chattingRoomPanel.add(userListScrollPane);
		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		userList.setFont(new Font("맑은 고딕", Font.PLAIN, 25));
		userListScrollPane.setViewportView(userList);
		userList.setCellRenderer(new ClientNameBoldRenderer(userListModel));
		/*<접속자 중 메세지를 보낼 상대를 선택>*/
		userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					String userName = userListModel.get(userList.getSelectedIndex());						
					
					if(userList.getSelectedIndex() == 0) {	
						userName = userName.substring(0, userName.length() - 4);
					}
					
					sendListLabel.setText(userName);
				}
			}
		});
		
		/*<접속자 중 메세지를 보낼 상대를 선택>*/
		/*<나가기 버튼>*/
		exitButton = new JButton("나가기");
		exitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("leave", null);				
				ClientSender.getInstance().send(requestBodyDto);
			}
		});
		exitButton.setBounds(459, 7, 100, 33);
		chattingRoomPanel.add(exitButton);
		
		/*<보낼사람 선택>*/
		sendListLabel = new JLabel();
		sendListLabel.setText("전체");
		sendListLabel.setBounds(12, 488, 61, 49);
		chattingRoomPanel.add(sendListLabel);
		
		
		
	}


	
}
