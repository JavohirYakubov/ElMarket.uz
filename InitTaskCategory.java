package com.jw.jewel.el_marketuz.Asyncs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.jw.jewel.el_marketuz.Adapters.DatabaseHelper;
import com.jw.jewel.el_marketuz.Adapters.FavoritProductList;
import com.jw.jewel.el_marketuz.Constants;
import com.jw.jewel.el_marketuz.Fragments.FragmentProducts;
import com.jw.jewel.el_marketuz.MainActivity;
import com.jw.jewel.el_marketuz.models.Category;
import com.jw.jewel.el_marketuz.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jewel on 19.02.2018.
 */

public class InitTaskCategory extends AsyncTask<String,Void,String>{
    Context context;
    ProgressDialog progressDialog;
    WeakReference<FragmentProducts> mainActivityWeakReference;
    String slug="";

    public InitTaskCategory(FragmentProducts context) {
        this.context = context.getActivity();
        progressDialog=new ProgressDialog(context.getActivity());
        mainActivityWeakReference=new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        FragmentProducts fragmentProducts=mainActivityWeakReference.get();
        fragmentProducts.pbProduct.setVisibility(View.VISIBLE);
        //progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        slug=params[0];
        try {
            URL url = new URL("http://elmarket.uz/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_url = URLEncoder.encode("option", "UTF-8") + "=" + URLEncoder.encode("3", "UTF-8")+
                    "&"+URLEncoder.encode("category_name", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
            bufferedWriter.write(post_url);
            bufferedWriter.flush();
            bufferedWriter.close();

            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            String result = "";
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                result += line + "\n";
            }

            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onCancelled() {

        FragmentProducts fragmentProducts=mainActivityWeakReference.get();
        fragmentProducts.pbProduct.setVisibility(View.GONE);
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(String s) {
        //progressDialog.dismiss();
        final FragmentProducts fragmentProducts=mainActivityWeakReference.get();
        if (!s.isEmpty()) {
            try {
                JSONObject jsonObject=new JSONObject(s);
                if (!jsonObject.isNull("products")){
                    List<Product> productList=new ArrayList<>();

                    JSONArray jsonArray=jsonObject.getJSONArray("products");
                    for (int i=0; i<jsonArray.length(); i++){
                        Product product=Product.getCProductFromJsonObj(jsonArray.getJSONObject(i));
                        productList.add(product);
                    }


                    fragmentProducts.favoritProductList = new FavoritProductList(fragmentProducts.getActivity(), productList);
                    fragmentProducts.recyclerView.setAdapter(fragmentProducts.favoritProductList);
                    fragmentProducts.recyclerView.setItemAnimator(new DefaultItemAnimator());
                    fragmentProducts.recyclerView.setLayoutManager(new LinearLayoutManager(fragmentProducts.getActivity()));

                }
            } catch (JSONException e) {
                Toast.makeText(context, "Ошибка сети!\n" +
                        "Повторите попытку.", Toast.LENGTH_SHORT).show();
                fragmentProducts.imgRefresh.setVisibility(View.VISIBLE);
                fragmentProducts.imgRefresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new InitTaskCategory(fragmentProducts).execute(slug);
                        fragmentProducts.imgRefresh.setVisibility(View.GONE);
                    }
                });
                e.printStackTrace();
            }

        }else {
            Toast.makeText(context, "Ошибка сети!\n" +
                    "Повторите попытку.", Toast.LENGTH_SHORT).show();
            fragmentProducts.imgRefresh.setVisibility(View.VISIBLE);
            fragmentProducts.imgRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new InitTaskCategory(fragmentProducts).execute(slug);
                    fragmentProducts.imgRefresh.setVisibility(View.GONE);
                }
            });
        }

        fragmentProducts.pbProduct.setVisibility(View.GONE);
        super.onPostExecute(s);
    }

}
