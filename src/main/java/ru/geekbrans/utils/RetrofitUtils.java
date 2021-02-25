package ru.geekbrans.utils;

import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.geekbrans.dto.ErrorBody;
import ru.geekbrans.dto.Product;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass
public class RetrofitUtils {

   HttpLoggingInterceptor logging =  new HttpLoggingInterceptor(new PrettyLogger());

    public Retrofit getRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMinutes(1l))
                .addInterceptor(logging.setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        return new Retrofit.Builder()
                .baseUrl(ConfigUtils.getBaseUrl())
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

    public static ErrorBody convertBody(Response<Product> response, Class<ErrorBody> errorBodyClass) throws IOException {
        if (response != null && !response.isSuccessful() && response.errorBody() != null) {
            ResponseBody body = response.errorBody();
            Converter<ResponseBody, ErrorBody> converter = RetrofitUtils.getRetrofit().responseBodyConverter(errorBodyClass, new Annotation[0]);
            return converter.convert(body);
        }
        return null;
    }
}
