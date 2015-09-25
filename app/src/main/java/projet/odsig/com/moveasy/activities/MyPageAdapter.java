package projet.odsig.com.moveasy.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

// Adapter pour l'aide
public class MyPageAdapter extends FragmentPagerAdapter {

    private final List fragments;

    //On fournit à l'adapter la liste des fragments à afficher
    public MyPageAdapter(FragmentManager fm, List fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return (Fragment)this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}