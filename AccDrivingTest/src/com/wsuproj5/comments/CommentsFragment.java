package com.wsuproj5.comments;

import com.wsuproj5.accuratedrivingtest.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class CommentsFragment extends Fragment {
	public CommentTemplates currentFragment;
	public String commentTemplate;
	public boolean returnTemplate = false;
	public boolean currentView = false;
	private View v;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		v = inflater.inflate(R.layout.comments_menu, container, false);
		
		return v;
    }
	public void setCommentContent() {
		EditText commentContent = (EditText) v.findViewById(R.id.Field_Comment);
		commentContent.setText(commentTemplate);
	}
}
