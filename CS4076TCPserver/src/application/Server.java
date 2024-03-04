package application;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private static ServerSocket servSock;
    private static final int PORT = 1234;
    private static int clientConnections = 0;
    
    public static HashMap<Integer, ArrayList<Module>> serverModules;
    private static Queries queries; // Declare a variable to hold an instance of the Queries class
    private static String controllerUsername;
    private static int userId;

    public static void main(String[] args) throws SQLException {
        queries = new Queries(); // Initialize the Queries instance
        queries.q1();
        System.out.println("Opening port...\n");
        try {
            servSock = new ServerSocket(PORT);
                // Handle each client connection in a separate thread
        } catch (IOException e) {
            System.out.println("Unable to attach to port!");
            System.exit(1);
        }
        do {
        	handleClientRequest();
        } while (true);
    }

    private static void initializeClassData() {
        try {
            serverModules = queries.getModulesWithData(controllerUsername);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientRequest() {
    	Socket link = null;
        try {
        		link = servSock.accept();
        		clientConnections++;
            PrintWriter out = new PrintWriter(link.getOutputStream(),true); 
            BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
        		{
            String request = in.readLine();
            switch (request) {
            	case "TRANSFER_USERNAME":
            		String user = in.readLine();
            		controllerUsername = user;
            		userId = queries.getUserIdByUsername(user);
            		initializeClassData();
            		break;
                case "CHECK_USER_EXISTENCE":
                    String usernameExistence = in.readLine(); // Rename variable to avoid duplicate
                    boolean userExists = queries.userExists(usernameExistence);
                    out.println(userExists);
                    break;
                case "CHECK_LOGIN_MATCH":
                    String usernameLogin = in.readLine(); // Rename variable to avoid duplicate
                    String password = in.readLine();
                    boolean loginMatch = queries.loginMatch(usernameLogin, password);
                    out.println(loginMatch);
                    break;
                case "REQUEST_MAP":
                    // Send the serverModules HashMap to the client
                	out.println(userId);
                    out.println(serializeModules(serverModules));
                    break;
                case "INSERT_USER":
                	String userToInsert = in.readLine();
                	String passwordToInsert = in.readLine();
                	queries.insertUser(userToInsert, passwordToInsert);
                case "GET_USER_ID_BY_USERNAME":
                	
                	
                // Handle other requests if needed
            }
        		}
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static String serializeModules(HashMap<Integer, ArrayList<Module>> modules) {
        StringBuilder serializedModules = new StringBuilder();
        
        // Iterate through the HashMap and serialize each module
        for (ArrayList<Module> moduleList : modules.values()) {
            for (Module module : moduleList) {
                serializedModules.append(module.serialize()).append(";"); // Serialize each module
            }
        }
        
        return serializedModules.toString();
    }
}