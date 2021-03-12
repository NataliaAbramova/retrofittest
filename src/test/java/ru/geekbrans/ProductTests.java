package ru.geekbrans;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Converter;
import ru.geekbrains.db.dao.CategoriesMapper;
import ru.geekbrains.db.dao.ProductsMapper;
import ru.geekbrains.db.model.Categories;
import ru.geekbrains.db.model.CategoriesExample;
import ru.geekbrains.db.model.Products;
import ru.geekbrains.db.model.ProductsExample;
import ru.geekbrans.base.enums.CategoryType;
import ru.geekbrans.dto.ErrorBody;
import ru.geekbrans.dto.Product;
import ru.geekbrans.service.ProductService;
import ru.geekbrans.service.ProductServiceWrong;
import ru.geekbrans.utils.DbUtils;
import ru.geekbrans.utils.RetrofitUtils;


import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductTests {
    Integer productId;
    final Integer wrondProdId = -555;
    Faker faker = new Faker();
    static ProductService productService;
    static ProductServiceWrong productServiceWrong;
    Product product;
    static ProductsMapper productsMapper;
    static CategoriesMapper categoriesMapper;

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        productService = RetrofitUtils.getRetrofit()
                .create(ProductService.class);
        productServiceWrong = RetrofitUtils.getRetrofit()
                .create(ProductServiceWrong.class);
        productsMapper = DbUtils.getProductsMapper();
        categoriesMapper = DbUtils.getCategoriesMapper();
    }

    @SneakyThrows
    @BeforeEach
    void setUp() {
        product = new Product()
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        retrofit2.Response<Product> response =
                productService.createProduct(product)
                        .execute();
        productId = response.body().getId();
    }

    @SneakyThrows
    @Test
    void getProductsTest() {
        retrofit2.Response<List<Product>> response =
                productService.getProducts()
                        .execute();
        assertThat(response.isSuccessful()).isTrue();
        //проверим, что вернулся список продуктов
        assertThat(response.body().size()).isGreaterThanOrEqualTo(0);
        //и что у всех есть ид
        assertThat(response.body().stream().allMatch(el -> el.getId() != null)).isTrue();
    }

    @SneakyThrows
    @Test
    void getProductByIdPositive() {
        retrofit2.Response<Product> response =
                productService.getProduct(productId)
                        .execute();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body().getCategoryTitle()).isEqualTo(product.getCategoryTitle());
        assertThat(response.body().getPrice()).isEqualTo(product.getPrice());
        assertThat(response.body().getTitle()).isEqualTo(product.getTitle());
        Products productDB = productsMapper.selectByPrimaryKey(productId.longValue());
        assertThat(response.body().getPrice()).isEqualTo(productDB.getPrice());
        assertThat(response.body().getTitle()).isEqualTo(productDB.getTitle());

    }

    @SneakyThrows
    @Test
    void getProductByIdNegative() {
        retrofit2.Response<Product> response =
                productService.getProduct(wrondProdId)
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.code()).isEqualTo(404);
        ErrorBody errorBody = RetrofitUtils.convertBody(response, ErrorBody.class);
        if (errorBody != null) {
            assertThat(errorBody.getMessage()).startsWith("Unable to find product with id");
        }
        assertThat(productsMapper.selectByPrimaryKey(wrondProdId.longValue())).isNull();
    }

    @SneakyThrows
    @Test
    void createNewProductTest() {
        retrofit2.Response<Product> response =
                productService.createProduct(product)
                        .execute();
        productId = response.body().getId();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(productsMapper.selectByPrimaryKey(productId.longValue())).isNotNull();
    }

    @SneakyThrows
    @Test
    void createNewProductNegativeTest() {
        retrofit2.Response<Product> response =
                productService.createProduct(product.withId(555))
                        .execute();
        assertThat(response.code()).isEqualTo(400);
        ErrorBody errorBody = RetrofitUtils.convertBody(response, ErrorBody.class);
        if (errorBody != null) {
            assertThat(errorBody.getMessage()).isEqualTo("Id must be null for new entity");
        }
        assertThat(productsMapper.selectByPrimaryKey(555L)).isNull();
    }

    @SneakyThrows
    @Test
    void createNewProductEmptyFieldsTest() {
        retrofit2.Response<Product> response =
                productService.createProduct(new Product())
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.code()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void createNewProductLongTitleTest() {
        retrofit2.Response<Product> response =
                productService.createProduct(new Product().withTitle(faker.lorem().fixedString(5000)))
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.code()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void createNewProductNullTest() {
        retrofit2.Response<Product> response =
                productServiceWrong.createProduct()
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.code()).isEqualTo(400);
    }

    @SneakyThrows
    @Test
    void deleteProductTest() {
        retrofit2.Response<ResponseBody> response =
                productService.deleteProduct(productId)
                        .execute();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(productsMapper.selectByPrimaryKey(productId.longValue())).isNull();
        productId = null;
    }

    @SneakyThrows
    @Test
    void deleteNotExistsProductTest() {
        retrofit2.Response<ResponseBody> response =
                productService.deleteProduct(wrondProdId)
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        productId = null;
    }

    @SneakyThrows
    @Test
    void deleteWithoutId() {
        retrofit2.Response<ResponseBody> response =
                productServiceWrong.deleteProduct()
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.code()).isEqualTo(405);
    }

    @SneakyThrows
    @Test
    void updateProductTest() {
        Product newProduct = new Product()
                .withId(productId)
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        retrofit2.Response<Product> response =
                productService.updateProduct(newProduct)
                        .execute();
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body().getId()).isEqualTo(productId);
        assertThat(response.body().getPrice()).isEqualTo(newProduct.getPrice());
        assertThat(response.body().getTitle()).isEqualTo(newProduct.getTitle());
        assertThat(response.body().getCategoryTitle()).isEqualTo(newProduct.getCategoryTitle());

        CategoriesExample categoriesExample = new CategoriesExample();
        categoriesExample.createCriteria().andTitleEqualTo(newProduct.getCategoryTitle());
        List<Categories> categories = categoriesMapper.selectByExample(categoriesExample);
        assertThat(categories).isNotNull();
        assertThat(categories.size()).isEqualTo(1);
        ProductsExample productsExample = new ProductsExample();
        productsExample.createCriteria()
                       .andIdEqualTo(productId.longValue())
                       .andPriceEqualTo(newProduct.getPrice())
                       .andTitleEqualTo(newProduct.getTitle())
                       .andCategory_idEqualTo(Long.valueOf(categories.get(0).getId()));
        assertThat(productsMapper.countByExample(productsExample)).isEqualTo(1L);
    }

    @SneakyThrows
    @Test
    void updateProductTestNegative() {
        Product newProduct = new Product()
                .withId(wrondProdId)
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        retrofit2.Response<Product> response =
                productService.updateProduct(newProduct)
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.code()).isEqualTo(400);
        ErrorBody errorBody = RetrofitUtils.convertBody(response, ErrorBody.class);
        if (errorBody != null) {
            assertThat(errorBody.getMessage()).containsIgnoringCase("doesn't exist");
        }
        assertThat(productsMapper.selectByPrimaryKey(wrondProdId.longValue())).isNull();
    }

    @SneakyThrows
    @Test
    void updateWithoutBody() {
        retrofit2.Response<Product> response =
                productServiceWrong.updateProduct()
                        .execute();
        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.code()).isEqualTo(400);
    }

    @AfterEach
    void tearDown() {
        if (productId!=null) {
            productsMapper.deleteByPrimaryKey(productId.longValue());
        }
    }

}
