package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.BookingRow;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.entity.Movie;
import com.fu.cinema.entity.Room;
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.entity.Ticket;
import com.fu.cinema.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookingFacadeTest {

    @Test
    @DisplayName("BookingRow.from correctly maps entity to UI record")
    void testBookingRowMapping() {
        Customer c = new Customer();
        c.setCustomerId(1L);
        c.setFullName("Nguyen Van A");

        Movie m = new Movie();
        m.setTitle("Avengers");

        Room r = new Room();
        r.setRoomName("Cinema 1");

        Showtime s = new Showtime();
        s.setMovie(m);
        s.setRoom(r);
        s.setStartTime(LocalDateTime.of(2026, 11, 10, 19, 0));

        Booking b = new Booking();
        b.setBookingId(100L);
        b.setCustomer(c);
        b.setShowtime(s);
        b.setTotalAmount(new BigDecimal("150000"));
        b.setStatus(BookingStatus.PENDING);
        b.setBookingDate(LocalDateTime.of(2026, 11, 5, 12, 0));

        Ticket t1 = new Ticket(s, "A1", new BigDecimal("75000"));
        Ticket t2 = new Ticket(s, "A2", new BigDecimal("75000"));
        b.setTickets(List.of(t1, t2));

        BookingRow row = BookingRow.from(b);

        assertEquals(100L, row.id());
        assertEquals("Nguyen Van A", row.customerName());
        assertEquals("Avengers", row.movieTitle());
        assertEquals("Cinema 1", row.roomName());
        assertEquals(2, row.seatCount());
        assertEquals(new BigDecimal("150000"), row.total());
        assertEquals(BookingStatus.PENDING, row.status());
        assertNotNull(row.bookingDate());
    }
}
