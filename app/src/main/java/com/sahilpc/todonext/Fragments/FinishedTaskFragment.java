package com.sahilpc.todonext.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sahilpc.todonext.Adapters.FinishedTaskAdapter;
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

public class FinishedTaskFragment extends Fragment implements RecyclerViewClickListener {

    SharedPreferenceClass sharedPreferenceClass;
    String token;
    FinishedTaskAdapter todoListAdapter;
    RecyclerView recyclerView;
    TextView empty_tv;
    ArrayList<TodoModel> arrayList;

    public FinishedTaskFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_finished_task, container, false);

        sharedPreferenceClass = new SharedPreferenceClass(getContext());
        token = sharedPreferenceClass.getValue_string("token");


        recyclerView = view.findViewById(R.id.recycler_view);
        empty_tv = view.findViewById(R.id.empty_tv);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        getTasks();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

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

    private void getTasks() {
        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(getActivity());

        arrayList = new ArrayList<>();
         lottie.show();
        String url = "https://todoappsahilpc.herokuapp.com/api/todo/finished";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONArray jsonArray = response.getJSONArray("todos");

                        if(jsonArray.length() == 0) {
                            empty_tv.setVisibility(View.VISIBLE);
                        } else {
                            empty_tv.setVisibility(View.GONE);
                            for(int i = 0; i < jsonArray.length(); i ++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                TodoModel todoModel = new TodoModel(
                                        jsonObject.getString("_id"),
                                        jsonObject.getString("title"),
                                        jsonObject.getString("description")
                                );
                                arrayList.add(todoModel);
                            }

                            todoListAdapter = new FinishedTaskAdapter(getActivity(), arrayList, FinishedTaskFragment.this);
                            recyclerView.setAdapter(todoListAdapter);
                        }

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



                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;

//                final String statusCode = String.valueOf(error.networkResponse.statusCode);

                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    JSONObject errorObject = new JSONObject(body);


                    if(errorObject.getString("msg").equals("Token not valid")) {
                        sharedPreferenceClass.clear();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getActivity(), errorObject.getString("msg") , Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    // exception
                    e.printStackTrace();
                }


              lottie.dismiss();
            }
        }) {
            @Override
            public Map<String, String> getHeaders()  {
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


    public void deleteTask(final String id, final int position) {
        //loading dialog
        llottiedialogfragment lottie = new llottiedialogfragment(getActivity());

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
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        lottie.dismiss();
                    }
                }, error -> {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                    lottie.dismiss();
                });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }


    public void showDeleteDialog(final String id, final int position) {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Delete Task..")
                .setPositiveButton("Delete", null)
                .setNegativeButton("Cancel", null)
                .create();


        dialog.setOnShowListener(dialogInterface -> {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

            button.setOnClickListener(v -> {
                deleteTask(id, position);
                dialog.dismiss();
            });
        });

        dialog.show();
    }


    @Override
    public void onLongItemClick(int position) {

    }

    @Override
    public void onEditButtonClick(int position) {

    }

    @Override
    public void onDeleteButtonClick(int position) {
        showDeleteDialog(arrayList.get(position).getId(), position);
    }

    @Override
    public void onDoneButtonClick(int position) {

    }
}