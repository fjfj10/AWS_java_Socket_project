package socket_project_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import socket_project_server.entity.Room;

public class ProjectServer {

	public static List<ConnectedSocket> connectedSocketList = new ArrayList<>();
	public static List<Room> roomList = new ArrayList<>();           //roomList= 방들이 저장되는 리스트
	
	
	public static void main(String[] args) {
		
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			System.out.println("[서버 실행]");
			
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("접속");
				ConnectedSocket connectedSocket = new ConnectedSocket(socket);
				connectedSocket.start();
				connectedSocketList.add(connectedSocket);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
