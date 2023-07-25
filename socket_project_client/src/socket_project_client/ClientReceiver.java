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
//		if(isUsernameDuplicated == true) ->  if(isUsernameDuplicated) 논리형일때 == 쓰지말기
		if(isUsernameDuplicated) { //if문에 논리형을 넣으면 true일때만 if문 안의 명령을 실행함. false는 else 안의 명령 실행
			JOptionPane.showMessageDialog(null, "중복된 ID입니다.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
			return;
		}else {
			JOptionPane.showMessageDialog(null, "사용 가능한 ID입니다.", "", JOptionPane.PLAIN_MESSAGE);
			
		}
	}
	
	private void updateRoomList(String requestBody) {
		//searchCondition는 roomNameList를 Json으로 받음
		String searchCondition = ProjectClient.getInstance().getSearchRoomTextField().getText();
		List<String> roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		/*검색 조건(searchCondition)이 비어있지 않으면(equals("")), 방 목록을 해당 검색 조건으로 필터링 
		 * stream()을 사용하여 방 목록을 스트림으로 변환한 후, 
		 * filter()를 사용하여 검색 조건을 만족하는 방들로 필터링
		 * collect()를 사용하여 필터링된 방들을 다시 List로 변환
		 * 마지막으로, 프로젝트 클라이언트의 방 목록 모델을 업데이트*/ 
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

		ProjectClient.getInstance().getChattingTextArea().setText("");	//지금은 나갈때 TextArea를 초기화함 -> 들어갈때 해줘도 상관X
		ProjectClient.getInstance().getMessageTextField().setText("");	//방에서 나가면 MessageTextField 초기화 -> 들어갈때 해줘도 상관X

	}
}