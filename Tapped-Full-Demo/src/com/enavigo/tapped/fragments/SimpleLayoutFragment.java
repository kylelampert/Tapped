package com.enavigo.tapped.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SimpleLayoutFragment extends Fragment {
	
	private int layoutResourceId;
	
	public SimpleLayoutFragment(int layoutResourceId){
		this.layoutResourceId = layoutResourceId;
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		// Inflate the layout for this fragment
		View view = inflater.inflate(layoutResourceId, container, false);
		return initializeView(view);
	}
	
	// Sub-classes can override to change the view
	protected View initializeView(View view){
		return view;
	}
}
