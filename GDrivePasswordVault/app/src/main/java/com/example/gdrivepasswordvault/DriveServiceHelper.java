package com.example.gdrivepasswordvault;

import android.util.Log;
import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its mTitle and
     * contents.
     */
    public Task<Pair<String, byte[]>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            Logger.log("Reading file "+name,false);

            Vector<byte[]> vector = new Vector<>();

            byte[] output = null;
            try {

                InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                DataInputStream dis = new DataInputStream(is);

                int receivedsize = 0;

                do{
                    byte[] data = new byte[512];
                    int nm = dis.read(data);

                    if(nm < 512) {
                        data = Arrays.copyOfRange(data, 0, nm);
                    }

                    vector.add(data);
                    receivedsize+=nm;

                }while(dis.available() >0);

                output = new byte[receivedsize];
                int offset = 0;
                for(int i=0;i<vector.size();i++){

                    byte[] arr = vector.get(i);
                    System.arraycopy(arr,0,output, offset, arr.length);
                    offset +=arr.length;
                }

                Log.d("READ-FILE",output.length+" ");
                is.close();
            }
            catch(Exception e){
                Logger.log("Couldn't read stream",false);
            }

            return Pair.create(name, output);
    });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code mTitle} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, byte[] content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = new ByteArrayContent(null,content,0,content.length);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () -> mDriveService.files().list().setSpaces("drive").execute());
    }
}