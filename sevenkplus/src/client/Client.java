package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	public static void main(String[] args) {
		int portNumber = args.length >= 1 ? Integer.parseInt(args[0]) : 5000;
		String hostName = "localhost";
		try {			
			System.out.println("connecting.");
		    Socket socket = new Socket(hostName, portNumber);
		    System.out.println("connected.");
		    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		    BufferedReader in = new BufferedReader(
		        new InputStreamReader(socket.getInputStream()));
		    System.out.println(in.readLine());
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}