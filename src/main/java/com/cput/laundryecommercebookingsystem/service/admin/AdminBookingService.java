package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.repository.ILaundryMachineRepository;
import com.cput.laundryecommercebookingsystem.repository.TimeSlotRepository;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import com.cput.laundryecommercebookingsystem.service.IBookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus.*;

/*AdminBookingService.java
 * Admin dashboard: every booking, booking details, status changes and a day schedule.
 * Double bookings are blocked when a booking is made (BookingController locks the machine and
 * BookingServiceImpl checks the slot), so the admin never has to untangle two students in one slot.
 */
@Service
public class AdminBookingService {

    /** Allowed status changes. Finished bookings (completed or cancelled) can't be reopened. */
    private static final Map<BookingStatus, Set<BookingStatus>> NEXT = Map.of(
            PENDING, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(COMPLETED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    private final iBookingRepository bookingRepository;
    private final IBookingService bookingService;
    private final ILaundryMachineRepository machineRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AdminFacilityService facilityService;

    public AdminBookingService(iBookingRepository bookingRepository, IBookingService bookingService,
                               ILaundryMachineRepository machineRepository, TimeSlotRepository timeSlotRepository,
                               AdminFacilityService facilityService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.machineRepository = machineRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.facilityService = facilityService;
    }

    /** Newest wash date first. Every filter is optional. */
    @Transactional(readOnly = true)
    public List<AdminBookingView> list(BookingStatus status, Long machineId, LocalDate from, LocalDate to, String q) {
        String search = q == null ? "" : q.trim().toLowerCase();
        return bookingRepository.findAll().stream()
                .filter(b -> status == null || b.getStatus() == status)
                .filter(b -> machineId == null
                        || (b.getLaundryMachine() != null && machineId.equals(b.getLaundryMachine().getMachineId())))
                .filter(b -> from == null || (slotDate(b) != null && !slotDate(b).isBefore(from)))
                .filter(b -> to == null || (slotDate(b) != null && !slotDate(b).isAfter(to)))
                .filter(b -> search.isEmpty() || matches(b, search))
                .sorted(Comparator.comparing(AdminBookingService::slotDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AdminBookingService::slotStart, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(AdminBookingView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminBookingView get(Long id) {
        return AdminBookingView.from(find(id));
    }

    /** The statuses this booking can move to next (the dashboard shows these as buttons). */
    public Set<BookingStatus> nextStatuses(BookingStatus current) {
        return NEXT.getOrDefault(current, Set.of());
    }

    @Transactional
    public AdminBookingView updateStatus(Long id, BookingStatus target) {
        if (target == null) {
            throw new IllegalArgumentException("Choose a status");
        }
        Booking booking = find(id);
        if (booking.getStatus() == target) {
            return AdminBookingView.from(booking);
        }
        if (!nextStatuses(booking.getStatus()).contains(target)) {
            throw new IllegalStateException("A " + booking.getStatus() + " booking can't be changed to " + target);
        }
        // Use the existing service methods so the student gets the usual notifications.
        Booking saved = switch (target) {
            case CONFIRMED -> bookingService.confirmBooking(id);
            case CANCELLED -> bookingService.cancelBooking(id);
            default -> bookingService.updateStatus(id, target);
        };
        return AdminBookingView.from(saved);
    }

    /** One day at a glance: every machine against every time slot, with the bookings in between. */
    @Transactional(readOnly = true)
    public ScheduleView schedule(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        List<TimeSlot> slots = timeSlotRepository.findByDateBetweenOrderByDateAscStartTimeAsc(day, day);
        Set<Long> slotIds = slots.stream().map(TimeSlot::getId).collect(Collectors.toSet());

        List<AdminBookingView> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != CANCELLED)
                .filter(b -> b.getTimeSlot() != null && slotIds.contains(b.getTimeSlot().getId()))
                .map(AdminBookingView::from)
                .toList();

        Map<Long, Long> perSlot = bookings.stream()
                .collect(Collectors.groupingBy(AdminBookingView::timeSlotId, Collectors.counting()));

        List<MachineView> machines = machineRepository.findAll().stream()
                .sorted(Comparator.comparing(LaundryMachine::getMachineNumber, Comparator.nullsLast(String::compareTo)))
                .map(facilityService::toView)
                .toList();

        List<TimeSlotView> slotViews = slots.stream()
                .map(t -> new TimeSlotView(t.getId(), t.getDate(), t.getStartTime(), t.getEndTime(),
                        t.isAvailable(), perSlot.getOrDefault(t.getId(), 0L)))
                .toList();

        return new ScheduleView(day, machines, slotViews, bookings);
    }

    private Booking find(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with id: " + id));
    }

    private static LocalDate slotDate(Booking b) {
        return b.getTimeSlot() == null ? null : b.getTimeSlot().getDate();
    }

    private static LocalTime slotStart(Booking b) {
        return b.getTimeSlot() == null ? null : b.getTimeSlot().getStartTime();
    }

    private static boolean matches(Booking b, String search) {
        String haystack = (b.getStudent() == null ? "" : b.getStudent().getFirstName() + " "
                + b.getStudent().getLastName() + " " + b.getStudent().getEmail())
                + " " + (b.getLaundryMachine() == null ? "" : b.getLaundryMachine().getMachineNumber())
                + " #" + b.getId();
        return haystack.toLowerCase().contains(search);
    }
}
