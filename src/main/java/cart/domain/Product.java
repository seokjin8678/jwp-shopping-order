package cart.domain;

import java.util.Objects;

public class Product {
    private final Long id;
    private final String name;
    private final Price price;
    private final String imageUrl;

    public Product(Long id, String name, long price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = Price.from(price);
        this.imageUrl = imageUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }

        Product product = (Product) o;

        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price.getAmount();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public static final class Builder {
        private Long id;
        private String name;
        private long price;
        private String imageUrl;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(long price) {
            this.price = price;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Product build() {
            return new Product(id, name, price, imageUrl);
        }
    }
}
