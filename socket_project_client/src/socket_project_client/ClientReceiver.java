package socket_project_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import com.google.gson.Gson;

import socket_project_client.dto.RequestBodyDto;


public class ClientReceiver extends Thread{
	
	private Gson gson;
	
	@Override
	public void run() {
		gson = new Gson();
		
		ProjectClient projectClient = ProjectClient.getInstance();
		while (true) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(projectClient.getSocket().getInputStream()));
				String requestBody = bufferedReader.readLine();
				
				requestController(requestBody);
				
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		
			
		}
		
	}
	
	private void requestController(String requestBody) {
		String resorce = gson.fromJson(requestBody, RequestBodyDto.class).getResource();
		switch (resorce) {
			case "checkUserName":
				checkUserName(requestBody);
				break;
				
			case "updateRoomList":
				updateRoomList(requestBody);
				break;
		
			case "showMessage":
				showMessage(requestBody);
				break;
			
			case "updateUserList":
				updateUserList(requestBody);
				break;
			
			case "leave":
				leave(requestBody);
		}
	}
	
	private void checkUserName(String requestBody) {
		boolean isUsernameDuplicated = (boolean) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		if(isUsernameDuplicated == true) {
			JOptionPane.showMessageDialog(null, "중복된 ID입니다.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}else {
			JOptionPane.showMessageDialog(null, "사용 가능한 ID입니다.", "", JOptionPane.PLAIN_MESSAGE);
			
		}
	}
	
	private void updateRoomList(String requestBody) {
		String searchCondition = ProjectClient.getInstance().getSearchRoomTextField().getText();
		List<String> roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		if(!searchCondition.equals("")) {
			roomList = roomList.stream().filter((room) -> {
				return room.contains(searchCondition);
			}).collect(Collectors.toList());					
		}
		
		ProjectClient.getInstance().getRoomListModel().clear();
		ProjectClient.getInstance().getRoomListModel().addAll(roomList);
	}
	
	private void showMessage(String requestBody) {
		String messageContent = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		ProjectClient.getInstance().getChattingTextArea().append(messageContent + "\n");
	}
	
	private void updateUserList(String requestBody) {
		List<String> usernameList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		DefaultListModel<String> userListModel = ProjectClient.getInstance().getUserListModel();
		userListModel.clear();
		userListModel.addAll(usernameList);
		userListModel.set(0, userListModel.get(0) + "(방장)");
	}
	
	private void leave(String requestBody) {
		ProjectClient projectClient = ProjectClient.getInstance();
		projectClient.getMainCardLayout().show(projectClient.getMainCardPanel(), "chattingRoomListPanel");
		ProjectClient.getInstance().getChattingTextArea().setText("");
	}
}