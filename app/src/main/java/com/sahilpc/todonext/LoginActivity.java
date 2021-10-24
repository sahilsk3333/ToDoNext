package com.sahilpc.todonext;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button joinNow,logInbtn;
    TextInputLayout usermail, password;
    SharedPreferenceClass sharedPreferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        joinNow = findViewById(R.id.join_now);
        usermail = findViewById(R.id.maillayout);
        password = findViewById(R.id.passlayout);
        logInbtn = findViewById(R.id.logIn);

        sharedPreferenceClass = new SharedPreferenceClass(this);

        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(this);

        joinNow.setOnClickListener(v -> {

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this);
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class),options.toBundle());

        });

        logInbtn.setOnClickListener(v -> {
            if (!validateUserMail() | !validatePassword()) {
                return;
            }
            loginUser();
        });


    }

    private void loginUser() {

        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(LoginActivity.this);

       lottie.show();

        final HashMap<String, String> params = new HashMap<>();
        params.put("email",usermail.getEditText().getText().toString().toLowerCase());
        params.put("password",password.getEditText().getText().toString());

        String apiKey = "https://todoappsahilpc.herokuapp.com/api/todo/auth/login";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        String token = response.getString("token");

                        sharedPreferenceClass.setValue_string("token", token);
                        Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                   lottie.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                   lottie.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if(error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,  "utf-8"));
                        JSONObject obj = new JSONObject(res);
                        Toast.makeText(LoginActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                        lottie.dismiss();
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                        lottie.dismiss();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
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

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences todo_pref = getSharedPreferences("user_todo", MODE_PRIVATE);
        if(todo_pref.contains("token")) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

}