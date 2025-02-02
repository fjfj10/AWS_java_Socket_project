package socket_project_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import socket_project_server.dto.RequestBodyDto;
import socket_project_server.dto.SendMessage;
import socket_project_server.entity.Room;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectedSocket extends Thread {

	private final Socket socket;
	private Gson gson;

	private String username;

	@Override
	public void run() {
		gson = new Gson();

		while (true) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String requestBody = bufferedReader.readLine();

				requestController(requestBody);

			} catch (IOException e) {
				leave();
				ProjectServer.connectedSocketList.remove(this);
				System.out.println(username + "님 접속 종료");
				return;
			}

		}

	}

	private void requestController(String requestBody) {

		String resorce = gson.fromJson(requestBody, RequestBodyDto.class).getResource();

		switch (resorce) {
			case "setUserName":
				setUserName(requestBody);
				break;
		
			case "getRoomNameList":
				getRoomNameList(requestBody);
				break;
	
			case "createRoom":
				createRoom(requestBody);
				break;
	
			case "join":
				join(requestBody);
				break;
	
			case "sendMessage":
				sendMessage(requestBody);
				break;
	
			case "leave":
				leave();
				break;
		}
	}
	 /**중복을 확인하고
	  * username 필드를 초기화함
	  */
	private void setUserName(String requestBody) {
		boolean isUsernameDuplicated = false;
		String inputUserName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		List<String> usernameList = new ArrayList<>();
		
		ProjectServer.connectedSocketList.forEach(con -> {
			usernameList.add(con.username);
		});
		
		if(usernameList.contains(inputUserName)) {
			isUsernameDuplicated = true;
		}
		RequestBodyDto<Boolean> requestBodyDto = new RequestBodyDto<>("checkUserName", isUsernameDuplicated);
		ProjectServerSender.getInstance().send(socket, requestBodyDto);
				
		username = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
	}
	/**
	 * 이 소켓에게 최신 방 정보를 보내줍니다 (READ -> roomList)
	 */
	private void getRoomNameList(String requestBody) {
		
		List<String> roomNameList = getRoomNameList(ProjectServer.roomList);

		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = new RequestBodyDto<List<String>>("updateRoomList",
				roomNameList);

		ProjectServerSender.getInstance().send(socket, updateRoomListRequestBodyDto);
	}

	/**
	 * 1. 방 목록에 새로운 방을 하나 추가합니다 (CREATE -> roomList)
	 * 2. 모든 소켓에게 최신 방 정보를 보내줍니다 (READ -> roomList)
	 */
	private void createRoom(String requestBody) {
		
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();

		Room newRoom = Room.builder().roomName(roomName).owner(username).userList(new ArrayList<ConnectedSocket>())
				.build();

		ProjectServer.roomList.add(newRoom);
		List<String> roomNameList = getRoomNameList(ProjectServer.roomList);

		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = new RequestBodyDto<List<String>>("updateRoomList",
				roomNameList);
		
		ProjectServer.connectedSocketList.forEach(con -> {
			ProjectServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
		});

	}

	/**
	 * roomName을 입력받아 -> 일치하는 방이 있으면 해당 방의 유저목록에 이 ConnectedSocket을 기록합니다 (ADD -> roomList.room.userList)
	 * 이 방에 접속한 유저들에게
	 * -> 이 방에 접속한 유저들의 이름을 전달하고 (READ -> roomList.room)
	 * -> 새로 들어온 유저의 환영 메시지를 보냅니다
	 */
	private void join(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		ProjectServer.roomList.forEach(room -> {

			if (room.getRoomName().equals(roomName)) {
				room.getUserList().add(this);

				List<String> usernameList = getUserNameList(room.getUserList());

				room.getUserList().forEach(connectedSocket -> {
					RequestBodyDto<List<String>> updateUserListDto = new RequestBodyDto<List<String>>("updateUserList",
							usernameList);
					RequestBodyDto<String> joinMessageDto = new RequestBodyDto<String>("showMessage",
							username + "님이 들어왔습니다.");

					ProjectServerSender.getInstance().send(connectedSocket.socket, updateUserListDto);
					sleep(100);
					ProjectServerSender.getInstance().send(connectedSocket.socket, joinMessageDto);
				});
			}
		});
	}
	
	private void sendMessage(String requestBody) {
		TypeToken<RequestBodyDto<SendMessage>> typeToken = new TypeToken<RequestBodyDto<SendMessage>>() {
		};

		RequestBodyDto<SendMessage> requestBodyDto = gson.fromJson(requestBody, typeToken.getType());
		SendMessage sendMessage = requestBodyDto.getBody();
		ProjectServer.roomList.forEach(room -> {
			if (room.getUserList().contains(this)) {
				if(sendMessage.getToUsername().equals("전체")) {
					room.getUserList().forEach(con -> {
						RequestBodyDto<String> dto = new RequestBodyDto<String>("showMessage", sendMessage.getFromUsername() + ": " + sendMessage.getMessageBody());
						ProjectServerSender.getInstance().send(con.socket, dto);
					});
				
				}else {					
					room.getUserList().forEach(con -> {
						if(con.username.equals(sendMessage.getToUsername())) {
							RequestBodyDto<String> senddto = new RequestBodyDto<String>("showMessage","[" + sendMessage.getToUsername() + "]에게 귓속말: " 
									+ sendMessage.getMessageBody());
							RequestBodyDto<String> receivedto = new RequestBodyDto<String>("showMessage","[" + sendMessage.getFromUsername() + "]의 귓속말: " 
									+ sendMessage.getMessageBody());
							ProjectServerSender.getInstance().send(socket, senddto);
							ProjectServerSender.getInstance().send(con.socket, receivedto);
						}
					});											
				}
			}
		});
	}

	private void leave() {
		for(int i = 0; i < ProjectServer.roomList.size(); i++) {
			Room room = ProjectServer.roomList.get(i);
			
			if(room.getUserList().contains(this)) {
				if(!room.getOwner().equals(username)) {
					room.getUserList().remove(this);
					
					List<String> usernameList = getUserNameList(room.getUserList());				
					
					RequestBodyDto<List<String>> updateUserListDto = new RequestBodyDto<List<String>>("updateUserList", usernameList);
					RequestBodyDto<String> leaveMessageDto = new RequestBodyDto<String>("showMessage", username + "님이 나갔습니다.");
					
					room.getUserList().forEach(connectedSocket -> {
						ProjectServerSender.getInstance().send(connectedSocket.socket, updateUserListDto);
						sleep(10);
						ProjectServerSender.getInstance().send(connectedSocket.socket, leaveMessageDto);
						sleep(10);
					});
					
					sleep(100);
		
					RequestBodyDto<String> leaveDto = new RequestBodyDto<String>("leave", null);
					ProjectServerSender.getInstance().send(socket, leaveDto);
					
				} else {
					ProjectServer.roomList.remove(room);
					List<String> roomNameList = getRoomNameList(ProjectServer.roomList);

					RequestBodyDto<List<String>> updateRoomListRequestBodyDto = new RequestBodyDto<List<String>>("updateRoomList",
							roomNameList);
					ProjectServer.connectedSocketList.forEach(con -> {
						ProjectServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
					});
					sleep(10);
					room.getUserList().forEach(connectedSocket -> {
						RequestBodyDto<String> leaveDto = new RequestBodyDto<String>("leave", null);
						ProjectServerSender.getInstance().send(connectedSocket.socket, leaveDto);
					});
				}
				
			}
		}
	}
	
	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private List<String> getRoomNameList(List<Room> roomList) {
		List<String> roomNameList = new ArrayList<String>();
		
		roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});
		
		return roomNameList;
	}
	
	private List<String> getUserNameList(List<ConnectedSocket> userList) {
		List<String> userNameList = new ArrayList<>();
		
		userList.forEach(con -> {
			userNameList.add(con.username);
		});
		
		return userNameList;
	}
}