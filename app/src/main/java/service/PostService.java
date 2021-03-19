package service;

import java.util.List;

import model.PostModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PostService {

    @GET("/posts") //https://jsonplaceholder.typecode.com
    Call<List<PostModel>> buscarPost();

    @POST("/posts")
    Call<PostModel> enviarPost(@Body PostModel postEnviado);
}
