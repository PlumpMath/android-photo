package cs213.selmon.androidphoto;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cs213.selmon.androidphoto.model.Album;
import cs213.selmon.androidphoto.util.DataStore;
import cs213.selmon.androidphoto.util.PhotoScanner;

public class AlbumListActivity extends ListActivity {


  private DataStore mDataStore;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    scanDefaultPhotos();
    
    mDataStore = ((PhotoApplication) this.getApplication()).getDataStore();
    mDataStore.restoreStateFromDisk();

    setContentView(R.layout.album_list);

    Button addButton = (Button) findViewById(R.id.add_button);
    addButton.setOnClickListener( new View.OnClickListener() {      
      @Override
      public void onClick(View v) {
        showNewAlbumDialog();
      }
    });
    registerForContextMenu(getListView());

    refresh();
  }
  
  private void scanDefaultPhotos() {
    // TODO Don't run this more than once
    
    PhotoScanner scanner = new PhotoScanner(this);
    scanner.scanDefaultPhotos();
    
  }

  @Override
  protected void onStop() {
    mDataStore.saveStateToDisk();    
    
    super.onStop();
  }
  
  

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    
    AlbumListAdapter listAdapter = (AlbumListAdapter) getListAdapter();
    Album album = (Album) listAdapter.getItem(position);
    
    PhotoApplication application = ((PhotoApplication) this.getApplication());
    application.setCurrentAlbum(album);

    // Load new activity
    
    Intent intent = new Intent(this, AlbumDetailActivity.class);
    startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
                                  ContextMenuInfo menuInfo) {
    if (v == getListView()) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
      menu.setHeaderTitle("");
      String[] menuItems = getResources().getStringArray(R.array.album_list_context_choices);
      for (int i = 0; i<menuItems.length; i++) {
        menu.add(Menu.NONE, i, i, menuItems[i]);
      }
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    int menuItemIndex = item.getItemId();
    
    String[] menuItems = getResources().getStringArray(R.array.album_list_context_choices);
    String menuItemName = menuItems[menuItemIndex];

    Album album = (Album) getListAdapter().getItem(info.position);
    String listItemName = album.getName();

    if (menuItemName.equals("Rename")) {
      showRenameDialogForAlbum(album);
    } else if (menuItemName.equals("Delete")) {
      deleteAlbum(album);
    }

    // Handle which option was chosen

    //TextView text = (TextView)findViewById(R.id.footer);
    //text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));

    return true;
  }
  
  private void showRenameDialogForAlbum(final Album album) {
    final EditText textEntryView = new EditText(this);
    textEntryView.setText(album.getName());
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Rename Album")
    .setView(textEntryView)
    .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        /* User clicked OK so do some stuff */
        album.setName(textEntryView.getText().toString());
      }
    })
    .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        /* User clicked cancel so do some stuff */
      }
    })
    .show();

  }
  
  private void deleteAlbum(Album album) {
    mDataStore.deleteAlbum(album);
    refresh();
  }

  private void addNewAlbum(String name) {
    Album newAlbum = new Album(name);
    mDataStore.addAlbum(newAlbum);
    refresh();
  }

  private void showNewAlbumDialog() {

    final EditText textEntryView = new EditText(this);
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("New Album")
    .setView(textEntryView)
    .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        /* User clicked OK so do some stuff */
        addNewAlbum(textEntryView.getText().toString());
      }
    })
    .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        /* User clicked cancel so do some stuff */
      }
    })
    .show();
  }
  
  private void refresh() {
    setListAdapter(new AlbumListAdapter(
        mDataStore.getAlbums()
        ));
  }

  private class AlbumListAdapter implements ListAdapter {

    private List<Album> mAlbums;

    public AlbumListAdapter(List<Album> albums) {
      mAlbums = albums;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
      // TODO Implement this later
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
      // TODO Implement this later      
    }

    @Override
    public int getCount() {
      return mAlbums.size();
    }

    @Override
    public Object getItem(int position) {
      return mAlbums.get(position);
    }

    @Override
    public long getItemId(int position) {
      //return 0;
      //return position;
      return mAlbums.get(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
      return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = getLayoutInflater();
      View row = inflater.inflate(R.layout.album_row, null);
      TextView text = (TextView) row.findViewById(R.id.text);
      Album album = mAlbums.get(position);
      text.setText(album.getName());

      return(row);
    }

    @Override
    public int getItemViewType(int position) {
      return 1;
    }

    @Override
    public int getViewTypeCount() {
      return 1;
    }

    @Override
    public boolean isEmpty() {
      return getCount() == 0;
    }

    @Override
    public boolean areAllItemsEnabled() {      
      return true;
    }

    @Override
    public boolean isEnabled(int position) {
      return true;
    }

  }

}
