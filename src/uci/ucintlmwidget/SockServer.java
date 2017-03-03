package uci.ucintlmwidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SockServer {

	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static InputStreamReader inputStreamReader;
	private static BufferedReader bufferedReader;
	private static String message;

	public SockServer() {

		try {
			serverSocket = new ServerSocket(4444); // Server socket

		} catch (IOException e) {
			System.out.println("Could not listen on port: 4444");
		}

		System.out.println("Server started. Listening to the port 4444");

		while (true) {
			try {

				clientSocket = serverSocket.accept(); // accept the client
														// connection
				inputStreamReader = new InputStreamReader(
						clientSocket.getInputStream());
				bufferedReader = new BufferedReader(inputStreamReader); // get
																		// the
																		// client
																		// message
				message = bufferedReader.readLine();
				while (message.length() > 0) {
					System.out.println(message);
					message = bufferedReader.readLine();

				}

				inputStreamReader.close();
				clientSocket.close();

			} catch (IOException ex) {
				System.out.println("Problem in message reading");
			}
		}

	}
}
