package com.sahilpc.todonext.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sahilpc.todonext.Adapters.TodoListAdapter;
import com.sahilpc.todonext.Interfaces.RecyclerViewClickListener;
import com.sahilpc.todonext.LoginActivity;
import com.sahilpc.todonext.Model.TodoModel;
import com.sahilpc.todonext.R;
import com.sahilpc.todonext.Utils.SharedPreferenceClass;
import com.sahilpc.todonext.Utils.llottiedialogfragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements RecyclerViewClickListener {

    //Variables
    FloatingActionButton floatingActionButton;
    SharedPreferenceClass sharedPreferenceClass;
    String token;
    Dialog dialog;
    RecyclerView home_rv;
    TextView empty_tv;
    ArrayList<TodoModel> arrayList;
    TodoListAdapter todoListAdapter;

    public HomeFragment() {

        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferenceClass = new SharedPreferenceClass(getContext());
        token = sharedPreferenceClass.getValue_string("token");
        floatingActionButton = view.findViewById(R.id.add_task_btn);
        home_rv = view.findViewById(R.id.home_rv);
        empty_tv = view.findViewById(R.id.empty_tv);


        home_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        home_rv.setHasFixedSize(true);

        dialog = new Dialog(getContext());
        floatingActionButton.setOnClickListener(v -> showAlertDialog());

        getTasks();


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(home_rv);

        return view;
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            showDeleteDialog(arrayList.get(position).getId(), position);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };



    public void showAlertDialog() {
        dialog.setContentView(R.layout.custom_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView cd_close = dialog.findViewById(R.id.cd_close);
        Button cd_add = dialog.findViewById(R.id.cd_addBtn);
        EditText cd_title = dialog.findViewById(R.id.cd_title);
        EditText cd_disc = dialog.findViewById(R.id.cd_discription);
        dialog.show();

        cd_add.setOnClickListener(v -> {
            String title = cd_title.getText().toString().trim();
            String description = cd_disc.getText().toString().trim();
            if (!TextUtils.isEmpty(title)) {

                addTask(title, description);

                dialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Please enter title...", Toast.LENGTH_SHORT).show();
            }
        });

        cd_close.setOnClickListener(v -> dialog.dismiss());

    }

    public void showUpdateDialog(final  String  id, String title, String description)  {

        dialog.setContentView(R.layout.custom_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView cd_heading = dialog.findViewById(R.id.cd_heading);
        ImageView cd_close = dialog.findViewById(R.id.cd_close);
        Button cd_add = dialog.findViewById(R.id.cd_addBtn);
        EditText cd_title = dialog.findViewById(R.id.cd_title);
        EditText cd_disc = dialog.findViewById(R.id.cd_discription);

        cd_heading.setText("Update Task");
        cd_title.setText(title);
        cd_disc.setText(description);
        cd_add.setText("Update");

        dialog.show();

        cd_add.setOnClickListener(v -> {
            String utitle = cd_title.getText().toString().trim();
            String udescription = cd_disc.getText().toString().trim();
            if (!TextUtils.isEmpty(utitle)) {
                updateTask(id, utitle, udescription);

                dialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Please enter title...", Toast.LENGTH_SHORT).show();
            }
        });
        cd_close.setOnClickListener(v -> dialog.dismiss());

    }

    public void showDeleteDialog(final String id, final  int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Are you want to delete the task ?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", (dialog, which) -> getTasks())
                .create();

        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteTodo(id, position);
                    alertDialog.dismiss();
                }
            });
        });

        alertDialog.show();
    }

    public void showFinishedTaskDialog(final String id, final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Move to finished task?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                updateToFinishTodo(id, position);
                alertDialog.dismiss();
            });
        });

        alertDialog.show();
    }

    // Add Todo Task Method
    private void addTask(String title, String description) {

        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(getContext());


        lottie.show();
        String url = "https://todoappsahilpc.herokuapp.com/api/todo";

        HashMap<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, new JSONObject(body), response -> {
            try {
                if (response.getBoolean("success")) {
                    lottie.dismiss();
                    Toast.makeText(getActivity(), "Added Successfully", Toast.LENGTH_SHORT).show();

                                            getTasks();

                }
            } catch (JSONException e) {
                lottie.dismiss();
                e.printStackTrace();
            }
        }, error -> {

            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && response != null) {
                try {
                    lottie.dismiss();
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject obj = new JSONObject(res);
                    Toast.makeText(getActivity(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                } catch (JSONException | UnsupportedEncodingException je) {
                    lottie.dismiss();
                    je.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    // Get all Todo Task method
    public void getTasks() {

        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(getContext());

        arrayList = new ArrayList<>();
        lottie.show();
        String url = "https://todoappsahilpc.herokuapp.com/api/todo";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray = response.getJSONArray("todos");

                            if (jsonArray.length() == 0) {
                                empty_tv.setVisibility(View.VISIBLE);
                            } else {
                                empty_tv.setVisibility(View.GONE);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    TodoModel todoModel = new TodoModel(
                                            jsonObject.getString("_id"),
                                            jsonObject.getString("title"),
                                            jsonObject.getString("description")
                                    );
                                    arrayList.add(todoModel);
                                }

                                todoListAdapter = new TodoListAdapter(arrayList,getContext(),HomeFragment.this);
                                home_rv.setAdapter(todoListAdapter);
                            }

                        }
                        lottie.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        lottie.dismiss();
                    }
                }, error -> {


            NetworkResponse response = error.networkResponse;

            if (error == null || error.networkResponse == null) {
                return;
            }

            String body;

//                final String statusCode = String.valueOf(error.networkResponse.statusCode);

            try {
                body = new String(error.networkResponse.data, "UTF-8");
                JSONObject errorObject = new JSONObject(body);


                if (errorObject.getString("msg").equals("Token not valid")) {
                    sharedPreferenceClass.clear();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(getActivity(), errorObject.getString("msg"), Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException | JSONException e) {
                // exception
            }


            lottie.dismiss();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    // Update Todo Task Method
    private  void  updateTask(String id, String title, String description) {
        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(getContext());
        lottie.show();

        String url = "https://todoappsahilpc.herokuapp.com/api/todo/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                response -> {
                    try {
                        if(response.getBoolean("success")) {
                            getTasks();
                            lottie.dismiss();
                            Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        lottie.dismiss();
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", token);
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    // Delete Todo Method
    private void deleteTodo(final String id, final  int position) {

        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(getContext());
        lottie.show();
        String url = "https://todoappsahilpc.herokuapp.com/api/todo/"+id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null
                , response -> {
                    try {
                        if(response.getBoolean("success")) {
                            Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                            arrayList.remove(position);
                            todoListAdapter.notifyItemRemoved(position);
                            lottie.dismiss();
                        }lottie.dismiss();
                    } catch (JSONException e) {
                        lottie.dismiss();
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show());

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    // Update to finished task
    private void updateToFinishTodo(String id,final int position) {

        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(getContext());

        lottie.show();

        String url = "https://todoappsahilpc.herokuapp.com/api/todo/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("finished", "true");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                response -> {
                    try {
                        lottie.dismiss();
                        if(response.getBoolean("success")) {
                            arrayList.remove(position);
                            getTasks();

                            todoListAdapter.notifyItemRemoved(position);
                            Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        lottie.dismiss();
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }



    @Override
    public void onLongItemClick(int position) {

    }

    @Override
    public void onEditButtonClick(int position) {
        showUpdateDialog(arrayList.get(position).getId(), arrayList.get(position).getTitle(), arrayList.get(position).getDescription());
    }

    @Override
    public void onDeleteButtonClick(int position) {
        showDeleteDialog(arrayList.get(position).getId(), position);

    }

    @Override
    public void onDoneButtonClick(int position) {
        showFinishedTaskDialog(arrayList.get(position).getId(), position);
    }
}