package socket_project_server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

import socket_project_server.dto.RequestBodyDto;

public class ProjectServerSender {
	
	private Gson gson;
	
	private static ProjectServerSender instance;
	
	private ProjectServerSender() {
		gson = new Gson();
	}
	
	public static ProjectServerSender getInstance() {
		if(instance == null) {
			instance = new ProjectServerSender();
		}
		return instance;
	}
	
	public void send(Socket socket, RequestBodyDto<?> requestBodyDto) {
		try {
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
			printWriter.println(gson.toJson(requestBodyDto));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
