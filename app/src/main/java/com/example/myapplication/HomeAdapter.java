package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import androidx.collection.SimpleArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import android.nfc.Tag;
import android.service.autofill.Dataset;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myapplication.PostInfo;
import com.example.myapplication.R;
import com.example.myapplication.WritePostActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MainViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private Activity activity;
    private OnPostListener onPostListener;
    private FirebaseFirestore firebaseFirestore;
    private Util util;

    private FirebaseUser user;


    Map<String, Boolean> likey = new HashMap<>();


    public static class MainViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public MainViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public HomeAdapter(Activity activity, ArrayList<PostInfo> myDataset) {
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
    public HomeAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,PostActivity.class);
                intent.putExtra("postInfo",mDataset.get(mainViewHolder.getAdapterPosition()));
                activity.startActivity(intent);
            }
        });

//        cardView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPopup(view,mainViewHolder.getAdapterPosition());
//            }
//        });

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
                        likeCounter.setText(document.get("likesCount").toString()); // 디비에 있는 좋아요 수로 설정.
                        thisData.setLikesCount(Integer.parseInt(document.get("likesCount").toString())); // thisData에 디비데이터 넣기
                        Object likefromdoc = document.get("likes");
                        if (likefromdoc == null){
                            Log.d(TAG, "is already liked ::: no like ");
                        } else {
//                            Log.d(TAG, "is already liked ::: "+ likefromdoc.toString());
                            String[] liked = likefromdoc.toString().split("=");
//                            Log.d(TAG, "is already liked to string ::: "+  liked[0] + liked[1]);
                            isAlreadyliked.put(liked[0], true);
                            Log.d(TAG, "is already liked ::: "+ isAlreadyliked);
                            thisData.setLikes(isAlreadyliked);
                            Log.d(TAG, "onComplete: " + thisData.getLikes());
                            if (thisData.getLikes().containsKey(currentUserUID)){
                                Log.d(TAG, "is already liked :: make btn liked");
                                likebtn.setText("liked");
                            }
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
                if (thisData.getLikes().containsKey(currentUserUID)){ //이미 눌렀던 상태
                    Log.d(TAG, "3  ::  ");
                    likebtn.setText("like"); // 다시 라이크로 돌리고
                    thisData.setLikesCount(thisData.getLikesCount() - 1); // 좋아요 수 하나빼기
                    thisData.getLikes().remove(currentUserUID); // 눌럿던거 지우기
                    postDoc.update("likes", null);
                    likeCounter.setText(Integer.toString(thisData.getLikesCount()));
                    postDoc.update("likesCount", thisData.getLikesCount() );
                } else { // 좋아요 누르기
                    Log.d(TAG, "4  ::  ");
                    likebtn.setText("liked"); // 좋아요
                    thisData.setLikesCount(thisData.getLikesCount() + 1); // 좋아요 수 +
                    thisData.getLikes().put(currentUserUID,true);
                    postDoc.update("likes", likey);
                    likeCounter.setText(Integer.toString(thisData.getLikesCount())); // 카운터글씨를 다시 바꿔주기
                    postDoc.update("likesCount", thisData.getLikesCount() );
                }

//                firebaseFirestore.runTransaction(new Transaction.Function<Void>() {
//                    @Override
//                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
//                        PostInfo postInfo =  transaction.get(postDoc).toObject(PostInfo.class);
//                        Log.d(TAG, "3  ::  "+ postInfo);
//                        if(postInfo.getLikes().containsKey(id)){
//                            postInfo.setLikesCount(postInfo.getLikesCount() -1);
//                            postInfo.getLikes().remove(id);
//
//                            firebaseFirestore.collection("posts").whereEqualTo("likes",likey)
//                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                                            if (value != null) {
//                                                likeCounter.setText("Likes "+postInfo.getLikesCount());
//                                                return;
//                                            }
//                                            for (DocumentSnapshot doc : value) {
//                                            }
//                                            notifyDataSetChanged();
//                                        }
//                                    });
//                        }else{
//                            postInfo.setLikesCount(postInfo.getLikesCount() +1);
//                            postInfo.getLikes().put(id,true);
//                            firebaseFirestore.collection("posts").whereEqualTo("likes",likey)
//                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                                            if (value != null) {
//                                                likeCounter.setText("Likes "+postInfo.getLikesCount());
//                                                return;
//                                            }
//                                            for (DocumentSnapshot doc : value) {
//                                            }
//                                            notifyDataSetChanged();
//                                        }
//                                    });
//                        }
//                        transaction.set(postDoc,postInfo);
//                        return null;
//                    }
//                });
                //Log.d(TAG, likey.toString());
                //likebtn.setText("liked");
                //likeCounter.setText("1");
            }
        });



        contentsLayout.removeAllViews();

        // 기존 방식 이미지어레이 없이 스트링만
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
                                        startToast("게시글을 삭제하였습니다.");
//                                        postsUpdate();
                                        onPostListener.onDelete(id);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        startToast("게시글을 삭제하지 못하였습니다.");
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