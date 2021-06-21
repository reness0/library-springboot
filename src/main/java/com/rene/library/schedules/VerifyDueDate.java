package com.rene.library.schedules;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rene.library.models.Book;
import com.rene.library.services.BookService;
import com.rene.library.services.ReserveService;

@Component
public class VerifyDueDate {

	Logger logger = Logger.getLogger(VerifyDueDate.class.getName());

	@Autowired
	private BookService bookService;

	@Autowired
	private ReserveService reserveService;

	@Scheduled(fixedRate = 5000)
	public void scheduleIfIsItExpired() {

		isExpired();

	}

	private void isExpired() {

		List<Book> books = bookService.findAll();

		logger.log(Level.INFO, "Searching all books...");

		for (Book book : books) {

			Thread thread = new Thread() {
				public void run() {

					try {
						if (book.getExpiration_date().isBefore(Instant.now().minusSeconds(10800))) {

							book.setExpiration_date(null);
							book.setIsExpired("Y");

							logger.log(Level.WARNING, "The book: " + book.getTitle() + " is expired");

							logger.log(Level.WARNING, "The book: " + book.getTitle() + " is being devolved right now...");
							reserveService.devolveBook(book.getReservedBy().getId(), book.getId());

						} else {

							logger.log(Level.INFO, "The book: " + book.getTitle() + " is not expired");

						}
					} catch (NullPointerException e) {

						book.setExpiration_date(null);
						book.setIsExpired("N");

						logger.log(Level.INFO, "The book: " + book.getTitle() + " has no expiration date");
					}

				}
			};

			thread.start();

		}

	}

}
