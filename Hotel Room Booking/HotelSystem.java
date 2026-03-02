import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class HotelSystem {

    

    enum RoomType {
        STANDARD, DELUXE, SUITE
    }

    enum BookingStatus {
        CONFIRMED, CANCELLED
    }

    

    static class Room {
        int roomId;
        RoomType type;
        double price;
        boolean isAvailable;

        Room(int roomId, RoomType type, double price, boolean isAvailable) {
            this.roomId = roomId;
            this.type = type;
            this.price = price;
            this.isAvailable = isAvailable;
        }

        @Override
        public String toString() {
            return roomId + "," + type + "," + price + "," + isAvailable;
        }
    }

    

    static class Booking {
        int bookingId;
        String userName;
        int roomId;
        LocalDate checkIn;
        LocalDate checkOut;
        BookingStatus status;

        Booking(int bookingId, String userName, int roomId,
                LocalDate checkIn, LocalDate checkOut,
                BookingStatus status) {
            this.bookingId = bookingId;
            this.userName = userName;
            this.roomId = roomId;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.status = status;
        }

        @Override
        public String toString() {
            return bookingId + "," + userName + "," + roomId + ","
                    + checkIn + "," + checkOut + "," + status;
        }
    }



    static class RoomService {

        static final String ROOM_FILE = "rooms.txt";

        public List<Room> getAllRooms() throws Exception {
            List<Room> rooms = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(ROOM_FILE));
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                rooms.add(new Room(
                        Integer.parseInt(parts[0]),
                        RoomType.valueOf(parts[1]),
                        Double.parseDouble(parts[2]),
                        Boolean.parseBoolean(parts[3])
                ));
            }
            br.close();
            return rooms;
        }

        public void saveRooms(List<Room> rooms) throws Exception {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ROOM_FILE));
            for (Room room : rooms) {
                bw.write(room.toString());
                bw.newLine();
            }
            bw.close();
        }

        public List<Room> searchAvailableRooms(RoomType type) throws Exception {
            List<Room> available = new ArrayList<>();
            for (Room room : getAllRooms()) {
                if (room.type == type && room.isAvailable) {
                    available.add(room);
                }
            }
            return available;
        }
    }

    static class BookingService {

        static final String BOOKING_FILE = "bookings.txt";
        RoomService roomService = new RoomService();

        public int generateBookingId() {
            return new Random().nextInt(100000);
        }

        public boolean simulatePayment(double amount) {
            System.out.println("Processing payment of ₹" + amount + "...");
            return Math.random() > 0.2; // 80% success
        }

        public void createBooking(String userName, int roomId,
                                  LocalDate checkIn, LocalDate checkOut) throws Exception {

            List<Room> rooms = roomService.getAllRooms();
            Room selectedRoom = null;

            for (Room room : rooms) {
                if (room.roomId == roomId && room.isAvailable) {
                    selectedRoom = room;
                    break;
                }
            }

            if (selectedRoom == null) {
                System.out.println("Room not available!");
                return;
            }

            double totalAmount = selectedRoom.price *
                    (checkOut.toEpochDay() - checkIn.toEpochDay());

            if (!simulatePayment(totalAmount)) {
                System.out.println("Payment Failed!");
                return;
            }

            selectedRoom.isAvailable = false;
            roomService.saveRooms(rooms);

            Booking booking = new Booking(
                    generateBookingId(),
                    userName,
                    roomId,
                    checkIn,
                    checkOut,
                    BookingStatus.CONFIRMED
            );

            BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKING_FILE, true));
            bw.write(booking.toString());
            bw.newLine();
            bw.close();

            System.out.println("Booking Successful! Booking ID: " + booking.bookingId);
        }

        public void cancelBooking(int bookingId) throws Exception {
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(BOOKING_FILE));
            String line;
            int roomId = -1;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (Integer.parseInt(parts[0]) == bookingId &&
                        parts[5].equals("CONFIRMED")) {

                    roomId = Integer.parseInt(parts[2]);
                    line = parts[0] + "," + parts[1] + "," + parts[2] + ","
                            + parts[3] + "," + parts[4] + ",CANCELLED";
                }
                lines.add(line);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKING_FILE));
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
            bw.close();

            if (roomId != -1) {
                List<Room> rooms = roomService.getAllRooms();
                for (Room r : rooms) {
                    if (r.roomId == roomId) {
                        r.isAvailable = true;
                    }
                }
                roomService.saveRooms(rooms);
                System.out.println("Booking Cancelled Successfully!");
            } else {
                System.out.println("Booking not found.");
            }
        }

        public void viewBooking(int bookingId) throws Exception {
            BufferedReader br = new BufferedReader(new FileReader(BOOKING_FILE));
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (Integer.parseInt(parts[0]) == bookingId) {
                    System.out.println("----- Booking Details -----");
                    System.out.println("Booking ID: " + parts[0]);
                    System.out.println("User: " + parts[1]);
                    System.out.println("Room ID: " + parts[2]);
                    System.out.println("Check-In: " + parts[3]);
                    System.out.println("Check-Out: " + parts[4]);
                    System.out.println("Status: " + parts[5]);
                }
            }
            br.close();
        }
    }



    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        RoomService roomService = new RoomService();
        BookingService bookingService = new BookingService();

        
        File roomFile = new File("rooms.txt");
        if (!roomFile.exists()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(roomFile));
            bw.write("1,STANDARD,2000,true\n");
            bw.write("2,DELUXE,3500,true\n");
            bw.write("3,SUITE,6000,true\n");
            bw.close();
        }

        while (true) {
            System.out.println("\n1. Search Room");
            System.out.println("2. Book Room");
            System.out.println("3. Cancel Booking");
            System.out.println("4. View Booking");
            System.out.println("5. Exit");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.println("Enter Room Type (STANDARD/DELUXE/SUITE): ");
                    RoomType type = RoomType.valueOf(sc.next().toUpperCase());
                    List<Room> rooms = roomService.searchAvailableRooms(type);
                    for (Room r : rooms) {
                        System.out.println("Room ID: " + r.roomId + " Price: ₹" + r.price);
                    }
                    break;

                case 2:
                    sc.nextLine();
                    System.out.println("Enter Name: ");
                    String name = sc.nextLine();
                    System.out.println("Enter Room ID: ");
                    int roomId = sc.nextInt();
                    System.out.println("Enter Check-In Date (YYYY-MM-DD): ");
                    LocalDate checkIn = LocalDate.parse(sc.next());
                    System.out.println("Enter Check-Out Date (YYYY-MM-DD): ");
                    LocalDate checkOut = LocalDate.parse(sc.next());
                    bookingService.createBooking(name, roomId, checkIn, checkOut);
                    break;

                case 3:
                    System.out.println("Enter Booking ID: ");
                    bookingService.cancelBooking(sc.nextInt());
                    break;

                case 4:
                    System.out.println("Enter Booking ID: ");
                    bookingService.viewBooking(sc.nextInt());
                    break;

                case 5:
                    System.exit(0);
            }
        }
    }
}