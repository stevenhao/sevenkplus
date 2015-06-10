package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) {
		int portNumber = args.length >= 1 ? Integer.parseInt(args[0]) : 5000;
		try {			
			System.out.println("started");
			ServerSocket serverSocket = new ServerSocket(portNumber);
			System.out.println("got halfway");
			Socket clientSocket = serverSocket.accept();
			PrintWriter out = new PrintWriter(
					clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			System.out.println("alive");
			out.println("Sup Client.");
			out.flush();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
