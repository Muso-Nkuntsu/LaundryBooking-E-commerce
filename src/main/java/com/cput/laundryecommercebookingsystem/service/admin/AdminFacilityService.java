package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.repository.ILaundryMachineRepository;
import com.cput.laundryecommercebookingsystem.repository.ILaundryRoomRepository;
import com.cput.laundryecommercebookingsystem.repository.TimeSlotRepository;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import com.cput.laundryecommercebookingsystem.service.ILaundryMachineService;
import com.cput.laundryecommercebookingsystem.service.ILaundryRoomService;
import com.cput.laundryecommercebookingsystem.service.TimeSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/*AdminFacilityService.java
 * Admin dashboard: laundry rooms, machines and time slots (when machines can be booked).
 * Things with booking history are never deleted: the admin deactivates or closes them instead,
 * so old bookings, payments and reports stay correct.
 */
@Service
public class AdminFacilityService {

    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    private static final int MAX_GENERATE_DAYS = 31;

    private final ILaundryRoomRepository roomRepository;
    private final ILaundryRoomService roomService;
    private final ILaundryMachineRepository machineRepository;
    private final ILaundryMachineService machineService;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotService timeSlotService;
    private final iBookingRepository bookingRepository;

    public AdminFacilityService(ILaundryRoomRepository roomRepository, ILaundryRoomService roomService,
                                ILaundryMachineRepository machineRepository, ILaundryMachineService machineService,
                                TimeSlotRepository timeSlotRepository, TimeSlotService timeSlotService,
                                iBookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.roomService = roomService;
        this.machineRepository = machineRepository;
        this.machineService = machineService;
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotService = timeSlotService;
        this.bookingRepository = bookingRepository;
    }

    // =====================================================================
    // Rooms
    // =====================================================================

    @Transactional(readOnly = true)
    public List<RoomView> listRooms() {
        return roomRepository.findAll().stream()
                .sorted(Comparator.comparing(LaundryRoom::getRoomNumber, Comparator.nullsLast(String::compareTo)))
                .map(this::toView)
                .toList();
    }

    /** New rooms are switched on straight away so machines in them can be booked. */
    @Transactional
    public RoomView createRoom(RoomRequest req) {
        if (req == null || isBlank(req.roomNumber())) {
            throw new IllegalArgumentException("Room number is required");
        }
        validateRoom(req);
        if (roomRepository.findByRoomNumber(req.roomNumber().trim()).isPresent()) {
            throw new IllegalStateException("A room with number " + req.roomNumber().trim() + " already exists");
        }
        LaundryRoom room = roomService.createRoom(req.roomNumber().trim(), req.location().trim(),
                req.capacity(), trimOrEmpty(req.description()));
        return toView(roomService.activateRoom(room.getRoomId()));
    }

    /** The room number stays the same; location, capacity and description can change. */
    @Transactional
    public RoomView updateRoom(Long id, RoomRequest req) {
        LaundryRoom room = findRoom(id);
        validateRoom(req);
        int machines = machineRepository.findByLaundryRoom(room).size();
        if (req.capacity() < machines) {
            throw new IllegalStateException("This room already has " + machines
                    + " machines, so its capacity can't be lower than that");
        }
        return toView(roomService.updateRoom(id, req.location().trim(), req.capacity(), trimOrEmpty(req.description())));
    }

    @Transactional
    public RoomView setRoomActive(Long id, boolean active) {
        LaundryRoom room = findRoom(id);
        if (room.isActive() == active) {
            return toView(room);
        }
        return toView(active ? roomService.activateRoom(id) : roomService.deactivateRoom(id));
    }

    @Transactional
    public void deleteRoom(Long id) {
        LaundryRoom room = findRoom(id);
        if (!machineRepository.findByLaundryRoom(room).isEmpty()) {
            throw new IllegalStateException("This room still has machines. Move or delete them first, "
                    + "or deactivate the room instead.");
        }
        roomService.deleteRoom(id);
    }

    public RoomView toView(LaundryRoom room) {
        return new RoomView(room.getRoomId(), room.getRoomNumber(), room.getLocation(), room.getCapacity(),
                room.getDescription(), room.isActive(), machineRepository.findByLaundryRoom(room).size());
    }

    private void validateRoom(RoomRequest req) {
        if (req == null || isBlank(req.location())) {
            throw new IllegalArgumentException("Location is required");
        }
        if (req.capacity() == null || req.capacity() < 1 || req.capacity() > 200) {
            throw new IllegalArgumentException("Capacity must be between 1 and 200 machines");
        }
    }

