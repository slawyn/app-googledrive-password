package com.example.gdrivepasswordvault;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gdrivepasswordvault.cards.CustomLayoutManager;
import com.example.gdrivepasswordvault.cards.RecyclerViewAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.Collections;
import java.util.Vector;

// https://dev.to/mesadhan/google-drive-api-with-android-4m2e
// https://developer.android.com/studio/publish/app-signing#signing-manually
public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher {
    private static final String TAG = "GDrivePasswordVault";

    private final int PERMISSION_NONE = 0;
    private final int PERMISSION_ACCOUNTS_REQUEST_CODE = 1;
    private final int PERMISSION_INTERNET_REQUEST_CODE = 2;
    private final int PERMISSIONS_REQUIRED = PERMISSION_ACCOUNTS_REQUEST_CODE|PERMISSION_INTERNET_REQUEST_CODE;
    private int mPermissionsGranted;

    private DriveServiceHelper mDriveServiceHelper;
    private RecyclerViewAdapter recycleViewAdapter;
    private RecyclerView recyclerView;
    private TextView tFilter;
    private TextView tNumberOfItems;
    private Button[] mButtons;
    private ProgressBar pbProgressBar;


    private String mOpenFileId;
    private static String mFileTitleName;
    private static byte[] mFileContent;
    private String mEncryptionKey = "";


    private boolean mFileFetched;

    private final int REQUEST_SIGNIN_CODE = 1;
    public final int LOGIN_NONE = 0;
    public final int LOGIN_SUCCESS = 1;
    public final int LOGIN_FAILURE = 2;
    public int mLoggedIn;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdata);

        mPermissionsGranted = PERMISSION_NONE;
        mLoggedIn = LOGIN_NONE;

        mFileFetched = false;
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        recyclerView.setLayoutManager(new CustomLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        recycleViewAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(recycleViewAdapter);

        // Get Ui Elements
        tFilter = findViewById(R.id.tFilter);
        tNumberOfItems = findViewById(R.id.tNumberOfItems);
        pbProgressBar = findViewById(R.id.pbProgressbar);

        mButtons = new Button[4];
        mButtons[0] =  findViewById(R.id.bPush);
        mButtons[1] =  findViewById(R.id.bPull);
        mButtons[2] =  findViewById(R.id.bAdd);
        mButtons[3] =  findViewById(R.id.bResetFilter);

        // Visibility
        mButtons[3].setVisibility(View.INVISIBLE);
        pbProgressBar.setVisibility(View.INVISIBLE);

        // Handlers
        for(int idx=0;idx<mButtons.length;idx++) {
            mButtons[idx].setOnClickListener(this);
            mButtons[idx].setEnabled(false);
        }
        tFilter.addTextChangedListener(this);
        tFilter.setOnFocusChangeListener(this);
        tFilter.setEnabled(false);

        Logger.setup(this, findViewById(R.id.nagivation_main).findViewById(R.id.logger));
        Logger.log("Checking Permissions",false);

        // Check and request missing permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, PERMISSION_INTERNET_REQUEST_CODE);
        } else {
            mPermissionsGranted |= PERMISSION_INTERNET_REQUEST_CODE;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
           requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSION_ACCOUNTS_REQUEST_CODE);
        } else {
            mPermissionsGranted |= PERMISSION_ACCOUNTS_REQUEST_CODE;
        }

        // Permissions are there
        if(mPermissionsGranted == PERMISSIONS_REQUIRED){
            requestSignIn();
        }
    }

    /**
     * Starts a sign-in activity using {@link #}.
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void scrollToPosition(int idx){
        recyclerView.scrollToPosition(idx);
    }

    public void showNumberOfItems(String num){
        tNumberOfItems.setText(num);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCOUNTS_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionsGranted |= PERMISSION_ACCOUNTS_REQUEST_CODE;
                }  else {
                    Logger.log( "Error: accounts permission!", true);

                }
                break;
            case PERMISSION_INTERNET_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionsGranted |= PERMISSION_INTERNET_REQUEST_CODE;
                }  else {
                    Logger.log( "Error: internet permission!",true);

                }
                break;

        }

        // Request sign in if the permissions are there
        if(mPermissionsGranted == PERMISSIONS_REQUIRED){
            requestSignIn();
        }
    }


    public void requestSignIn() {
        Logger.log("Requesting sign-in",false);
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(),REQUEST_SIGNIN_CODE );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_SIGNIN_CODE:
                // Exit if canceled
                if(resultCode == Activity.RESULT_CANCELED){
                    Logger.log( "Error: Action canceled", true);
                    this.finishAffinity();
                }

                if (resultCode == Activity.RESULT_OK) {
                    if(resultData == null){
                        Logger.log( "Error: Intent-Result is null",true);
                    } else {
                        handleSignInResult(resultData);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                    enableEditing(true);
                    Logger.log("Signed in!",true);
                    mLoggedIn = LOGIN_SUCCESS;

                })

                .addOnFailureListener(exception -> { Logger.log("Error: Unable to sign in",false);  mLoggedIn = LOGIN_FAILURE; });
    }

    private void openFile(String fileId) {
        if (mDriveServiceHelper != null) {
            pbProgressBar.setVisibility(View.VISIBLE);
            Logger.log( "Opening " + fileId, false);

            mDriveServiceHelper.readFile( fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        mFileTitleName = nameAndContent.first;
                        byte[] array = nameAndContent.second;

                        mFileContent = Encryption.blowfish(array,mEncryptionKey,false);

                        if(mFileContent != null && recycleViewAdapter.parseDownloadedData(mFileContent)){
                            Logger.log("Data fetched",true);
                            mFileFetched = true;
                        }else{
                            Logger.log("Error: Data is erroneous",true);
                            mFileFetched = false;
                        }

                        pbProgressBar.setVisibility(View.INVISIBLE);

                    })
                    .addOnFailureListener(exception -> {
                                Logger.log("Error: Unable to fetch data",true);
                                pbProgressBar.setVisibility(View.INVISIBLE);
                            }
                           );
        }
    }

    /**
     * Creates a new file via the Drive REST API.
     */
    private void createFile() {
        if (mDriveServiceHelper != null) {
            Logger.log( "Creating a file",false);

            mDriveServiceHelper.createFile()
                    .addOnSuccessListener(fileId -> mDriveServiceHelper.readFile(fileId))
                    .addOnFailureListener(exception ->
                            Logger.log( "Error: Couldn't create file."+ exception,true));
        }
    }

    /**
     * Saves the currently opened file created via {@link #createFile()} if one exists.
     */
    private void saveFile() {
        if (mDriveServiceHelper != null && mOpenFileId != null) {
            Logger.log( "Saving " + mOpenFileId,false);

            mDriveServiceHelper.saveFile(mOpenFileId, mFileTitleName, mFileContent)
                    .addOnSuccessListener(runnable -> {
                        Logger.log( "Data pushed", true);
                        pbProgressBar.setVisibility(View.INVISIBLE);
                    })
                    .addOnFailureListener(exception -> {
                                Logger.log(  "Error: Unable to save file via REST."+ exception,false);
                                pbProgressBar.setVisibility(View.INVISIBLE);
                            });
        }
    }

    /**
     * Queries the Drive REST API for files visible to this app and lists them in the content view.
     */
    private void queryFiles() {
        if (mDriveServiceHelper != null) {
            pbProgressBar.setVisibility(View.VISIBLE);
            Logger.log(  "Querying for files.",false);
            mDriveServiceHelper.queryFiles()
                    .addOnSuccessListener(fileList -> {
                        pbProgressBar.setVisibility(View.INVISIBLE);
                        Vector<String> queriedIds = new Vector<>();
                        for (File file : fileList.getFiles()) {
                            queriedIds.add(file.getId());
                            Logger.log(file.getName(),false);
                        }

                        if(queriedIds.size() == 1) {
                            mOpenFileId = queriedIds.get(0);
                            Logger.log("Opening "+mOpenFileId,false);
                            openFile(mOpenFileId);
                        }
                        else{
                            Logger.log("Error: multiple files",true);
                        }
                    })
                    .addOnFailureListener(exception -> {Logger.log(  "Error: Unable to query files."+ exception,false);               pbProgressBar.setVisibility(View.INVISIBLE);});
        }
    }

    public void enableEditing(boolean enable){
        for(int idx=0;idx<mButtons.length;idx++) {
            mButtons[idx].setEnabled(enable);
        }
        tFilter.setEnabled(enable);
    }

    private void askForPassword(){

        final EditText encrypt = new EditText(this);
        encrypt.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        encrypt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme))
        .setTitle("Verification")
        .setMessage("Enter your password")
        .setCancelable(false)
        .setView(encrypt);
        alert.setPositiveButton("Go", (dialog, whichButton) -> {
            mEncryptionKey = encrypt.getText().toString();
            queryFiles();
        })
        .show();/**/
    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bPull:
                //createFile();
                askForPassword();
                break;
            case R.id.bPush:
                if(mFileFetched ){
                    pbProgressBar.setVisibility(View.VISIBLE);
                    mFileContent = Encryption.blowfish(recycleViewAdapter.getDataToUpload(),mEncryptionKey,true);
                    if(mFileContent != null)
                        saveFile();
                }
                break;
            case R.id.bAdd:
                if(mFileFetched ) {
                   recycleViewAdapter.addNewCard();
                }
                break;
            case R.id.bResetFilter:
                hideKeyboard();
                tFilter.clearFocus();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(b){
            tFilter.setText("");
        }else{
            tFilter.setText("Filter");
            mButtons[3].setVisibility(View.INVISIBLE);
        }

        Log.d("onFocusChange",b+"");
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void afterTextChanged(Editable editable) {
           String search =  editable.toString();
           if(tFilter.isFocused()){
               recycleViewAdapter.setFilter(search);
               mButtons[3].setVisibility(View.VISIBLE);
           }else{
               recycleViewAdapter.setFilter("");
               mButtons[3].setVisibility(View.INVISIBLE);
           }
    }
}