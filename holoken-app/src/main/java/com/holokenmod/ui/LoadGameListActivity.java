package com.holokenmod.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holokenmod.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LoadGameListActivity extends AppCompatActivity implements LoadGameListAdapter.ItemClickListener {
	private LoadGameListAdapter mAdapter;
	private View empty;
	
	public LoadGameListActivity() {
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		
		super.onCreate(savedInstanceState);
		
		if (!PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("showfullscreen", false)) {
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		setContentView(R.layout.activity_savegame);
		
		empty = findViewById(android.R.id.empty);
		
		RecyclerView recyclerView = findViewById(android.R.id.list);
		
		int relativeWidth = (int) (getResources().getDisplayMetrics().widthPixels
				/ getResources().getDisplayMetrics().density);
				
		int columns = relativeWidth / 180;
		
		if (columns < 1) {
			columns = 1;
		}
		
		recyclerView.setLayoutManager(new GridLayoutManager(this, columns));
		
		this.mAdapter = new LoadGameListAdapter(this);
		this.mAdapter.setClickListener(this);
		recyclerView.setAdapter(this.mAdapter);
		
		if (mAdapter.getItemCount() == 0) {
			empty.setVisibility(View.VISIBLE);
		}
		
		MaterialToolbar appBar = findViewById(R.id.saveGameAppBar);
	
		appBar.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				case R.id.discardbutton:
					deleteAllGamesDialog();
					
					return true;
				default:
					return false;
			}
		});
		
		appBar.setNavigationOnClickListener(v -> {
			LoadGameListActivity.this.setResult(Activity.RESULT_CANCELED);
			LoadGameListActivity.this.finish();
		});
		
		numberOfSavedGamesChanged();
	}
	
	public void deleteSaveGame(final File filename) {
		filename.delete();
		mAdapter.refreshFiles();
		mAdapter.notifyDataSetChanged();
		
		numberOfSavedGamesChanged();
	}
	
	public void deleteAllSaveGames() {
		for (final File file : getSaveGameFiles()) {
			file.delete();
		}
		
		mAdapter.refreshFiles();
		mAdapter.notifyDataSetChanged();
		
		numberOfSavedGamesChanged();
	}
	
	private void numberOfSavedGamesChanged() {
		if (mAdapter.getItemCount() == 0) {
			empty.setVisibility(View.VISIBLE);
			findViewById(R.id.discardbutton).setEnabled(false);
		} else {
			empty.setVisibility(View.GONE);
			findViewById(R.id.discardbutton).setEnabled(true);
		}
	}
	
	@Nullable
	List<File> getSaveGameFiles() {
		final File dir = this.getFilesDir();
		
		return Arrays.asList(dir.listFiles((dir1, name) -> name.startsWith("savegame_")));
	}
	
	public void deleteGameDialog(final File filename) {
		new MaterialAlertDialogBuilder(this)
				.setTitle(getResources().getString(R.string.dialog_delete_title))
				.setMessage(getResources().getString(R.string.dialog_delete_msg))
				.setNegativeButton(getResources().getString(R.string.dialog_cancel), (dialog, whichButton) -> dialog.cancel())
				.setPositiveButton(getResources().getString(R.string.dialog_ok), (dialog, whichButton) -> LoadGameListActivity.this
						.deleteSaveGame(filename))
				.show();
	}
	
	public void deleteAllGamesDialog() {
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.dialog_delete_all_title)
				.setMessage(R.string.dialog_delete_all_msg)
				.setNegativeButton(R.string.dialog_cancel, (dialog, whichButton) -> dialog.cancel())
				.setPositiveButton(R.string.dialog_ok, (dialog, whichButton) -> LoadGameListActivity.this
						.deleteAllSaveGames())
				.show();
	}
	
	public void loadSaveGame(final File filename) {
		final Intent i = new Intent().putExtra("filename", filename.getAbsolutePath());
		
		setResult(Activity.RESULT_OK, i);
		finish();
	}
	
	@Override
	public void onItemClick(View view, int position) {
		loadSaveGame(getSaveGameFiles().get(position));
	}
}