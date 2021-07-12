package com.yashkasera.livstoryuploaddata;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.yashkasera.livstoryuploaddata.fragment.ModelMissingWords;
import com.yashkasera.livstoryuploaddata.fragment.PlayAudioFragment;
import com.yashkasera.livstoryuploaddata.fragment.UserMissingWords;

public class ViewPagerAdapter extends FragmentPagerAdapter {


    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return new PlayAudioFragment();
        else if (position == 1)
            return new ModelMissingWords();
        else
            return new UserMissingWords();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0)
            return "Audio";
        else if (position == 1)
            return "Model";
        else
            return "User";
    }

}
