package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeAdapter2 extends RecyclerView.Adapter<HomeAdapter2.MainViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private Activity activity;
    private OnPostListener onPostListener;
    private FirebaseFirestore firebaseFirestore;
    private Util util;

    private FirebaseUser user;


    Map<String, Boolean> likey = new HashMap<>();
    Map<String, Boolean> likeyCancel = new HashMap<>();
    Object likefromdoc;


    public static class MainViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public MainViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public HomeAdapter2(Activity activity, ArrayList<PostInfo> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public void setOnPostListener(OnPostListener onPostListener){
        this.onPostListener = onPostListener;
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @NonNull
    @Override
    public HomeAdapter2.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post2, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,PostActivity.class);
                intent.putExtra("postInfo",mDataset.get(mainViewHolder.getAdapterPosition()));
                activity.startActivity(intent);
            }
        });


        cardView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, mainViewHolder.getAdapterPosition());
            }
        });

        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        // title
        CardView cardView = holder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.titleTextView);
        titleTextView.setText(mDataset.get(position).getTitle());
        // createdAt
        TextView createdAtTextView = cardView.findViewById(R.id.createdAtTextView);
        createdAtTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(mDataset.get(position).getCreatedAt()));

        LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // like
        Button likebtn = cardView.findViewById(R.id.likeBtn);
        TextView likeCounter = cardView.findViewById(R.id.likeCounterTextView);
        String  TAG = "like debug  :: ";
        Log.d(TAG, (Integer.toString(mDataset.get(position).getLikesCount())));

        user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUID = user.getUid();
        Log.d(TAG, "current user : " + currentUserUID);


        String id = mDataset.get(position).getID();
        PostInfo thisData = mDataset.get(position);
        Map<String, Boolean> isAlreadyliked = new HashMap<>();
        DocumentReference postDoc = firebaseFirestore.collection("posts").document(id);
        Log.d(TAG, "postDoc : "+postDoc.get().toString());
        postDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        likeCounter.setText(document.get("likesCount").toString()); // ????????? ?????? ????????? ?????? ??????.
                        thisData.setLikesCount(Integer.parseInt(document.get("likesCount").toString())); // thisData??? ??????????????? ??????
                        likefromdoc = document.get("likes");
                        Object likefromdoclikecnt = document.get("likesCount");
                        int likefromdoclikecntToint = Integer.parseInt(likefromdoclikecnt.toString());
                        Log.d(TAG, "likefromdoc count :::  " + likefromdoclikecnt);
                        if (likefromdoc == null || (likefromdoclikecntToint == 0)){
                            Log.d(TAG, "is already liked ::: no like ");
                            likebtn.setText("like");
                        } else { // ????????? ?????????
//                            Log.d(TAG, "is already liked ::: "+ likefromdoc.toString());
                            String[] liked = likefromdoc.toString().split("=");
//                            Log.d(TAG, "is already liked to string ::: "+  liked[0] + liked[1]);
                            isAlreadyliked.put(liked[0], true); // likes map ?????? ??????
                            Log.d(TAG, "is already liked ::: "+ isAlreadyliked);
                            thisData.setLikes(isAlreadyliked);
                            Log.d(TAG, "onComplete: " + thisData.getLikes());
                            if (liked[0].contains(currentUserUID)){
                                Log.d(TAG, "is already liked :: make btn liked");
                                likebtn.setText("liked");
                            }
//                            if (thisData.getLikes().containsKey(currentUserUID)){
//                                Log.d(TAG, "is already liked :: make btn liked");
//                                likebtn.setText("liked");
//                            }
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
//        likeCounter.setText("likescount : "+Integer.toString(mDataset.get(position).getLikesCount()));

        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likey.clear();
                likey.put(currentUserUID,true);
                Log.d(TAG, "1  ::  "+ likey.toString());
                DocumentReference postDoc = firebaseFirestore.collection("posts").document(id);
                Log.d(TAG, "2  ::  "+ postDoc);
//                if (likefromdoc.toString().split("=")[0].contains(currentUserUID)){
//
//                }
//                if (thisData.getLikes().containsKey(currentUserUID)){ //?????? ????????? ??????
                if (thisData.getLikes().containsValue(true)){
                    Log.d(TAG, "3  ::  ");
                    likebtn.setText("like"); // ?????? ???????????? ?????????
                    thisData.setLikesCount(thisData.getLikesCount() - 1); // ????????? ??? ????????????
                    thisData.getLikes().remove(currentUserUID); // ???????????? ?????????
                    likeyCancel.put(null,null);
                    thisData.setLikes(likeyCancel);
                    postDoc.update("likes", null);
                    likeCounter.setText(Integer.toString(thisData.getLikesCount()));
                    postDoc.update("likesCount", thisData.getLikesCount() );
                } else { // ????????? ?????????
                    Log.d(TAG, "4  ::  ");
                    likebtn.setText("liked"); // ?????????
                    thisData.setLikes(likey);
                    thisData.setLikesCount(thisData.getLikesCount() + 1); // ????????? ??? +
                    thisData.getLikes().put(currentUserUID,true);
                    postDoc.update("likes", likey);
                    likeCounter.setText(Integer.toString(thisData.getLikesCount())); // ?????????????????? ?????? ????????????
                    postDoc.update("likesCount", thisData.getLikesCount() );
                }
            }
        });


        contentsLayout.removeAllViews();

        // ?????? ?????? ?????????????????? ?????? ????????????
        String contents = mDataset.get(position).getContents();
        TextView textView = new TextView(activity);
        textView.setLayoutParams(layoutParams);
        textView.setText(contents);
        contentsLayout.addView(textView);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    private void showPopup(View v, final int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                String id = mDataset.get(position).getID();
                switch (menuItem.getItemId()) {
                    case R.id.modify:
                        //

                        onPostListener.onModify(id);
                        return true;
                    case R.id.delete:
                        firebaseFirestore.collection("posts").document(id)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        startToast("???????????? ?????????????????????.");
//                                        postsUpdate();
                                        onPostListener.onDelete(id);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        startToast("???????????? ???????????? ??????????????????.");
                                    }
                                });

                        return true;
                    default:
                        return false;
                }
            }
        });

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post, popup.getMenu());
        popup.show();
    }

    private void startToast(String msg){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}