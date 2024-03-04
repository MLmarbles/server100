package application;

public class Class {
    private String day;
    private String start;
    private String end;
    private String room;
    private int userId;

    public Class(String day, String start, String end, String room, int userId) {
        this.day = day;
        this.start = start;
        this.end = end;
        this.room = room;
        this.userId = userId;
    }

    public String getDay() {
        return day;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getRoom() {
        return room;
    }

    public int getUserId() {
        return userId;
    }
    
    // Custom serialization method
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(day).append(";") // Append day
          .append(start).append(";") // Append start time
          .append(end).append(";") // Append end time
          .append(room).append(";") // Append room
          .append(userId); // Append userId
        
        return sb.toString();
    }
    
    // Custom deserialization method
    public static Class deserialize(String data) {
        String[] parts = data.split(";");
        String day = parts[0];
        String start = parts[1];
        String end = parts[2];
        String room = parts[3];
        int userId = Integer.parseInt(parts[4]);
        
        return new Class(day, start, end, room, userId);
    }
}
