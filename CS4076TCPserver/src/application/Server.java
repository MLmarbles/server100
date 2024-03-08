package application;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
	private static ServerSocket servSock;
	private static final int PORT = 9523;
	private static ArrayList<Module> modules = new ArrayList<>();

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
				case "DISPLAY_SCHEDULE":
					String moduleCodeToDisplay = data;
					Module moduleToDisplay = findModule(moduleCodeToDisplay);
					StringBuilder schedule = new StringBuilder();

					if (moduleToDisplay != null) {
						schedule.append("Schedule for Module: ").append(moduleCodeToDisplay);
						for (Class classObj : moduleToDisplay.getClasses()) {
							schedule.append(" [");
							schedule.append(classObj.toString());
							schedule.append("]");
						}
						out.println(schedule.toString());

					} else {
						out.println("Error: Module " + moduleCodeToDisplay + " not found.");
					}
					break;

				case "ADD_MODULE":
					if (modules.size() < 5) {
						String moduleCode = data;
						// Check if the module is already added
						if (!isModuleAdded(moduleCode)) {
							Module module = new Module(moduleCode);
							modules.add(module);
							out.println(moduleCode + " Module added successfully.");
						} else {
							out.println(moduleCode + " Module already added.");
						}
					} else {
						out.println("Error: Maximum modules reached. You cannot add more modules.");
					}
					break;
				case "REMOVE_MODULE":
					String moduleCodeToRemove = data;
					boolean removed = removeModule(moduleCodeToRemove);
					if (removed) {
						out.println((moduleCodeToRemove + " Module removed successfully."));
					} else {
						out.println("Error: " + moduleCodeToRemove + " Module not found.");
					}
					break;
				case "ADD_CLASS":
					String classModuleToAdd = data;
					String classData = parts[2];

					String[] classParts = classData.split(", ");
					if (classParts.length == 4) { // Ensure that all parts are present
						String day = classParts[0];
						String start = classParts[1];
						String end = classParts[2];
						String room = classParts[3];

						// Check for clashing times
						Module moduleToAddClass = findModule(classModuleToAdd);
						if (moduleToAddClass != null) {
							if (!isTimeClash(moduleToAddClass, day, start, end)) {
								Class newClass = new Class(day, start, end, room);
								moduleToAddClass.addClass(newClass);
								out.println("Class added to module " + classModuleToAdd + " successfully.");
							} else {
								out.println("Error: This module clashes with another class: " + classModuleToAdd + ".");
							}
						} else {
							out.println("Error: Module " + classModuleToAdd + " not found.");
						}
					} else {
						out.println(
								"Error: Invalid class information format. Please provide information in the format: day, start time, end time, room.");
					}
					break;

				case "REMOVE_CLASS":
					String classModuleToRemove = data;
					String classDataToRemove = parts[2];

					String[] classPartsToRemove = classDataToRemove.split(", ");
					if (classPartsToRemove.length == 4) { // Ensure that all parts are present
						String day = classPartsToRemove[0];
						String start = classPartsToRemove[1];
						String end = classPartsToRemove[2];
						String room = classPartsToRemove[3];

						Module classModuleToRemoveClass = findModule(classModuleToRemove);
						if (classModuleToRemoveClass != null) {
							// Search for the class within the module
							Class classToRemove = null;
							for (Class classObj : classModuleToRemoveClass.getClasses()) {
								if (classObj.getDay().equals(day) && classObj.getStart().equals(start)
										&& classObj.getEnd().equals(end) && classObj.getRoom().equals(room)) {
									classToRemove = classObj;
									break;
								}
							}

							if (classToRemove != null) {
								// Remove the class from the module
								boolean removedClass = classModuleToRemoveClass.removeClass(classToRemove);
								if (removedClass) {
									out.println("Class removed from module " + classModuleToRemove + " successfully.");
									// Reply with freed time slot and room information
									out.println(
											"Freed time slot: " + day + ", " + start + " - " + end + ", Room: " + room);
								} else {
									out.println("Error: Failed to remove class from module " + classModuleToRemove);
								}
							} else {
								out.println("Error: Class not found in module " + classModuleToRemove);
							}
						} else {
							out.println("Error: Module " + classModuleToRemove + " not found.");
						}
					} else {
						out.println(
								"Error: Invalid class information format. Please provide information in the format: day, start time, end time, room.");
					}
					break;
				case "STOP":
					out.println("TERMINATE");
					link.close();
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

	private static boolean removeModule(String moduleCodeToRemove) {
		for (Module module : modules) {
			if (module.getModuleCode().equals(moduleCodeToRemove)) {
				modules.remove(module);
				return true;
			}
		}
		return false;
	}

	private static Module findModule(String moduleCode) {
		for (Module module : modules) {
			if (module.getModuleCode().equals(moduleCode)) {
				return module;
			}
		}
		return null;
	}

	private static boolean isTimeClash(Module module, String day, String start, String end) {
		for (Class classObj : module.getClasses()) {
			if (classObj.getDay().equals(day)) {
				if (isOverlap(classObj.getStart(), classObj.getEnd(), start, end)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isOverlap(String start1, String end1, String start2, String end2) {
		int start1Min = parseTimeToMinutes(start1);
		int end1Min = parseTimeToMinutes(end1);
		int start2Min = parseTimeToMinutes(start2);
		int end2Min = parseTimeToMinutes(end2);

		return (start1Min < end2Min && start2Min < end1Min);
	}

	private static int parseTimeToMinutes(String time) {
		String[] parts = time.split(":");
		int hours = Integer.parseInt(parts[0]);
		int minutes = Integer.parseInt(parts[1]);
		return hours * 60 + minutes;
	}

}
