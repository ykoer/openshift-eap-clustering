package com.redhat.ads.openshift.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/product")
    public void addProductIdToCart(@RequestParam long id,
                                   @RequestParam String name,
                                   @RequestParam BigDecimal price,
                                   @RequestParam int quantity,
                                   HttpSession httpSession) {

        Set<Product> cartProducts = (Set<Product>) httpSession.getAttribute("cart");
        if (cartProducts == null) {
            cartProducts = new HashSet<>();
        }


        Product product = cartProducts.stream().filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);

        if (product!=null) {
            product.addQuantity(quantity);

        } else {
            product = new Product(id, name, price, quantity);
        }

        cartProducts.add(product);
        httpSession.setAttribute("cart", cartProducts);

        System.out.println("----> Add product with the ID:" + id);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "")
    public Overview checkout(HttpSession httpSession) {

        Set<Product> cartProducts = (Set<Product>) httpSession.getAttribute("cart");

        BigDecimal total = BigDecimal.ZERO;
        int products = 0;
        if (cartProducts!=null) {
            for(Product p:cartProducts) {
                total = total.add(p.getTotalPrice());
                products+=p.getQuantity();
            }
        }
        System.out.println("----> Return cart");
        return new Overview(total, products, cartProducts);
    }

    static class Product implements Serializable {
        private Long id;
        private String name;
        private BigDecimal price;
        private int quantity;

        public Product(Long id, String name, BigDecimal price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getTotalPrice() {
            return price.multiply(new BigDecimal(quantity));
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void addQuantity(int quantity) {
            this.quantity += quantity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Product product = (Product) o;
            return Objects.equals(id, product.id);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id);
        }
    }

    static class Overview implements Serializable {
        private BigDecimal total;
        private int totalProductCount;
        private Set<Product> products;

        public Overview(BigDecimal total, int totalProductCount, Set<Product> products) {
            this.total = total;
            this.totalProductCount = totalProductCount;
            this.products = products;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public int getTotalProductCount() {
            return totalProductCount;
        }

        public void setTotalProductCount(int totalProductCount) {
            this.totalProductCount = totalProductCount;
        }

        public Set<Product> getProducts() {
            return products;
        }

        public void setProducts(Set<Product> products) {
            this.products = products;
        }
    }



}
