package com.yashkasera.livstoryuploaddata.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yashkasera.livstoryuploaddata.R;
import com.yashkasera.livstoryuploaddata.adapter.SoundRecyclerAdapter;
import com.yashkasera.livstoryuploaddata.model.SoundModel;
import com.yashkasera.livstoryuploaddata.util.ClickListener;
import com.yashkasera.livstoryuploaddata.util.RecyclerTouchListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.yashkasera.livstoryuploaddata.MainActivity.lastVisibleSound;
import static com.yashkasera.livstoryuploaddata.MainActivity.playPosition;
import static com.yashkasera.livstoryuploaddata.MainActivity.soundModelArrayList;

public class PlayAudioFragment extends Fragment {
    private static final String TAG = "PlayAudioFragment";
    private final int limit = 10;
    SoundRecyclerAdapter soundRecyclerAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;

    public PlayAudioFragment() {
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        context = requireContext();
        return inflater.inflate(R.layout.fragment_play_audio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        soundRecyclerAdapter = new SoundRecyclerAdapter(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(soundRecyclerAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        if (soundModelArrayList.size() == 0) {
            soundRecyclerAdapter.addItem(null);
            db.collection("sounds")
                    .orderBy("type")
                    .limit(limit)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            soundRecyclerAdapter.removeItem(null);
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                SoundModel soundModel = new SoundModel(
                                        document.getId(),
                                        document.getString("type"),
                                        document.getString("media"),
                                        (List<String>) document.get("keywords")
                                );
                                soundRecyclerAdapter.addItem(soundModel);
                            }
                            lastVisibleSound = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        } else {
                            soundRecyclerAdapter.removeItem(null);
                            Log.w(TAG, "Error getting documents.", task.getException());
                            if (view != null)
                                Snackbar.make(view.findViewById(android.R.id.content), "Unable to fetch records!",
                                        BaseTransientBottomBar.LENGTH_INDEFINITE).show();
                        }
                    });
        }
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) &&
                        !isLastItemReached && !soundModelArrayList.contains(null)) {
                    isScrolling = false;
                    soundRecyclerAdapter.addItem(null);
                    db.collection("sounds")
                            .orderBy("type")
                            .startAfter(lastVisibleSound).limit(limit)
                            .get()
                            .addOnCompleteListener(t -> {
                                if (t.isSuccessful()) {
                                    soundRecyclerAdapter.removeItem(null);
                                    for (DocumentSnapshot document : Objects.requireNonNull(t.getResult())) {
                                        SoundModel soundModel = new SoundModel(
                                                document.getId(),
                                                document.getString("type"),
                                                document.getString("media"),
                                                (List<String>) document.get("keywords")
                                        );
                                        soundRecyclerAdapter.addItem(soundModel);
                                    }
                                    lastVisibleSound = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                    if (t.getResult().size() < limit) {
                                        isLastItemReached = true;
                                    }
                                } else
                                    soundRecyclerAdapter.removeItem(null);
                            });
                }
            }
        };
        recyclerView.addOnScrollListener(onScrollListener);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (soundModelArrayList.get(position) != null) playPosition.setValue(position);

            }

            @Override
            public void onLongClick(View view, int position) {
                if (soundModelArrayList.get(position) != null)
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you  sure you want to delete this sound? This action cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> db.collection("sounds")
                                    .document(soundModelArrayList.get(position).getId())
                                    .delete()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Sound deleted Successfully!", Toast.LENGTH_SHORT).show();
                                            soundRecyclerAdapter.notifyItemRemoved(position);
                                            soundModelArrayList.remove(position);
                                        } else {
                                            Toast.makeText(context, "Could not delete sound! Please try again later", Toast.LENGTH_SHORT).show();
                                        }

                                    }))
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
            }
        }));


    }
}
