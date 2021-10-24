package com.sahilpc.todonext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.sahilpc.todonext.Utils.SharedPreferenceClass;
import com.sahilpc.todonext.Utils.llottiedialogfragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private ImageView backbtn;
    private Button loginActivityBtn,signUp;

    TextInputLayout username, usermail, password;
    SharedPreferenceClass sharedPreferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        backbtn = findViewById(R.id.backbtn);
        loginActivityBtn = findViewById(R.id.login_activity);
        signUp = findViewById(R.id.signup);

        username = findViewById(R.id.usernamelayout);
        usermail = findViewById(R.id.userMailLayout);
        password = findViewById(R.id.passwordLayout);
        sharedPreferenceClass = new SharedPreferenceClass(this);



        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loginActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             onBackPressed();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateUserName() | !validateUserMail() | !validatePassword()) {
                    return;
                }

               registerUser();

            }

        });


    }

    private void registerUser() {
        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(SignUpActivity.this);

        lottie.show();
        final HashMap<String, String> params = new HashMap<>();
        params.put("username", username.getEditText().getText().toString());
        params.put("email",usermail.getEditText().getText().toString().toLowerCase());
        params.put("password",password.getEditText().getText().toString());

        String apiKey = "https://todoappsahilpc.herokuapp.com/api/todo/auth/register";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                apiKey, new JSONObject(params), response -> {
                    try {
                        if(response.getBoolean("success")) {
                            String token = response.getString("token");
                            sharedPreferenceClass.setValue_string("token", token);
                            Toast.makeText(SignUpActivity.this, "SignUp Success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                        lottie.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        lottie.dismiss();

                    }
                }, error -> {

            NetworkResponse response = error.networkResponse;
            if(error instanceof ServerError && response != null) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,  "utf-8"));

                    JSONObject obj = new JSONObject(res);
                    Toast.makeText(SignUpActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                    lottie.dismiss();
                } catch (JSONException | UnsupportedEncodingException je) {
                    je.printStackTrace();
                    lottie.dismiss();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return params;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }

    public void onBackPressed() {
        super.onBackPressed();
        return;
    }

    private boolean validateUserName() {

        String val = username.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            username.setError("Field Can not be empty");
            return false;
        } else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateUserMail() {

        String val = usermail.getEditText().getText().toString().trim();
        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";

        if (val.isEmpty()) {
            usermail.setError("Field can not be empty");
            return false;
        } else if (!val.matches(checkEmail)) {
            usermail.setError("Invalid Email!");
            return false;
        } else {
            usermail.setError(null);
            usermail.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {

        String val = password.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            password.setError("Field can not be empty");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }


}

