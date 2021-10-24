package com.sahilpc.todonext.Interfaces;
public interface RecyclerViewClickListener {

    void onLongItemClick(int position);

    void onEditButtonClick(int position);
    void onDeleteButtonClick(int position);
    void onDoneButtonClick(int position);
}