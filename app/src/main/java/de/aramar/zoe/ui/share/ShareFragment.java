package de.aramar.zoe.ui.share;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import de.aramar.zoe.R;

public class ShareFragment extends Fragment {

    private ShareViewModel shareViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.shareViewModel = new ViewModelProvider(this).get(ShareViewModel.class);
        View root = inflater.inflate(R.layout.fragment_share, container, false);
        final TextView textView = root.findViewById(R.id.text_share);
        this.shareViewModel
                .getText()
                .observe(this.getViewLifecycleOwner(), s -> textView.setText(s));
        return root;
    }
}