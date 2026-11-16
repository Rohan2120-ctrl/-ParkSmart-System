import java.time.*;
import java.util.*;

enum VehicleType {
    SUV(60.0),
    CAR(40.0),
    BIKE(20.0);
    private final double hourlyRate;
    VehicleType(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    public double getHourlyRate() {
        return hourlyRate;
    }
}

class Vehicle {
    private final String plateNumber;
    private final VehicleType type;
    private final String ownerName;
    private final String ownerPhone;
    public Vehicle(String plateNumber, VehicleType type, String ownerName, String ownerPhone) {
        this.plateNumber = plateNumber;
        this.type = type;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
    }
    public String getPlateNumber() {
        return plateNumber;
    }
    public VehicleType getType() {
        return type;
    }
    public String getOwnerName() {
        return ownerName;
    }
    public String getOwnerPhone() {
        return ownerPhone;
    }
}

class ParkingSlot {
    private final int slotNumber;
    private final VehicleType allowedType;
    private boolean occupied;
    private Vehicle currentVehicle;
    public ParkingSlot(int slotNumber, VehicleType allowedType) {
        this.slotNumber = slotNumber;
        this.allowedType = allowedType;
        this.occupied = false;
        this.currentVehicle = null;
    }
    public boolean park(Vehicle vehicle) {
        if (occupied || vehicle == null || vehicle.getType() != allowedType) {
            return false;
        }
        currentVehicle = vehicle;
        occupied = true;
        return true;
    }
    public Vehicle vacate() {
        Vehicle vehicle = currentVehicle;
        currentVehicle = null;
        occupied = false;
        return vehicle;
    }
    public int getSlotNumber() {
        return slotNumber;
    }
    public VehicleType getAllowedType() {
        return allowedType;
    }
    public boolean isOccupied() {
        return occupied;
    }
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }
}

class Floor {
    private final int floorNumber;
    private final Map<VehicleType, List<ParkingSlot>> slotsByType;
    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.slotsByType = new EnumMap<>(VehicleType.class);
        int slotCounter = 1;
        this.slotsByType.put(VehicleType.SUV, createSlots(VehicleType.SUV, 15, slotCounter));
        slotCounter += 15;
        this.slotsByType.put(VehicleType.CAR, createSlots(VehicleType.CAR, 15, slotCounter));
        slotCounter += 15;
        this.slotsByType.put(VehicleType.BIKE, createSlots(VehicleType.BIKE, 10, slotCounter));
    }
    private List<ParkingSlot> createSlots(VehicleType type, int count, int startSlotNumber) {
        List<ParkingSlot> slots = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            slots.add(new ParkingSlot(startSlotNumber + i, type));
        }
        return slots;
    }
    public ParkingSlot findAndOccupyAvailableSlot(Vehicle vehicle) {
        List<ParkingSlot> availableSlots = slotsByType.get(vehicle.getType());
        if (availableSlots == null) {
            return null;
        }
        for (ParkingSlot slot : availableSlots) {
            if (!slot.isOccupied() && slot.park(vehicle)) {
                return slot;
            }
        }
        return null;
    }
    public int getFloorNumber() {
        return floorNumber;
    }
}

class Ticket {
    private final String uniqueSecurityCode;
    private final Vehicle vehicle;
    private final int floorNumber;
    private final int slotNumber;
    private final LocalDateTime entryTime;
    public Ticket(Vehicle vehicle, int floorNumber, int slotNumber) {
        this.uniqueSecurityCode = "PARK-" + (1000 + new Random().nextInt(9000));
        this.vehicle = vehicle;
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
        this.entryTime = LocalDateTime.now();
    }
    public double calculateBill(LocalDateTime exitTime) {
        long durationMinutes = Duration.between(entryTime, exitTime).toMinutes();
        long hours = (long) Math.ceil(durationMinutes / 60.0);
        if (hours == 0) {
            hours = 1;
        }
        return hours * vehicle.getType().getHourlyRate();
    }
    public String getUniqueSecurityCode() {
        return uniqueSecurityCode;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public int getFloorNumber() {
        return floorNumber;
    }
    public int getSlotNumber() {
        return slotNumber;
    }
}

public class ParkingSystem {
    private final List<Floor> floors;
    private final Map<String, Ticket> activeTicketsByCode;
    public ParkingSystem(int numberOfFloors) {
        floors = new ArrayList<>();
        activeTicketsByCode = new HashMap<>();
        for (int i = 1; i <= numberOfFloors; i++) {
            floors.add(new Floor(i));
        }
    }
    public Ticket parkVehicle(String plateNo, VehicleType type, String ownerName, String phone) {
        Vehicle vehicle = new Vehicle(plateNo, type, ownerName, phone);
        for (Floor floor : floors) {
            ParkingSlot allocatedSlot = floor.findAndOccupyAvailableSlot(vehicle);
            if (allocatedSlot != null) {
                Ticket ticket = new Ticket(vehicle, floor.getFloorNumber(), allocatedSlot.getSlotNumber());
                activeTicketsByCode.put(ticket.getUniqueSecurityCode(), ticket);
                System.out.println("\n--- [PARKING VEHICLE] SUCCESS ---");
                System.out.println("Vehicle Plate: " + plateNo);
                System.out.println("Assigned: Floor " + floor.getFloorNumber() + ", Slot #" + allocatedSlot.getSlotNumber());
                System.out.println("UNIQUE SECURITY CODE: " + ticket.getUniqueSecurityCode());
                return ticket;
            }
        }
        System.out.println("PARKING FULL: No available slots for " + type);
        return null;
    }
    public double generateSlip(String uniqueCode, LocalDateTime exitTime) {
        Ticket ticket = activeTicketsByCode.get(uniqueCode);
        if (ticket == null) {
            System.out.println("\n[ERROR] ACCESS DENIED: Invalid Security Code (" + uniqueCode + ").");
            return -1.0;
        }
        double amountDue = ticket.calculateBill(exitTime);
        activeTicketsByCode.remove(uniqueCode);
        System.out.println("\n--- [GENERATE SLIP] EXIT RECEIPT ---");
        System.out.println("Verified Code: " + uniqueCode);
        System.out.println("Vehicle Plate: " + ticket.getVehicle().getPlateNumber());
        System.out.println("Slot Vacated: Floor " + ticket.getFloorNumber() + ", Slot #" + ticket.getSlotNumber());
        System.out.println("Total Amount Billed: ₹" + amountDue);
        return amountDue;
    }

    public static void main(String[] args) {
        ParkingSystem parkingLot = new ParkingSystem(3);
        Ticket t1 = parkingLot.parkVehicle("TS-09-AB-1234", VehicleType.SUV, "Rahul Sharma", "9876543210");
        if (t1 != null) {
            parkingLot.generateSlip(t1.getUniqueSecurityCode(), LocalDateTime.now().plusHours(2));
        }
    }
}