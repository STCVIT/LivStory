package in.stcvit.livstory;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import in.stcvit.livstory.modal.RequestModel;
import in.stcvit.livstory.retrofit.RetrofitInstance;
import in.stcvit.livstory.retrofit.RetrofitInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReportFragment extends BottomSheetDialogFragment {
    private static final String TAG = "ReportFragment";
    private Context context;
    private TextInputEditText word;
    private TextInputLayout word1;
    private ProgressDialog progressDialog;

    public ReportFragment() {
    }

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        progressDialog = new ProgressDialog(context);
        word = view.findViewById(R.id.word);
        word1 = view.findViewById(R.id.word1);
        view.findViewById(R.id.submit).setOnClickListener(v -> {
            if (word.getText().length() != 0)
                postData(word.getText().toString());
            else
                word1.setError("Invalid Word!");
        });
        return view;
    }

    private void postData(String word) {
        progressDialog.show();
        Retrofit retrofit = RetrofitInstance.getRetrofitInstance();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<Object> call = retrofitInterface.postWord(new RequestModel(word));
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call,
                                   @NonNull Response<Object> response) {
                progressDialog.dismiss();
                Log.d(TAG, "onResponse() returned: " + response.body());
                if (response.body().toString().equalsIgnoreCase("success")) {
                    dismiss();
                    new MaterialAlertDialogBuilder(context)
                            .setMessage("Word successfully posted")
                            .setPositiveButton("Dismiss", (dialog, which) -> dialog.dismiss())
                            .show();
                } else
                    Toast.makeText(context, "Could not post word suggestion", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }
}
