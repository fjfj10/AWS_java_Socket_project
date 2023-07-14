package socket_project_client;
import java.awt.CardLayout;
import java.awt.EventQueue;
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

@Getter
public class ProjectClient extends JFrame {
	//싱글톤 쓴이유 : ClientReceiver에서 SimpleGUIClient안의 메소드들을 사용하고 싶어서
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
	private JButton ExitButton;
	private JLabel SendListLabel;
	private JLabel ClientNameLabel;
	private JLabel roomNameLabel;
			

	/*GUIClient 생성*/
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProjectClient frame = ProjectClient.getInstance();
					frame.setVisible(true);
										
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
			socket = new Socket("127.0.0.1", 8000);          //127.0.0.1은 로컬주소의 변수 같은거 자신의 주소를 불러옴
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 286);

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
		createRoomButton.setBounds(10, 10, 100, 30);
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
				/**/
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
		roomListScrollPanel.setBounds(10, 50, 414, 201);
		chattingRoomListPanel.add(roomListScrollPanel);
		/*더블클릭했을때 방으로 입장하는 기능*/
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
		/*Label에 Client 본인의 접속표시, 이름표시*/
		ClientNameLabel = new JLabel();
		ClientNameLabel.setText("<< 접속자: "+ username + " >>");
		ClientNameLabel.setBounds(116, 10, 158, 30);
		chattingRoomListPanel.add(ClientNameLabel);
		
		
		
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chattingRoomPanel.setLayout(null);
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");
	
		/*<<Text 입력과 출력(Client간의 대화 표시)부분>>*/
		JScrollPane chattingTextAreaScrollPanel = new JScrollPane();
		chattingTextAreaScrollPanel.setBounds(12, 10, 298, 188);
		chattingRoomPanel.add(chattingTextAreaScrollPanel);
		
		chattingTextArea = new JTextArea();
		chattingTextAreaScrollPanel.setViewportView(chattingTextArea);
		
		/*chattingRoomPanel의 방이름 표시*/
		roomNameLabel = new JLabel();
		roomNameLabel.setHorizontalAlignment(JLabel.CENTER);
		chattingTextAreaScrollPanel.setColumnHeaderView(roomNameLabel);
		
		messageTextField = new JTextField();
		messageTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					                     
					SendMessage sendmessage = SendMessage.builder().fromUsername(username).toUsername(SendListLabel.getName()).messageBody(messageTextField.getText()).build();
					
					RequestBodyDto<SendMessage> requestBodyDto = new RequestBodyDto<>("SendMessage", sendmessage);
					
					ClientSender.getInstance().send(requestBodyDto);
					messageTextField.setText("");
				}
			}
		});
		messageTextField.setBounds(68, 208, 354, 31);
		chattingRoomPanel.add(messageTextField);
		messageTextField.setColumns(10);
		
		/*<<접속자 목록 표시>>*/
		userListScrollPane = new JScrollPane();
		userListScrollPane.setBounds(322, 50, 100, 148);
		chattingRoomPanel.add(userListScrollPane);
		
		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		/*<접속자 중 메세지를 보낼 상대를 선택>*/
		userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					String userName = userListModel.get(userList.getSelectedIndex());						
					SendListLabel.setText(userName);
				}
			}
		});
		
		userListScrollPane.setViewportView(userList);
		
		ExitButton = new JButton("나가기");
		ExitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("leave", null);				
				ClientSender.getInstance().send(requestBodyDto);
			}
		});
		ExitButton.setBounds(322, 10, 100, 33);
		chattingRoomPanel.add(ExitButton);
		
		SendListLabel = new JLabel();
		SendListLabel.setBounds(12, 208, 51, 31);
		chattingRoomPanel.add(SendListLabel);
		
		
		
	}


	
}
