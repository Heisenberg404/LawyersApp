package com.herprogramacion.miAppAbogados.lawyerdetail;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.herprogramacion.miAppAbogados.R;
import com.herprogramacion.miAppAbogados.addeditlawyer.AddEditLawyerActivity;
import com.herprogramacion.miAppAbogados.data.Lawyer;
import com.herprogramacion.miAppAbogados.data.LawyersDbHelper;
import com.herprogramacion.miAppAbogados.lawyers.LawyersActivity;
import com.herprogramacion.miAppAbogados.lawyers.LawyersFragment;

/**
 * Vista para el detalle del abogado
 */
public class LawyerDetailFragment extends Fragment {
    private static final String ARG_LAWYER_ID = "lawyerId";

    private String mLawyerId;

    private CollapsingToolbarLayout mCollapsingView;
    private ImageView mAvatar;
    private TextView mPhoneNumber;
    private TextView mSpecialty;
    private TextView mBio;

    private LawyersDbHelper mLawyersDbHelper;


    public LawyerDetailFragment() {
        // Required empty public constructor
    }

    public static LawyerDetailFragment newInstance(String lawyerId) {
        LawyerDetailFragment fragment = new LawyerDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LAWYER_ID, lawyerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mLawyerId = getArguments().getString(ARG_LAWYER_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lawyer_detail, container, false);
        mCollapsingView = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        mAvatar = (ImageView) getActivity().findViewById(R.id.iv_avatar);
        mPhoneNumber = (TextView) root.findViewById(R.id.tv_phone_number);
        mSpecialty = (TextView) root.findViewById(R.id.tv_specialty);
        mBio = (TextView) root.findViewById(R.id.tv_bio);

        mLawyersDbHelper = new LawyersDbHelper(getActivity());

        loadLawyer();

        return root;
    }

    private void loadLawyer() {
        new GetLawyerByIdTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                showEditScreen();
                break;
            case R.id.action_delete:
                new DeleteLawyerTask().execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LawyersFragment.REQUEST_UPDATE_DELETE_LAWYER) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        }
    }

    private void showLawyer(Lawyer lawyer) {
        mCollapsingView.setTitle(lawyer.getName());
        Glide.with(this)
                .load(Uri.parse("file:///android_asset/" + lawyer.getAvatarUri()))
                .centerCrop()
                .into(mAvatar);
        mPhoneNumber.setText(lawyer.getPhoneNumber());
        mSpecialty.setText(lawyer.getSpecialty());
        mBio.setText(lawyer.getBio());
    }

    private void showEditScreen() {
        Intent intent = new Intent(getActivity(), AddEditLawyerActivity.class);
        intent.putExtra(LawyersActivity.EXTRA_LAWYER_ID, mLawyerId);
        startActivityForResult(intent, LawyersFragment.REQUEST_UPDATE_DELETE_LAWYER);
    }

    private void showLawyersScreen(boolean requery) {
        if (!requery) {
            showDeleteError();
        }
        getActivity().setResult(requery ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private void showLoadError() {
        Toast.makeText(getActivity(),
                "Error al cargar información", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteError() {
        Toast.makeText(getActivity(),
                "Error al eliminar abogado", Toast.LENGTH_SHORT).show();
    }

    private class GetLawyerByIdTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            return mLawyersDbHelper.getLawyerById(mLawyerId);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.moveToLast()) {
                showLawyer(new Lawyer(cursor));
            } else {
                showLoadError();
            }
        }

    }

    private class DeleteLawyerTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            return mLawyersDbHelper.deleteLawyer(mLawyerId);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            showLawyersScreen(integer > 0);
        }

    }

}
