package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.booking.*;

public class App {
    public static void main(String[] args) {

        Booking booking = BookingFactory.getBooking("venue");

        booking = new VIPDecorator(booking);
        booking = new CateringDecorator(booking);

        System.out.println(booking.createBooking());
    }
}