
package com.cput.laundryecommercebookingsystem.config;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.factory.LaundryMachineFactory;
import com.cput.laundryecommercebookingsystem.factory.LaundryRoomFactory;
import com.cput.laundryecommercebookingsystem.factory.LaundryServiceFactory;
import com.cput.laundryecommercebookingsystem.factory.ProductFactory;
import com.cput.laundryecommercebookingsystem.factory.TimeSlotFactory;
import com.cput.laundryecommercebookingsystem.repository.ILaundryMachineRepository;
import com.cput.laundryecommercebookingsystem.repository.ILaundryRoomRepository;
import com.cput.laundryecommercebookingsystem.repository.ILaundryServiceRepository;
import com.cput.laundryecommercebookingsystem.repository.IProductRepository;
import com.cput.laundryecommercebookingsystem.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills the database with sample data each time the application starts, so the app is not empty.
 *
 * - Rooms, machines, services and products are only added when their table is empty,
 *   so nothing is duplicated and nothing you added yourself is touched.
 * - Time slots are topped up on every start: any of the next DAYS_AHEAD days that has
 *   no slots yet gets a full day of them. That keeps "upcoming" dates available.
 *
 * To switch this off, add this line to application.properties:
 *     app.seed-data=false
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final int DAYS_AHEAD = 14;
    private static final int FIRST_SLOT_HOUR = 7;   // first slot starts at 07:00
    private static final int LAST_SLOT_HOUR = 20;   // last slot starts at 20:00, ends 21:00

    private final ILaundryRoomRepository roomRepository;
    private final ILaundryMachineRepository machineRepository;
    private final ILaundryServiceRepository serviceRepository;
    private final IProductRepository productRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Value("${app.seed-data:true}")
    private boolean enabled;

    public DataSeeder(ILaundryRoomRepository roomRepository,
                      ILaundryMachineRepository machineRepository,
                      ILaundryServiceRepository serviceRepository,
                      IProductRepository productRepository,
                      TimeSlotRepository timeSlotRepository) {
        this.roomRepository = roomRepository;
        this.machineRepository = machineRepository;
        this.serviceRepository = serviceRepository;
        this.productRepository = productRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        // Each section runs on its own, so a problem in one does not stop the others
        // or stop the application from starting.
        runSection("laundry rooms and machines", this::seedRoomsAndMachines);
        runSection("laundry services", this::seedServices);
        runSection("products", this::seedProducts);
        runSection("time slots", this::seedTimeSlots);
    }

    // ---------------- ROOMS AND MACHINES ----------------

    private int seedRoomsAndMachines() {
        if (roomRepository.count() > 0 || machineRepository.count() > 0) {
            return 0;
        }

        LaundryRoom roomA = saveRoom("Laundry Room A", "Block A, ground floor", 8,
                "The largest laundry room, next to the common room. Open to all residents.");
        LaundryRoom roomB = saveRoom("Laundry Room B", "Block B, first floor", 6,
                "Quieter room beside the study lounge, with a folding table.");
        LaundryRoom roomC = saveRoom("Laundry Room C", "Block C, basement", 4,
                "Small room with two large-capacity machines for bedding.");

        List<LaundryMachine> machines = new ArrayList<>();
        LaundryMachineFactory factory = new LaundryMachineFactory();

        machines.add(factory.create("A-W1", "Washer", MachineStatus.AVAILABLE, roomA));
        machines.add(factory.create("A-W2", "Washer", MachineStatus.AVAILABLE, roomA));
        machines.add(factory.create("A-W3", "Washer", MachineStatus.IN_USE, roomA));
        machines.add(factory.create("A-W4", "Washer", MachineStatus.AVAILABLE, roomA));
        machines.add(factory.create("A-D1", "Dryer", MachineStatus.AVAILABLE, roomA));
        machines.add(factory.create("A-D2", "Dryer", MachineStatus.AVAILABLE, roomA));

        machines.add(factory.create("B-W1", "Washer", MachineStatus.AVAILABLE, roomB));
        machines.add(factory.create("B-W2", "Washer", MachineStatus.AVAILABLE, roomB));
        machines.add(factory.create("B-W3", "Washer", MachineStatus.OUT_OF_ORDER, roomB));
        machines.add(factory.create("B-D1", "Dryer", MachineStatus.AVAILABLE, roomB));
        machines.add(factory.create("B-D2", "Dryer", MachineStatus.IN_USE, roomB));

        machines.add(factory.create("C-W1", "Large washer", MachineStatus.AVAILABLE, roomC));
        machines.add(factory.create("C-W2", "Large washer", MachineStatus.AVAILABLE, roomC));
        machines.add(factory.create("C-D1", "Large dryer", MachineStatus.AVAILABLE, roomC));

        machineRepository.saveAll(machines);
        return 3 + machines.size();
    }

    private LaundryRoom saveRoom(String roomNumber, String location, int capacity, String description) {
        LaundryRoom room = LaundryRoomFactory.createLaundryRoom(roomNumber, location, capacity, description);
        room.addRoom(); // new rooms start inactive; this activates the room so students can see it
        return roomRepository.save(room);
    }

    // ---------------- SERVICES ----------------

    private int seedServices() {
        if (serviceRepository.count() > 0) {
            return 0;
        }

        List<LaundryService> services = List.of(
                LaundryServiceFactory.createLaundryService("Wash and fold",
                        "We wash, dry and fold one load, ready to collect the same day.", 60.00),
                LaundryServiceFactory.createLaundryService("Ironing",
                        "Up to ten items pressed and hung.", 45.00),
                LaundryServiceFactory.createLaundryService("Duvet and bedding wash",
                        "One duvet or a full set of bedding in a large-capacity machine.", 90.00),
                LaundryServiceFactory.createLaundryService("Stain treatment",
                        "Pre-treatment of marked items before your wash.", 30.00),
                LaundryServiceFactory.createLaundryService("Express wash",
                        "A quick 30-minute cycle for a light load.", 40.00));

        serviceRepository.saveAll(services);
        return services.size();
    }

    // ---------------- PRODUCTS ----------------

    private int seedProducts() {
        if (productRepository.count() > 0) {
            return 0;
        }

        List<Product> products = List.of(
                ProductFactory.createProduct("Laundry liquid 1L",
                        "Liquid detergent for front and top loaders. About 20 washes.", 64.99, "Detergent", 40),
                ProductFactory.createProduct("Washing powder 2kg",
                        "Everyday washing powder for whites and colours.", 79.99, "Detergent", 35),
                ProductFactory.createProduct("Single-wash detergent sachet",
                        "One sachet for one load. Handy if you only wash once a week.", 8.50, "Detergent", 120),
                ProductFactory.createProduct("Fabric softener 500ml",
                        "Softens and freshens. Add to the softener drawer.", 38.50, "Softener", 30),
                ProductFactory.createProduct("Fabric softener sachet",
                        "One-wash softener sachet.", 6.00, "Softener", 90),
                ProductFactory.createProduct("Stain remover bar",
                        "Rub on collars, cuffs and marks before washing.", 21.00, "Stain care", 50),
                ProductFactory.createProduct("Stain remover spray 250ml",
                        "Spray on, leave for five minutes, then wash as usual.", 44.99, "Stain care", 25),
                ProductFactory.createProduct("Colour catcher sheets (10)",
                        "Catches loose dye so colours don't run in mixed loads.", 49.99, "Stain care", 20),
                ProductFactory.createProduct("Mesh laundry bag",
                        "Keeps delicates and socks together in the machine.", 45.00, "Accessories", 18),
                ProductFactory.createProduct("Clothes pegs (24)",
                        "Plastic pegs for the drying lines.", 25.00, "Accessories", 40),
                ProductFactory.createProduct("Foldable laundry basket",
                        "Carries a full load and folds flat under your bed.", 129.00, "Accessories", 12),
                ProductFactory.createProduct("Dryer sheets (20)",
                        "Reduces static and adds a light scent in the dryer.", 55.00, "Accessories", 0));

        productRepository.saveAll(products);
        return products.size();
    }

    // ---------------- TIME SLOTS ----------------

    private int seedTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int day = 0; day <= DAYS_AHEAD; day++) {
            LocalDate date = today.plusDays(day);

            // Leave any date that already has slots exactly as it is.
            if (!timeSlotRepository.findByDate(date).isEmpty()) {
                continue;
            }

            // For today, only add slots that have not started yet.
            int firstHour = day == 0
                    ? Math.max(FIRST_SLOT_HOUR, LocalTime.now().getHour() + 1)
                    : FIRST_SLOT_HOUR;

            for (int hour = firstHour; hour <= LAST_SLOT_HOUR; hour++) {
                slots.add(TimeSlotFactory.createTimeSlot(
                        LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0), date, true));
            }
        }

        timeSlotRepository.saveAll(slots);
        return slots.size();
    }

    // ---------------- Helpers ----------------

    private interface Section {
        int run();
    }

    private void runSection(String name, Section section) {
        try {
            int added = section.run();
            if (added > 0) {
                System.out.println("[DataSeeder] Added " + added + " " + name + ".");
            }
        } catch (Exception e) {
            System.out.println("[DataSeeder] Could not add " + name + ": " + e.getMessage());
        }
    }
}

