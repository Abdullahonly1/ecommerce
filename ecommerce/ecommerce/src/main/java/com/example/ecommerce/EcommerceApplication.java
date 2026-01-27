package com.example.ecommerce;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = "com.example.ecommerce")
@EnableJpaRepositories(basePackages = "com.example.ecommerce.repository")
public class EcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(ProductRepository productRepository) {
		return args -> {
			if (productRepository.count() == 0) {
				System.out.println("Loading initial products...");

				// Women's Fashion
				productRepository.save(new Product("Leather Handbag", "Stylish women's leather handbag", 3500, 10,
						"https://media.istockphoto.com/id/1302787124/photo/beige-leather-women-handbag-isolated-on-white-background.jpg?s=612x612&w=0&k=20&c=fOO0zCBqF3rbiGLLHwgtOMHxt66adpKikE7Fs2C_fDs=",
						"Women's Fashion"));

				productRepository.save(new Product("Summer Dress", "Floral summer dress", 2200, 4,
						"https://thumbs.dreamstime.com/b/beautiful-woman-luxurious-floral-dress-posing-summer-field-fashion-outdoor-photo-dark-curly-hair-45057116.jpg",
						"Women's Fashion"));

				productRepository.save(new Product("High Heel Sandals", "Elegant high heels", 2800, 10,
						"https://media.istockphoto.com/id/942926448/photo/set-of-colored-womens-shoes-on-beige-background.jpg?s=612x612&w=0&k=20&c=OKPtxR0axe8D9mnBEdY63NQT9hiWwN7b8BGjj-YYhBg=",
						"Women's Fashion"));

// Men's Fashion
				productRepository.save(new Product("Casual Shirt", "Cotton casual shirt", 1800, 5,
						"https://thumbs.dreamstime.com/b/neatly-folded-blue-dark-plaid-men-s-dress-shirt-button-down-collar-displayed-white-background-classic-pattern-423279018.jpg",
						"Men's Fashion"));

				productRepository.save(new Product("Jeans Pant", "Slim fit jeans", 3200, 4,
						"https://media.istockphoto.com/id/1281304280/photo/folded-blue-jeans-on-a-white-background-modern-casual-clothing-flat-lay-copy-space.jpg?s=612x612&w=0&k=20&c=nSMI2abaVovzkH1n0eXeJYCkrtI-6QcD_V7OVUz4zS4=",
						"Men's Fashion"));

				productRepository.save(new Product("Leather Belt", "Genuine leather belt", 1200, 6,
						"https://www.shutterstock.com/image-photo/black-leather-belt-isolated-on-260nw-2533333553.jpg",
						"Men's Fashion"));

// Electronics (100% working, clean isolated images)
				productRepository.save(new Product("Sony Headphones", "Noise cancelling headphones", 32000, 5,
						"https://www.sony.com/image/6145c1d32e6ac8e63a46c912dc33c5bb?fmt=pjpeg&bgcolor=FFFFFF&bgc=FFFFFF&wid=2515&hei=1320",
						"Electronics"));  // Official Sony WH-1000XM5 - black, high quality, always works

				productRepository.save(new Product("Smart TV 55\"", "4K UHD Smart TV", 65000, 8,
						"https://media.istockphoto.com/id/611294276/photo/uhd-4k-smart-tv-on-white-background.jpg?s=612x612&w=0&k=20&c=VtBQvDY7t131L2GScWcg6f4mXw1Kcgn3jqLUUD2jP1s=",
						"Electronics"));  // UHD 4K Smart TV front view, isolated white bg - tested working

				// Gaming Mouse - RGB lighting সহ cool gaming mouse
				productRepository.save(new Product("Gaming Mouse", "RGB Gaming Mouse", 2800, 5,
						"https://thumbs.dreamstime.com/b/close-up-rgb-computer-mouse-stylish-tech-accessory-glowing-pink-light-modern-device-work-gaming-high-input-great-gamers-356858655.jpg",
						"Electronics"));
// Home & Living
				productRepository.save(new Product("Sofa Set", "Comfortable 5-seater sofa", 48000, 3, "https://images.unsplash.com/photo-1555041469-a586c61ea9bc", "Home & Living"));
				productRepository.save(new Product("Dining Table", "Wooden dining table set", 25000, 10, "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92", "Home & Living"));
				productRepository.save(new Product("Bed Sheet Set", "Cotton bed sheet", 1800, 50, "https://images.unsplash.com/photo-1578894381163-e72c17f2d45f", "Home & Living"));

// Beauty (clean isolated product shots)
				productRepository.save(new Product("Lipstick Set", "Matte lipstick collection", 1200, 8,
						"https://thumbs.dreamstime.com/b/matte-lipstick-colors-isolated-white-background-matte-lipstick-colors-isolated-white-background-makeup-product-high-117461434.jpg",
						"Beauty"));

				// Face Cream - Moisturizing cream jar clean view
				productRepository.save(new Product("Face Cream", "Moisturizing face cream", 1500, 6,
						"https://thumbs.dreamstime.com/b/white-jar-beauty-cream-isolated-white-pearl-cosmetic-background-open-lid-31264661.jpg",
						"Beauty"));

				// Perfume - Luxury perfume bottle elegant shot
				productRepository.save(new Product("Perfume", "Long lasting perfume", 3500, 5,
						"https://freerangestock.com/sample/174252/bottle-of-luxury-perfume-on-white-background.jpg",
						"Beauty"));
// Mobile Phones (official-style isolated shots)
				productRepository.save(new Product("iPhone 15 Pro", "Latest iPhone model", 150000, 12,
						"https://c8.alamy.com/comp/2RTX656/antalya-turkey-september-14-2023-newly-released-natural-titanium-iphone-15-pro-mockup-set-with-back-and-front-angles-2RTX656.jpg",
						"Mobile Phones"));

				productRepository.save(new Product("Samsung Galaxy S24", "Flagship Android phone", 120000, 8,
						"https://www.shutterstock.com/image-photo/samsung-galaxy-s24-on-white-260nw-2479673213.jpg",
						"Mobile Phones"));

				productRepository.save(new Product("Modern Smartphone", "Latest mobile phone", 120000, 5,
						"https://www.shutterstock.com/image-photo/modern-smartphone-blank-white-screen-260nw-2582553305.jpg",
						"Mobile Phones"));
// Laptops
				productRepository.save(new Product("MacBook Air M2", "Lightweight laptop", 120000, 10, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8", "Laptops"));
				productRepository.save(new Product("Dell XPS 13", "Premium ultrabook", 110000, 8, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853", "Laptops"));
				productRepository.save(new Product("HP Pavilion", "Affordable laptop", 65000, 15, "https://images.unsplash.com/photo-1589561084283-930aa7b1ce50", "Laptops"));

// Books (actual cover isolated on white)
				productRepository.save(new Product("Atomic Habits", "Self-help book", 800, 10,
						"https://www.shutterstock.com/image-photo/close-james-clears-atomic-habits-260nw-2299777397.jpg",
						"Books"));

				productRepository.save(new Product("The Alchemist", "Motivational novel", 600, 8,
						"https://c8.alamy.com/comp/2PN4KN7/paulo-coelho-the-alchemist-paperback-book-on-white-background-first-published-1988-2PN4KN7.jpg",
						"Books"));

				productRepository.save(new Product("Rich Dad Poor Dad", "Financial education", 700, 9,
						"https://www.shutterstock.com/image-photo/pune-maharashtra-india-march-202025front-260nw-2600983151.jpg",
						"Books"));
// Groceries (real package product photos)
				productRepository.save(new Product("Rice 5kg", "Premium Basmati rice", 450, 20,
						"https://www.shutterstock.com/image-photo/woman-holding-bag-basmati-rice-260nw-2489531095.jpg",
						"Groceries"));

				// Cooking Oil 1L - Vegetable oil bottle real product
				productRepository.save(new Product("Cooking Oil 1L", "Healthy vegetable oil", 250, 15,
						"https://static.vecteezy.com/system/resources/previews/068/362/270/non_2x/cooking-oil-or-vegetable-oil-in-transparent-plastic-bottle-isolated-with-clipping-path-in-file-format-png.png",
						"Groceries"));

				productRepository.save(new Product("Tea Packet", "Premium black tea", 180, 30,
						"https://images.unsplash.com/photo-1571934811356-5cc061b6821f", "Groceries"));
				System.out.println("Products with categories loaded successfully!");

			}
		};
	}
}