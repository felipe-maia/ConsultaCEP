package com.example.consultacep;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import model.CEP;
import model.PostModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import service.CEPService;
import service.PostService;

public class MainActivity extends AppCompatActivity {

    private Retrofit retrofit;
    private EditText CEP;
    private TextView logradouro, cidade, uf, complemento, bairro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofit = new Retrofit.Builder().baseUrl("https://viacep.com.br/ws/") // ""https://jsonplaceholder.typicode.com
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        logradouro = findViewById(R.id.textLogradouro);
        cidade = findViewById(R.id.textCidade);
        complemento = findViewById(R.id.textCompl);
        uf = findViewById(R.id.textUf);
        bairro = findViewById(R.id.textBairro);
        CEP = findViewById(R.id.textCEP);
    }

    public void postService() { //com lista
        PostService postService = retrofit.create(PostService.class);
        Call<List<PostModel>> call = postService.buscarPost();

        call.enqueue(new Callback<List<PostModel>>() {
            @Override
            public void onResponse(Call<List<PostModel>> call, Response<List<PostModel>> response) {
                if (response.isSuccessful()) {
                    List<PostModel> posts = response.body();
                    String texto = "Objetos recuperados: " + posts.size();
                    logradouro.setText(texto);
                    for (PostModel postVal : posts) {
                        Log.i("Title", postVal.getTitle());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PostModel>> call, Throwable t) {
            }
        });
    }

    public void buscarCEPRetrofit() {
        CEPService cepService = retrofit.create(CEPService.class);
        Call<CEP> call = cepService.buscaCEP(CEP.getText().toString());
        call.enqueue(new Callback<model.CEP>() {
            @Override
            public void onResponse(Call<model.CEP> call, Response<model.CEP> response) {
                if (response.isSuccessful()) {
                    CEP cep = response.body();
                    logradouro.setText(cep.getLogradouro());
                    complemento.setText(cep.getComplemento());
                    cidade.setText(cep.getLocalidade());
                    uf.setText(cep.getUf());
                    bairro.setText(cep.getBairro());
                }
            }

            @Override
            public void onFailure(Call<model.CEP> call, Throwable t) {
                alerta();
            }
        });
    }

    public void busca(View v) {
        if (CEP.getText().length() == 8) {
            //buscarCEP();
            buscarCEPRetrofit();
            //postService();
            //acaoPost();
        } else {
            alerta();
        }
    }

    public void acaoPost() {
        PostService postService = retrofit.create(PostService.class);

        PostModel postEnviado = new PostModel();
        postEnviado.setId(0);
        postEnviado.setUserId(1);
        postEnviado.setTitle("Titulo teste");
        postEnviado.setBody("Corpo teste");
        Call<PostModel> call = postService.enviarPost(postEnviado);

        call.enqueue(new Callback<PostModel>() {
            @Override
            public void onResponse(Call<PostModel> call, Response<PostModel> response) {
                if (response.isSuccessful()) {
                    PostModel respostaPost = response.body();
                    String texto = "id: " + respostaPost.getId() + "\n"
                            + "Codigo: " + response.code();
                    logradouro.setText(texto);
                }
            }

            @Override
            public void onFailure(Call<PostModel> call, Throwable t) {

            }
        });
    }

    public void alerta() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Atenção");
        alertDialog.setMessage("CEP incorreto, verifique e tente novamente");
        alertDialog.setPositiveButton("OK", null);
        alertDialog.setCancelable(false);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public void buscarCEP() {
        String cepBusca = "https://viacep.com.br/ws/" + CEP.getText() + "/json/";
        BuscaCEP busca = new BuscaCEP();
        busca.execute(cepBusca);
    }

    class BuscaCEP extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String urlString = strings[0];

            try {
                URL url = new URL(urlString);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String linha = bf.readLine();
                StringBuffer sBuffer = new StringBuffer();

                while (linha != null) {
                    sBuffer.append(linha + "\n");
                    linha = bf.readLine();
                }

                bf.close();

                return sBuffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String sLogradouro, sComplemento, sCidade, sUf, sBairro;
            sLogradouro = sComplemento = sCidade = sUf = sBairro = "";


            try {
                JSONObject json = new JSONObject(s);
                sLogradouro = "Rua: " + json.getString("logradouro");
                sComplemento = json.getString("complemento");
                sCidade = "Cidade: " + json.getString("localidade");
                sUf = "UF: " + json.getString("uf");
                sBairro = "Bairro: " + json.getString("bairro");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            logradouro.setText(sLogradouro);
            complemento.setText(sComplemento);
            cidade.setText(sCidade);
            uf.setText(sUf);
            bairro.setText(sBairro);
        }
    }
}
