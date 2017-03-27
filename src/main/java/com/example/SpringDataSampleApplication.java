package com.example;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableSwagger2
@Import({springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration.class,
		springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration.class})
public class SpringDataSampleApplication {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.build();
	}

	@Bean
	CommandLineRunner initData(BookRepository bookRepository, AuthorRepository authorRepository){
		return args -> {
			bookRepository.save(new Book("Spring Microservices", "Learn how to efficiently build and implement microservices in Spring,\n" +
					"and how to use Docker and Mesos to push the boundaries. Examine a number of real-world use cases and hands-on code examples.\n" +
					"Distribute your microservices in a completely new way", LocalDate.of(2016, 06, 28), new Money(new BigDecimal(45.83)),
					authorRepository.save(new Author("Felipe", "Gutierrez"))));
			bookRepository.save(new Book("Pro Spring Boot", "A no-nonsense guide containing case studies and best practise for Spring Boot",
					LocalDate.of(2016, 05, 21 ), new Money(new BigDecimal(42.74)),
					authorRepository.save(new Author("Rajesh", "RV"))));
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringDataSampleApplication.class, args);
	}
}

@Data
@Entity
@NoArgsConstructor
class Book {

	@Id
	@GeneratedValue
	private Long id;

	@Size(min=1, max=255)
	private String title;

	@Size(min=1, max=255)
	private String description;

	@NotNull
	private LocalDate publishedDate;

	@NotNull
	@Embedded
	private Money price;

	@Size(min = 1)
	@ManyToMany
	private List<Author> authors;

	Book(String title, String description, LocalDate publishedDate, Money price, Author author) {
		this.title = title;
		this.description = description;
		this.publishedDate = publishedDate;
		this.price = price;
		this.authors = Arrays.asList(author);;
	}

	Book(String title, String description, LocalDate publishedDate, Money price, List<Author> authors) {
		this.title = title;
		this.description = description;
		this.publishedDate = publishedDate;
		this.price = price;
		this.authors = authors;
	}
}

@Embeddable
@Data
@NoArgsConstructor
class Money {

	enum Currency {CAD, EUR, USD }

	@DecimalMin(value="0",inclusive=false)
	@Digits(integer=1000000000,fraction=2)
	private BigDecimal amount;

	private Currency currency;

	Money(BigDecimal amount){
		this(Currency.USD, amount);
	}

	Money(Currency currency, BigDecimal amount){
		this.currency = currency;
		this.amount = amount;
	}
}

@RepositoryRestResource
interface  BookRepository extends CrudRepository<Book, Long> {

	List<Book> findByTitle(@Param("title") String title);
	List<Book> findByTitleContains(@Param("keyword") String keyword);
	List<Book> findByPublishedDateAfter(@Param("publishedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedDate);
	List<Book> findByTitleContainsAndPublishedDateAfter(@Param("keyword") String keyword,
														@Param("publishedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedDate);
	List<Book> findByTitleContainsAndPriceCurrencyAndPriceAmountBetween(@Param("keyword") String keyword,
																		@Param("currency") Money.Currency currency,
																		@Param("low") BigDecimal low,
																		@Param("high") BigDecimal high);
	List<Book> findByAuthorsLastName(@Param("lastName") String lastName);
}

@Entity
@Data
@NoArgsConstructor
class Author {

	@Id
	@GeneratedValue
	private Long id;

	@Size(min = 1, max=255)
	private String firstName;

	@Size(min = 1, max = 255)
	private String lastName;

	@Size(min = 1)
	@ManyToMany(mappedBy = "authors")
	private List<Book> books;

	Author(String firstName, String lastName){
		this.firstName = firstName;
		this.lastName = lastName;
	}
}

@RepositoryRestResource
interface AuthorRepository extends CrudRepository<Author, Long>{

	List<Author> findByLastName(@Param("lastName") String lastName);
	List<Author> findByBooksTitle(@Param("title") String title);
}