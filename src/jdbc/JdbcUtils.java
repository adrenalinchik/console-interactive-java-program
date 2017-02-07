package jdbc;

import enums.Gender;
import enums.VehicleType;
import models.Owner;
import models.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by taras.fihurnyak on 2/3/2017.
 */
public class JdbcUtils implements SqlQuerys {
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;
    private PreparedStatement pstmt = null;
    private Scanner userInput = null;
    public Map<Integer, Owner> ownerMap = new HashMap<>();
    public Map<Integer, Vehicle> vehicleMap = new HashMap<>();

    public void startApplication() {
        try {
            userInput = new Scanner(System.in);
            System.out.println("Please enter credentials to connect to database.");
            System.out.println("Login:");
            String login = userInput.nextLine();
            System.out.println("Password:");
            String password = userInput.nextLine();
            connectToDB(login, password);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Goodbye!");
    }


    private void connectToDB(String login, String password) throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DATABASE_URL, login, password);
            createDatabase();
            checkTables();
            displayMenu();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void createDatabase() {
        try {
            statement = connection.createStatement();
            statement.executeUpdate(CREATE_DB);
            connection.setCatalog(DB_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void checkTables() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            if (!existVehicleTable(meta)) {
                createVehicleTable(connection);
                generateVehicles();
            }
            if (!existOwnerTable(meta)) {
                createOwnerTable(connection);
                generateOwners();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean existVehicleTable(DatabaseMetaData meta) throws SQLException {
        resultSet = meta.getTables(null, null, "VEHICLE", null);
        while (resultSet.next()) {
            String name = resultSet.getString("TABLE_NAME");
            if (name.equals("vehicle")) {
                return true;
            }
        }
        return false;
    }

    private boolean existOwnerTable(DatabaseMetaData meta) throws SQLException {
        resultSet = meta.getTables(null, null, "OWNER", null);
        while (resultSet.next()) {
            String name = resultSet.getString("TABLE_NAME");
            if (name.equals("owner")) {
                return true;
            }
        }
        return false;
    }

    private void createVehicleTable(Connection connection) throws SQLException {
        pstmt = connection.prepareStatement(CREATE_VEHICLE_TABLE);
        pstmt.executeUpdate();
    }

    private void createOwnerTable(Connection connection) throws SQLException {
        pstmt = connection.prepareStatement(CREATE_OWNER_TABLE);
        pstmt.executeUpdate();
    }

    private void generateOwners() {
        String firstname = "Owner_";
        String lastname = "OwnerLastname_";
        int age = 18;
        for (int i = 0; i <= 4; i++) {
            addOwnerToTable(CREATE_OWNER, firstname + i, lastname + i, "Male", age += 5);
            addOwnerToTable(CREATE_OWNER, firstname + "Female_" + i, lastname + "Female_" + i, "Female", age += 2);
        }

    }

    private void generateVehicles() {
        String model = "supercar_";
        for (int i = 0; i <= 9; i++) {
            addVehicleToTable(CREATE_VEHICLE, "Electro", ThreadLocalRandom.current().nextInt(1, 10 + 1), String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999 + 1)), model + "ELECT");
            addVehicleToTable(CREATE_VEHICLE, "Diesel", ThreadLocalRandom.current().nextInt(1, 10 + 1), String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999 + 1)), model + "TDT");
            addVehicleToTable(CREATE_VEHICLE, "GASOLINE", ThreadLocalRandom.current().nextInt(1, 10 + 1), String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999 + 1)), model + "4W");
            addVehicleToTable(CREATE_VEHICLE, "HIBRID", ThreadLocalRandom.current().nextInt(1, 10 + 1), String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999 + 1)), model + "ECO");
        }

    }

    private void creteNewOwner() {
        try {
            Owner owner = new Owner();
            userInput = new Scanner(System.in);
            System.out.println("Create new Owner...");
            System.out.println("Owner First name:");
            owner.setFirstName(userInput.nextLine());
            System.out.println("Owner Last name:");
            owner.setLastName(userInput.nextLine());
            System.out.println("Owner Gender:");
            if (userInput.nextLine().toUpperCase().equals(Gender.MALE.toString()))
                owner.setGender(Gender.MALE);
            else owner.setGender(Gender.FEMALE);
            System.out.println("Owner Age:");
            owner.setAge(Integer.parseInt(userInput.nextLine()));
            System.out.println("Please wait...");
            int id = addOwnerToTable(CREATE_OWNER, owner.getFirstName(), owner.getLastName(), owner.getGender().toString(), owner.getAge());
            ownerMap.put(id, owner);
            System.out.println("Owner successfully saved!");
            displayMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOwnerById() {
        Scanner inputID = new Scanner(System.in);
        System.out.println("Show Owner by ID...");
        System.out.println("Please enter Owner ID:");
        int id = Integer.parseInt(inputID.nextLine());
        Owner owner = getOwnerById(id);
        if (owner != null) {
            System.out.println(owner.getFirstName() + " " + owner.getLastName() + " " + owner.getGender().toString() + " " + owner.getAge());
            displayMenu();
        }

    }

    private void creteNewCar() {
        try {
            Vehicle vehicle = new Vehicle();
            userInput = new Scanner(System.in);
            System.out.println("Create new Vehicle...");
            System.out.println("Vehicle Model name:");
            vehicle.setModel(userInput.nextLine());
            System.out.println("Owner id:");
            int ownerId = Integer.parseInt(userInput.nextLine());
            if (isOwnerInDb(ownerId)) {
                vehicle.setOwner(getOwnerById(ownerId));
            } else {
                ownerWithSuchIdNotExistValidation();
            }
            System.out.println("Vehicle Number:");
            vehicle.setNumber(userInput.nextLine());
            System.out.println("Please select Vehicle Type:");
            System.out.println("*****************************************");
            System.out.println("| Vehicle types                         |");
            System.out.println("|        1. ELECTRO                     |");
            System.out.println("|        2. HIBRID                      |");
            System.out.println("|        3. GASOLINE                    |");
            System.out.println("|        4. DIESEL                      |");
            System.out.println("*****************************************");
            String carType = userInput.nextLine();
            switch (carType) {
                case "1":
                    vehicle.setCarType(VehicleType.ELECTRO);
                    break;
                case "2":
                    vehicle.setCarType(VehicleType.HIBRID);
                    break;
                case "3":
                    vehicle.setCarType(VehicleType.GASOLINE);
                    break;
                case "4":
                    vehicle.setCarType(VehicleType.DIESEL);
                    break;
            }
            System.out.println("Please wait...");
            int id = addVehicleToTable(CREATE_VEHICLE, vehicle.getCarType().toString(), ownerId, vehicle.getNumber(), vehicle.getModel());
            vehicleMap.put(id, vehicle);
            System.out.println("Vehicle successfully saved!");
            displayMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int addOwnerToTable(String query, String firstname, String lastname, String gender, int age) {
        int id = 0;
        try {
            pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, firstname);
            pstmt.setString(2, lastname);
            if (gender.toLowerCase().equals("male")) {
                pstmt.setInt(3, 1);
            } else pstmt.setInt(3, 2);
            pstmt.setInt(4, age);
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            generatedKeys.next();
            id = generatedKeys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private ArrayList<Vehicle> getVehiclesByOwnerId(int id) {
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        try {
            pstmt = connection.prepareStatement(GET_VEHICLE_BY_OWNER_ID + id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (vehicleMap.containsKey(rs.getInt(1))) {
                    vehicles.add(vehicleMap.get(rs.getInt(1)));
                } else {
                    try {
                        Vehicle vehicle = new Vehicle();
                        switch (rs.getInt(2)) {
                            case 1:
                                vehicle.setCarType(VehicleType.ELECTRO);
                                break;
                            case 2:
                                vehicle.setCarType(VehicleType.HIBRID);
                                break;
                            case 3:
                                vehicle.setCarType(VehicleType.GASOLINE);
                                break;
                            case 4:
                                vehicle.setCarType(VehicleType.DIESEL);
                                break;
                            default:
                        }
                        vehicle.setOwner(getOwnerById(id));
                        vehicle.setNumber(rs.getString(4));
                        vehicle.setModel(rs.getString(5));
                        vehicles.add(vehicle);
                        vehicleMap.put(rs.getInt(1), vehicle);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Owner with such id doesn't exist.");
            displayMenu();
        }
        return vehicles;
    }

    private void updateOwnerLastName(int ownerId, String newLastName) {
        try {
            pstmt = connection.prepareStatement(UPDATE_OWNER_LAST_NAME);
            pstmt.setString(1, newLastName);
            pstmt.setInt(2, ownerId);
            pstmt.executeUpdate();
            if (ownerMap.containsKey(ownerId)) {
                ownerMap.get(ownerId).setLastName(newLastName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteOwner(int ownerId) {
        try {
            pstmt = connection.prepareStatement(DELETE_OWNER);
            pstmt.setInt(1, ownerId);
            pstmt.executeUpdate();
            if (ownerMap.containsKey(ownerId)) {
                ownerMap.remove(ownerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Owner getOwnerById(int id) {
        Owner owner;
        if (getOwnerFromMapById(id) != null)
            owner = getOwnerFromMapById(id);
        else {
            getOwnerFromDbById(id);
            owner = getOwnerFromMapById(id);
        }
        return owner;
    }

    private Owner getOwnerFromMapById(int id) {
        Owner owner = null;
        if (ownerMap.containsKey(id))
            owner = ownerMap.get(id);
        return owner;
    }

    private Owner getOwnerFromDbById(int id) {
        Owner owner = null;
        try {
            pstmt = connection.prepareStatement(GET_OWNER_BY_ID + id);
            ResultSet rs = pstmt.executeQuery();
            owner = new Owner();
            rs.next();
            owner.setFirstName(rs.getString(2));
            owner.setLastName(rs.getString(3));
            if (rs.getInt(4) == 1)
                owner.setGender(Gender.MALE);
            else owner.setGender(Gender.FEMALE);
            owner.setAge(rs.getInt(5));
            ownerMap.put(rs.getInt(1), owner);
            return owner;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            ownerWithSuchIdNotExistValidation();
        }
        return owner;
    }

    private boolean isOwnerInDb(int id) {
        boolean isTrue = false;
        try {
            pstmt = connection.prepareStatement(GET_OWNER_BY_ID + id);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            rs.getInt(1);
            isTrue = true;
        } catch (SQLException e) {
            isTrue = false;
        }
        return isTrue;
    }

    private int getOwnerTableRowNumber() {
        int number = 0;
        try {
            pstmt = connection.prepareStatement(GET_MAX_OWNER_ID);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            number = rs.getInt(1);
            return number;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number;
    }

    private int addVehicleToTable(String query, String type, int owner_id, String carNumber, String model) {
        int id = 0;
        try {
            pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            switch (type.toLowerCase()) {
                case "electro":
                    pstmt.setInt(1, 1);
                    break;
                case "hibrid":
                    pstmt.setInt(1, 2);
                    break;
                case "gasoline":
                    pstmt.setInt(1, 3);
                    break;
                case "diesel":
                    pstmt.setInt(1, 4);
                    break;
                default:
            }
            pstmt.setInt(2, owner_id);
            pstmt.setString(3, carNumber);
            pstmt.setString(4, model);
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            generatedKeys.next();
            id = generatedKeys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private void DisconnectFromDB() {
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
            if (pstmt != null) pstmt.close();
            if (userInput != null) userInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doUpdateOwnerLastname() {
        userInput = new Scanner(System.in);
        System.out.println("Update Owner LastName...");
        System.out.println("Please enter Owner id:");
        int ownerId = Integer.parseInt(userInput.nextLine());
        if (isOwnerInDb(ownerId)) {
            System.out.println("Please enter new Owner Lastname:");
            updateOwnerLastName(ownerId, userInput.nextLine());
            System.out.println("Owner Last Name is successfully updated.");
            displayMenu();
        } else {
            ownerWithSuchIdNotExistValidation();
        }
    }

    private void ownerWithSuchIdNotExistValidation() {
        System.out.println("Owner with such id doesn't exist.");
        System.out.println("The max owner id in db is: " + getOwnerTableRowNumber());
        displayMenu();
    }

    private void doDeleteOwner() {
        userInput = new Scanner(System.in);
        System.out.println("Delete Owner...");
        System.out.println("Please enter Owner id:");
        int ownerId = Integer.parseInt(userInput.nextLine());
        if (isOwnerInDb(ownerId)) {
            deleteOwner(ownerId);
            System.out.println("Owner is successfully deleted.");
            displayMenu();
        } else {
            ownerWithSuchIdNotExistValidation();
        }

    }

    private void showAllOwnerCars() {
        userInput = new Scanner(System.in);
        System.out.println("Show All Owner Cars...");
        System.out.println("Please enter Owner id:");
        int ownerId = Integer.parseInt(userInput.nextLine());
        if (isOwnerInDb(ownerId)) {
            ArrayList<Vehicle> vehicles = getVehiclesByOwnerId(ownerId);
            System.out.println("**************************************************");
            for (Vehicle v : vehicles) {

                System.out.println("| " + v.getCarType() + " | " + v.getOwner().getLastName() + " | " + v.getNumber() + " | " + v.getModel() + " |");
            }
            System.out.println("**************************************************");
            displayMenu();
        } else {
            ownerWithSuchIdNotExistValidation();
        }
    }

    private void displayMenu() {
        userInput = new Scanner(System.in);
        String READ_MENU;

        System.out.println("*****************************************");
        System.out.println("|           JAVA Training               |");
        System.out.println("*****************************************");
        System.out.println("| Options:                              |");
        System.out.println("|        1. Create New Owner            |");
        System.out.println("|        2. Create New Car              |");
        System.out.println("|        3. Update Owner LastName       |");
        System.out.println("|        4. Show Owner by ID            |");
        System.out.println("|        5. Show All Owner Cars         |");
        System.out.println("|        6. Delete Owner                |");
        System.out.println("|        7. Exit                        |");
        System.out.println("*****************************************");

        System.out.print("Select option: ");

        READ_MENU = userInput.next();

        switch (READ_MENU) {
            case "1":
                creteNewOwner();
                break;
            case "2":
                creteNewCar();
                break;
            case "3":
                doUpdateOwnerLastname();
                break;
            case "4":
                showOwnerById();
                break;
            case "5":
                showAllOwnerCars();
                break;
            case "6":
                doDeleteOwner();
                break;
            case "7":
                DisconnectFromDB();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid selection");
                break;
        }
    }
}
