package ru.geekbrans.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import ru.geekbrans.dto.Product;

import java.util.List;

public interface ProductService {

    @POST("products")
    Call<Product> createProduct(@Body Product createProductRequest);

    @DELETE("products/{id}")
    Call<ResponseBody> deleteProduct(@Path("id") int id);

    @PUT("products")
    Call<Product> updateProduct(@Body Product updateProductRequest);

    @GET("products/{id}")
    Call<Product> getProduct(@Path("id") int id);

    @GET("products")
    Call<List<Product>> getProducts();

}