    // =====================================================================
    // Machines
    // =====================================================================

    @Transactional(readOnly = true)
    public List<MachineView> listMachines() {
        return machineRepository.findAll().stream()
                .sorted(Comparator.comparing(LaundryMachine::getMachineNumber, Comparator.nullsLast(String::compareTo)))
                .map(this::toView)
                .toList();
    }

    @Transactional
    public MachineView createMachine(MachineRequest req) {
        if (req == null || isBlank(req.machineNumber())) {
            throw new IllegalArgumentException("Machine number is required");
        }
        if (isBlank(req.type())) {
            throw new IllegalArgumentException("Choose a machine type");
        }
        LaundryRoom room = findRoom(req.roomId());
        checkRoomHasSpace(room);
        LaundryMachine created = machineService.createMachine(req.machineNumber().trim(), req.type().trim(),
                req.status(), room.getRoomId());
        return toView(created);
    }

    @Transactional
    public MachineView updateMachine(Long id, MachineRequest req) {
        LaundryMachine machine = findMachine(id);
        if (req == null || isBlank(req.machineNumber())) {
            throw new IllegalArgumentException("Machine number is required");
        }
        if (isBlank(req.type())) {
            throw new IllegalArgumentException("Choose a machine type");
        }
        String number = req.machineNumber().trim();
        machineRepository.findByMachineNumber(number)
                .filter(other -> !other.getMachineId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalStateException("A machine with number " + number + " already exists");
                });
        LaundryRoom room = findRoom(req.roomId());
        boolean moving = machine.getLaundryRoom() == null || !room.getRoomId().equals(machine.getLaundryRoom().getRoomId());
        if (moving) {
            checkRoomHasSpace(room);
        }
        machine.updateDetails(number, req.type().trim(), room);
        if (req.status() != null) {
            machine.updateStatus(req.status());
        }
        return toView(machineRepository.save(machine));
    }

    /** Taking a machine offline doesn't cancel bookings; the result says how many need attention. */
    @Transactional
    public MachineStatusResult setMachineStatus(Long id, MachineStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Choose a status");
        }
        findMachine(id);
        LaundryMachine updated = machineService.updateMachineStatus(id, status);
        MachineView view = toView(updated);
        long affected = status == MachineStatus.OUT_OF_ORDER ? view.upcomingBookings() : 0;
        return new MachineStatusResult(view, affected);
    }

    @Transactional
    public void deleteMachine(Long id) {
        LaundryMachine machine = findMachine(id);
        if (bookingRepository.existsByLaundryMachine(machine)) {
            throw new IllegalStateException("This machine has bookings, so it can't be deleted. "
                    + "Set it to Out of order instead.");
        }
        machineRepository.delete(machine);
    }

    public MachineView toView(LaundryMachine m) {
        LaundryRoom room = m.getLaundryRoom();
        return new MachineView(m.getMachineId(), m.getMachineNumber(), m.getType(),
                m.getStatus() == null ? MachineStatus.AVAILABLE.name() : m.getStatus().name(),
                room == null ? null : room.getRoomId(), room == null ? "" : room.getRoomNumber(),
                upcomingBookings(m));
    }

    private long upcomingBookings(LaundryMachine machine) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findByLaundryMachineAndStatusIn(machine, ACTIVE).stream()
                .map(Booking::getTimeSlot)
                .filter(t -> t != null && t.getDate() != null && t.getEndTime() != null)
                .filter(t -> LocalDateTime.of(t.getDate(), t.getEndTime()).isAfter(now))
                .count();
    }

    private void checkRoomHasSpace(LaundryRoom room) {
        int machines = machineRepository.findByLaundryRoom(room).size();
        if (machines >= room.getCapacity()) {
            throw new IllegalStateException("Room " + room.getRoomNumber() + " is full (" + room.getCapacity()
                    + " machines). Increase its capacity first.");
        }
    }

    // =====================================================================
    // Time slots
    // =====================================================================

    /** Defaults to the next 14 days. */
    @Transactional(readOnly = true)
    public List<TimeSlotView> listSlots(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? LocalDate.now() : from;
        LocalDate end = to == null ? start.plusDays(13) : to;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("The end date must be on or after the start date");
        }
        Map<Long, Long> bookingsPerSlot = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED && b.getTimeSlot() != null)
                .collect(Collectors.groupingBy(b -> b.getTimeSlot().getId(), Collectors.counting()));
        return timeSlotRepository.findByDateBetweenOrderByDateAscStartTimeAsc(start, end).stream()
                .map(t -> new TimeSlotView(t.getId(), t.getDate(), t.getStartTime(), t.getEndTime(),
                        t.isAvailable(), bookingsPerSlot.getOrDefault(t.getId(), 0L)))
                .toList();
    }

    @Transactional
    public TimeSlotView createSlot(TimeSlotRequest req) {
        if (req == null || req.date() == null || req.startTime() == null || req.endTime() == null) {
            throw new IllegalArgumentException("Date, start time and end time are required");
        }
        validateTimes(req.date(), req.startTime(), req.endTime());
        if (slotExists(req.date(), req.startTime())) {
            throw new IllegalStateException("There is already a slot starting at " + req.startTime() + " on " + req.date());
        }
        TimeSlot slot = timeSlotService.createTimeSlot(req.startTime(), req.endTime(), req.date());
        return new TimeSlotView(slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.isAvailable(), 0);
    }

    /** Fills a date range with back-to-back slots, skipping any that already exist. */
    @Transactional
    public GenerateSlotsResult generateSlots(GenerateSlotsRequest req) {
        if (req == null || req.fromDate() == null || req.toDate() == null || req.dayStart() == null
                || req.dayEnd() == null || req.slotMinutes() == null) {
            throw new IllegalArgumentException("Fill in every field");
        }
        if (req.toDate().isBefore(req.fromDate())) {
            throw new IllegalArgumentException("The end date must be on or after the start date");
        }
        if (req.fromDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Slots can't be created in the past");
        }
        if (req.fromDate().plusDays(MAX_GENERATE_DAYS - 1L).isBefore(req.toDate())) {
            throw new IllegalArgumentException("Create at most " + MAX_GENERATE_DAYS + " days of slots at a time");
        }
        int length = req.slotMinutes();
        if (length < 15 || length > 240) {
            throw new IllegalArgumentException("Slot length must be between 15 and 240 minutes");
        }
        if (!req.dayEnd().isAfter(req.dayStart())) {
            throw new IllegalArgumentException("The day must end after it starts");
        }

        int created = 0;
        int skipped = 0;
        for (LocalDate day = req.fromDate(); !day.isAfter(req.toDate()); day = day.plusDays(1)) {
            Set<LocalTime> existing = timeSlotRepository.findByDate(day).stream()
                    .map(TimeSlot::getStartTime)
                    .collect(Collectors.toSet());
            LocalTime start = req.dayStart();
            while (true) {
                LocalTime end = start.plusMinutes(length);
                // Stop at the end of the day (and never wrap past midnight).
                if (end.isAfter(req.dayEnd()) || !end.isAfter(start)) {
                    break;
                }
                if (existing.contains(start)) {
                    skipped++;
                } else {
                    timeSlotService.createTimeSlot(start, end, day);
                    created++;
                }
                start = end;
            }
        }
        return new GenerateSlotsResult(created, skipped);
    }

    /** A closed slot can't be booked any more; existing bookings in it are kept. */
    @Transactional
    public TimeSlotView setSlotAvailable(Long id, boolean available) {
        findSlot(id);
        TimeSlot slot = available ? timeSlotService.markAsAvailable(id) : timeSlotService.markAsBooked(id);
        long bookings = bookingRepository.findByTimeSlotAndStatusNot(slot, BookingStatus.CANCELLED).size();
        return new TimeSlotView(slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(),
                slot.isAvailable(), bookings);
    }

    @Transactional
    public void deleteSlot(Long id) {
        TimeSlot slot = findSlot(id);
        if (bookingRepository.existsByTimeSlot(slot)) {
            throw new IllegalStateException("This slot has bookings, so it can't be deleted. Close it instead.");
        }
        timeSlotService.deleteTimeSlot(id);
    }

    private void validateTimes(LocalDate date, LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("The end time must be after the start time");
        }
        if (LocalDateTime.of(date, start).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Slots can't be created in the past");
        }
    }

    private boolean slotExists(LocalDate date, LocalTime start) {
        return timeSlotRepository.findByDate(date).stream().anyMatch(t -> start.equals(t.getStartTime()));
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private LaundryRoom findRoom(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Choose a laundry room");
        }
        return roomRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Laundry room not found with id: " + id));
    }

    private LaundryMachine findMachine(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Machine not found with id: " + id));
    }

    private TimeSlot findSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Time slot not found with id: " + id));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
