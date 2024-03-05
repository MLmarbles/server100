package application;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
	private static ServerSocket servSock;
	private static final int PORT = 1238;
	private static List<Module> modules = new ArrayList<>();

	public static void main(String[] args) throws SQLException {
		System.out.println("Opening port...\n");
		try {
			servSock = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Unable to attach to port!");
			System.exit(1);
		}
		do {
			handleClientRequest();
		} while (true);
	}

	private static void handleClientRequest() {
		Socket link = null;
		try {
			link = servSock.accept();
			PrintWriter out = new PrintWriter(link.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));

			String request = in.readLine();
			String[] parts = request.split(" ", 3);

			if (parts.length >= 2) {
				String command = parts[0];
				String data = parts[1];

				switch (command) {
				case "GET_MODULES":
					// Construct a string with module names separated by commas
					StringBuilder modulesList = new StringBuilder();
					for (Module module : modules) {
						modulesList.append(module.getModuleCode()).append(",");
					}
					// Remove the trailing comma
					if (modulesList.length() > 0) {
						modulesList.deleteCharAt(modulesList.length() - 1);
					}
					// Send the list of modules to the client
					out.println(modulesList.toString());
					break;

				case "ADD_MODULE":
					if (modules.size() < 5) {
						String moduleCode = data;
						// Check if the module is already added
						if (!isModuleAdded(moduleCode)) {
							Module module = new Module(moduleCode);
							modules.add(module);
							out.println("Module added successfully.");
						} else {
							out.println("Module already added.");
						}
					} else {
						out.println("Maximum modules reached. You cannot add more modules.");
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isModuleAdded(String moduleCode) {
		for (Module module : modules) {
			if (module.getModuleCode().equals(moduleCode)) {
				return true;
			}
		}
		return false;
	}
}
