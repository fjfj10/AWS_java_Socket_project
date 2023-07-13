package socket_project_client;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

import socket_project_client.dto.RequestBodyDto;

public class ClientSender {
	
	private Gson gson;
	
	private static ClientSender instance;
	
	private ClientSender() {
		gson = new Gson();
	}
	
	public static ClientSender getInstance() {
		if(instance == null) {
			instance = new ClientSender();
		}
		return instance;
	}
	
	public void send(RequestBodyDto<?> requestBodyDto) {
		try {
			PrintWriter printWriter = new PrintWriter(ProjectClient.getInstance().getSocket().getOutputStream(), true);
			printWriter.println(gson.toJson(requestBodyDto));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
