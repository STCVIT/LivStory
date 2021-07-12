package com.yashkasera.livstoryuploaddata.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashkasera.livstoryuploaddata.R;
import com.yashkasera.livstoryuploaddata.UploadDataActivity;
import com.yashkasera.livstoryuploaddata.adapter.MissingWordRecyclerAdapter;
import com.yashkasera.livstoryuploaddata.model.WordModel;
import com.yashkasera.livstoryuploaddata.util.ClickListener;
import com.yashkasera.livstoryuploaddata.util.RecyclerTouchListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

import static com.yashkasera.livstoryuploaddata.MainActivity.modelWordModelArrayList;
import static com.yashkasera.livstoryuploaddata.MainActivity.userWordModelArrayList;

public class UserMissingWords extends Fragment {
    private static final String TAG = "UserMissingWords";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MissingWordRecyclerAdapter adapter;
    private Context context;

    public UserMissingWords() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = requireContext();
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002 && resultCode == 1) {
            if (data != null) {
                modelWordModelArrayList.remove(data.getIntExtra("position", -1));
                adapter.notifyItemRemoved(data.getIntExtra("position", -1));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new MissingWordRecyclerAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        if (userWordModelArrayList.size() != 0) {
            adapter.setList(userWordModelArrayList);
        } else {
            db.collection("report")
                    .document("user")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<WordModel> list = new ArrayList<>();
                            for (Map.Entry<String, Object> entry : task.getResult().getData().entrySet())
                                list.add(new WordModel(entry.getKey(), (long) entry.getValue()));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                list.sort((o1, o2) -> o1.getCount() >= o2.getCount() ? -1 : 1);
                            userWordModelArrayList.addAll(list);
                            adapter.setList(userWordModelArrayList);
                        } else
                            Log.d(TAG, "onComplete() returned: " + task.getException());
                    });
        }
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(context, UploadDataActivity.class);
                intent.putExtra("word", userWordModelArrayList.get(position));
                intent.putExtra("from", "user");
                intent.putExtra("position", position);
                startActivityForResult(intent, 1002);
            }

            @Override
            public void onLongClick(View view, int position) {
                if (userWordModelArrayList.get(position) != null)
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you  sure you want to delete this sound? This action cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> db.collection("report")
                                    .document("user")
                                    .update(userWordModelArrayList.get(position).getWord(), FieldValue.delete())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Suggestion deleted Successfully!", Toast.LENGTH_SHORT).show();
                                            adapter.notifyItemRemoved(position);
                                            userWordModelArrayList.remove(position);
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
