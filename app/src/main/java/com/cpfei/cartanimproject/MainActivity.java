package com.cpfei.cartanimproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView tvAddCartBtn;
    ImageView ivShoppingCartIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAddCartBtn = findViewById(R.id.tv_xesmall_detail_bottom_add_cart);
        ivShoppingCartIcon = ((ImageView) findViewById(R.id.iv_course_shopping_cart_icon));

        tvAddCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView cartIcon = new TextView(MainActivity.this);
                cartIcon.setText("购");
                cartIcon.setTextSize(10);
                cartIcon.setGravity(Gravity.CENTER);
                cartIcon.setIncludeFontPadding(false);
                cartIcon.setTextColor(getResources().getColor(R.color.colorAccent));
                cartIcon.setBackgroundResource(R.drawable.shape_cart_icon_bg);

                new CartAnimUtils(MainActivity.this).addGoodsToCart(cartIcon, tvAddCartBtn, ivShoppingCartIcon);
            }
        });

    }



}
