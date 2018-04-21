package com.jw.jewel.el_marketuz;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jw.jewel.el_marketuz.Adapters.DatabaseHelper;
import com.jw.jewel.el_marketuz.Adapters.SharedPreferens;
import com.jw.jewel.el_marketuz.Asyncs.SendTask;
import com.jw.jewel.el_marketuz.Asyncs.SendTaskCash;
import com.jw.jewel.el_marketuz.models.CardProduct;
import com.jw.jewel.el_marketuz.models.Price;
import com.jw.jewel.el_marketuz.models.Product;
import com.jw.jewel.el_marketuz.models.order_items;

import org.json.JSONException;

import uz.paycom.payment.PaymentActivity;
import uz.paycom.payment.model.Result;
import uz.paycom.payment.utils.PaycomSandBox;

import static uz.paycom.payment.PaymentActivity.EXTRA_AMOUNT;
import static uz.paycom.payment.PaymentActivity.EXTRA_ID;
import static uz.paycom.payment.PaymentActivity.EXTRA_LANG;
import static uz.paycom.payment.PaymentActivity.EXTRA_RESULT;
import static uz.paycom.payment.PaymentActivity.EXTRA_SAVE;

public class ShopReportLastActivity extends AppCompatActivity {
    Button btnPay;
    CheckBox chbP;
    ImageView imgPayme;
    int pay_type=0;

    TextView tvTotalCount, tvTotalSumma,tvPayCash;
    public EditText edAdd, edPhone;
    LinearLayout lyP;
    int i = 0;

    public String address = "";
    public String phone = "";
    public String token = "";
    public String jsonCard = "";
    public Double amaunt = 0.0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (resultCode == RESULT_OK) {
            address = edAdd.getText().toString();
            phone = edPhone.getText().toString();

            Result result = data.getParcelableExtra(EXTRA_RESULT);
            if (result.isVerify()) {

                SharedPreferens.init(ShopReportLastActivity.this);
                SharedPreferens.write(SharedPreferens.TOKEN, result.getToken());
                DatabaseHelper databaseHelper = DatabaseHelper.newInstance(ShopReportLastActivity.this);
                Price price = databaseHelper.getPrice();
                SharedPreferens.write(SharedPreferens.SUMMA, (float) price.getSumma());

                token = result.getToken();
                amaunt = price.getSumma();
                try {
                    jsonCard = order_items.getJsonString(databaseHelper.getAllCardProd(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new SendTask(ShopReportLastActivity.this).execute();
            } else {
                Toast.makeText(this, "Ошибка оплаты!", Toast.LENGTH_SHORT).show();

            }
        } else if (resultCode == RESULT_CANCELED) {


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_report_last);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarShopReportLast);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Покупка");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imgPayme=(ImageView)findViewById(R.id.imgPayPayme);
        tvPayCash=(TextView)findViewById(R.id.tvPayCash);

        edAdd = (EditText) findViewById(R.id.edAddress);
        edPhone = (EditText) findViewById(R.id.edPhone);
        btnPay = (Button) findViewById(R.id.btnShopReportLastPay);
        SharedPreferens.init(ShopReportLastActivity.this);

        edAdd.setText("Город Фергана ");
        edPhone.setText(SharedPreferens.read(SharedPreferens.PHONE, ""));

        tvTotalCount = (TextView) findViewById(R.id.tvTotalCount);
        tvTotalSumma = (TextView) findViewById(R.id.tvTotalSumma);

        imgPayme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPayCash.setBackgroundResource(R.drawable.pay_unfocus);
                imgPayme.setBackgroundResource(R.drawable.pay_focus);
                pay_type=0;
            }
        });

        tvPayCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPayCash.setBackgroundResource(R.drawable.pay_focus);
                imgPayme.setBackgroundResource(R.drawable.pay_unfocus);
                pay_type=1;
            }
        });
        DatabaseHelper databaseHelper = DatabaseHelper.newInstance(ShopReportLastActivity.this);

        final Price price = databaseHelper.getPrice();

        if (price != null) {
            tvTotalCount.setText(String.valueOf(price.getCount()));
            tvTotalSumma.setText(String.format("%.2f", price.getSumma()));
        }
        if (price.getSumma() < 30000) {
            Toast.makeText(this, "Общее количество продуктов должно быть 30000.00. Сумма!", Toast.LENGTH_LONG).show();
            finish();
        }
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(edPhone.getText().toString().isEmpty() || edAdd.getText().toString().isEmpty())) {
                    if (pay_type==0) {
                        String xAuth = "--------------------------------";
                        Intent intent = new Intent(ShopReportLastActivity.this, PaymentActivity.class);
                        intent.putExtra(EXTRA_ID, xAuth); //Ваш ID мерчанта
                        final Double sum = Double.valueOf(price.getSumma());
                        intent.putExtra(EXTRA_AMOUNT, sum); //Сумма оплаты
                        intent.putExtra(EXTRA_SAVE, true); //Сохранить для многократной оплаты?
                        intent.putExtra(EXTRA_LANG, "RU"); //Язык "RU" или "UZ"
                        PaycomSandBox.setEnabled(false); //true для тестовой площадки, по умолчанию false
                        SharedPreferens.init(ShopReportLastActivity.this);
                        SharedPreferens.write(SharedPreferens.ADDRESS, edAdd.getText().toString());
                        SharedPreferens.write(SharedPreferens.PHONE, edPhone.getText().toString());
                        startActivityForResult(intent, 0);
                    } else {
                        DatabaseHelper databaseHelper = DatabaseHelper.newInstance(ShopReportLastActivity.this);
                        Price price = databaseHelper.getPrice();
                        amaunt = price.getSumma();
                        SharedPreferens.write(SharedPreferens.SUMMA, (float) price.getSumma());
                        SharedPreferens.write(SharedPreferens.ADDRESS, edAdd.getText().toString());
                        SharedPreferens.write(SharedPreferens.PHONE, edPhone.getText().toString());
                        new SendTaskCash(ShopReportLastActivity.this).execute();
                    }
                } else {

                    Toast.makeText(ShopReportLastActivity.this, "Пожалуйста! Заполните все поля!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
