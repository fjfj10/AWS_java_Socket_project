package socket_project_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import javax.swing.DefaultListModel;

import com.google.gson.Gson;

import socket_project_client.dto.RequestBodyDto;


public class ClientReceiver extends Thread{
	
	@Override
	public void run() {
		ProjectClient simpleGUIClient = ProjectClient.getInstance();
		while (true) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(simpleGUIClient.getSocket().getInputStream()));
				String requestBody = bufferedReader.readLine();
				
				requestController(requestBody);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			
		}
		
	}
	private void requestController(String requestBody) {
		Gson gson = new Gson();
		
		String resorce = gson.fromJson(requestBody, RequestBodyDto.class).getResource();
		switch (resorce) {
		
			case "updateRoomList":
				List<String> roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				ProjectClient.getInstance().getRoomListModel().clear();
				ProjectClient.getInstance().getRoomListModel().addAll(roomList);
				break;
		
			case "showMessage":
				String messageContent = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				ProjectClient.getInstance().getChattingTextArea().append(messageContent + "\n"); 
				break;
			
			case "updateUserList" : 
				List<String> usernameList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
				DefaultListModel<String> userListModel = ProjectClient.getInstance().getUserListModel();
				userListModel.clear();
				userListModel.addAll(usernameList);
				userListModel.set(0, userListModel.get(0) + "(방장)");
				break;

		}
	}
	
	
}
