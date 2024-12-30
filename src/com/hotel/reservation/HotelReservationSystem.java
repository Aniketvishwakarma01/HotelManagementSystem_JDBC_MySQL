package com.hotel.reservation;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HotelReservationSystem {

    private static final String url = "jdbc:mysql://localhost:3306/hotel-db"; // Corrected URL
    private static final String username = "root";
    private static final String password = "Root";

    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
            return;
        }

        try (Connection con = DriverManager.getConnection(url, username, password)) {
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservations");
                System.out.println("5. Delete Reservations");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        reserveRoom(con, sc);
                        break;
                    case 2:
                        viewReservations(con);
                        break;
                    case 3:
                        getRoomNumber(con, sc);
                        break;
                    case 4:
                        updateReservation(con, sc);
                        break;
                    case 5:
                        deleteReservation(con, sc);
                        break;
                    case 0:
                        exit();
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("System interrupted: " + e.getMessage());
        }
    }

    private static void reserveRoom(Connection con, Scanner sc) {
        try {
            System.out.print("Enter guest name: ");
            String guestName = sc.next();
            sc.nextLine();
            System.out.print("Enter room number: ");
            int roomNumber = sc.nextInt();
            System.out.print("Enter contact number: ");
            String contactNumber = sc.next();

            String sql = "INSERT INTO reservations(guest_name, room_no, contact_no) VALUES ('"
                    + guestName + "', " + roomNumber + ", '" + contactNumber + "')";

            try (Statement st = con.createStatement()) {
                int affectedRows = st.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation Successful");
                } else {
                    System.out.println("Reservation Failed");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during reservation: " + e.getMessage());
        }
    }

    private static void viewReservations(Connection con) throws SQLException {
        String sql = "SELECT * FROM reservations";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("Current Reservations:");
            System.out.println("+----------------+----------------------------+-------------+------------------+------------------------+");
            System.out.println("| Reservation ID |            Guest           | Room Number |  Contact Number  |    Reservation Date    |");
            System.out.println("+----------------+----------------------------+-------------+------------------+------------------------+");

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                String guestName = rs.getString("guest_name");
                int roomNumber = rs.getInt("room_no");
                String contactNumber = rs.getString("contact_no");
                String reservationDate = rs.getTimestamp("reservation_date").toString();

                System.out.printf("| %-14d | %-26s | %-11d | %-16s | %-22s |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }
            System.out.println("+----------------+----------------------------+-------------+------------------+------------------------+");
        }
    }

    private static void getRoomNumber(Connection con, Scanner sc) {
        try {
            System.out.print("Enter reservation ID: ");
            int reservationId = sc.nextInt();
            System.out.print("Enter guest name: ");
            String guestName = sc.next();

            String sql = "SELECT room_no FROM reservations WHERE reservation_id=" + reservationId
                    + " AND guest_name='" + guestName + "'";

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                if (rs.next()) {
                    int roomNumber = rs.getInt("room_no");
                    System.out.println("Room number for Reservation ID " + reservationId
                            + " and guest " + guestName + " is: " + roomNumber);
                } else {
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching room number: " + e.getMessage());
        }
    }

    private static void updateReservation(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Reservation ID to update: ");
            int reservationId = sc.nextInt();
            sc.nextLine();

            if (!reservationExists(con, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            System.out.print("Enter new guest name: ");
            String newGuestName = sc.nextLine();
            System.out.print("Enter new room number: ");
            int newRoomNumber = sc.nextInt();
            System.out.print("Enter new contact number: ");
            String newContactNumber = sc.next();

            String sql = "UPDATE reservations SET guest_name='" + newGuestName + "', room_no=" + newRoomNumber
                    + ", contact_no='" + newContactNumber + "' WHERE reservation_id=" + reservationId;

            try (Statement st = con.createStatement()) {
                int affectedRows = st.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully!");
                } else {
                    System.out.println("Reservation update failed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating reservation: " + e.getMessage());
        }
    }

    private static void deleteReservation(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Reservation ID to delete: ");
            int reservationId = sc.nextInt();

            if (!reservationExists(con, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String sql = "DELETE FROM reservations WHERE reservation_id=" + reservationId;

            try (Statement st = con.createStatement()) {
                int affectedRows = st.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully!");
                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting reservation: " + e.getMessage());
        }
    }

    private static boolean reservationExists(Connection con, int reservationId) {
        String sql = "SELECT reservation_id FROM reservations WHERE reservation_id=" + reservationId;

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking reservation existence: " + e.getMessage());
            return false;
        }
    }

    public static void exit() throws InterruptedException {
        System.out.println("Exiting System...");
        for (int i = 5; i > 0; i--) {
            System.out.println(".");
            Thread.sleep(1000);
        }
        System.out.println("Thank you for using the hotel Reservation system.");
    }
}
