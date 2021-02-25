package ru.geekbrans.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import ru.geekbrans.dto.Product;

public interface ProductServiceWrong {

    @POST("products")
    Call<Product> createProduct();

    @DELETE("products")
    Call<ResponseBody> deleteProduct();

    @PUT("products")
    Call<Product> updateProduct();

}
