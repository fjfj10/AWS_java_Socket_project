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
				e.printStackTrace();
			}

		}

	}

	private void requestController(String requestBody) {

		String resorce = gson.fromJson(requestBody, RequestBodyDto.class).getResource();

		switch (resorce) {
			case "connection":
				connection(requestBody);
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

	private void connection(String requestBody) {
		username = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();

		List<String> roomNameList = new ArrayList<>();

		ProjectServer.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});

		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = new RequestBodyDto<List<String>>("updateRoomList",
				roomNameList);

		ProjectServerSender.getInstance().send(socket, updateRoomListRequestBodyDto);
	}

	private void createRoom(String requestBody) {
		
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();

		Room newRoom = Room.builder().roomName(roomName).owner(username).userList(new ArrayList<ConnectedSocket>())
				.build();

		ProjectServer.roomList.add(newRoom);
		List<String> roomNameList = new ArrayList<>();

		ProjectServer.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});

		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = new RequestBodyDto<List<String>>("updateRoomList",
				roomNameList);
		
		ProjectServer.connectedSocketList.forEach(con -> {
			ProjectServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
		});

	}

	private void join(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		ProjectServer.roomList.forEach(room -> {

			if (room.getRoomName().equals(roomName)) {
				room.getUserList().add(this);

				List<String> usernameList = new ArrayList<>();

				room.getUserList().forEach(con -> {
					usernameList.add(con.username);
				});

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
		System.out.println(sendMessage);
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
				if(!room.getOwner().equals(this.username)) {
					room.getUserList().remove(this);
					
					List<String> usernameList = new ArrayList<>();					
					
					room.getUserList().forEach(con -> {
						usernameList.add(con.username);
					});					
					
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
					ProjectServerSender.getInstance().send(this.socket, leaveDto);
					
				}else {
					ProjectServer.roomList.remove(room);
					List<String> roomNameList = new ArrayList<>();
					ProjectServer.roomList.forEach(room3 -> {
						roomNameList.add(room3.getRoomName());
					});
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
}