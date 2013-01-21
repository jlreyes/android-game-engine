package com.jlreyes.libraries.android_game_engine.io.storage.external;

import android.content.Context;
import android.os.Environment;
import com.jlreyes.libraries.android_game_engine.io.storage.StorageHelper;
import com.jlreyes.libraries.android_game_engine.utils.Utils;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.DataStreamEndedEarlyException;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.MissingFileException;

import java.io.*;
import java.nio.ByteBuffer;

public class ExternalStorageHelper {
    /**
     * Returns true iff the external media is read/writeable.
     */
    public static boolean MediaMounted() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean FileExists(Context context,
                                     String fileName) throws IOException {
        if (MediaMounted() == false)
            throw new IOException("Something went wrong when trying to mount " +
                                  "external storage.");
        File file = new File(context.getExternalFilesDir(null), fileName);
        return file.exists();
    }

    /**
     * Opens a file for writing. Creates a new file if one doesn't exist and
     * truncates any existing file.
     * @throws IOException
     */
    public static BufferedOutputStream WriteFile(Context context,
                                                 String fileName)
            throws IOException {        /* Checking if the media is mounted */
        if (MediaMounted() == false)
            throw new IOException("Something went wrong when trying to mount " +
                                  "external storage.");
        /* Deleting the file if it exists */
        DeleteFile(context, fileName);
        /* Creating the file */
        File file = new File(context.getExternalFilesDir(null), fileName);
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    /**
     * Opens a file with the given name. Returns a bufferedinputstream.
     * @throws IOException
     * @throws MissingFileException
     */
    public static BufferedInputStream OpenFile(Context context,
                                               String fileName)
            throws IOException,
                   MissingFileException {        /* Checking if the media is mounted */
        if (MediaMounted() == false)
            throw new IOException("Something went wrong when trying to mount " +
                                  "external storage.");
		/* Creating the file */
        File file = new File(context.getExternalFilesDir(null), fileName);
		/* Making sure the file exists */
        if (file.exists() == false) throw new MissingFileException();
		/* Returning an inputStream */
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Deletes the given filename it exists.
     * @throws IOException
     */
    public static void DeleteFile(Context context,
                                  String fileName) throws IOException {
		/* Checking if the media is mounted */
        if (MediaMounted() == false)
            throw new IOException("Something went wrong when trying to mount " +
                                  "external storage.");
		/* Creating the file */
        File file = new File(context.getExternalFilesDir(null), fileName);
		/* Making sure the file exists */
        if (file.exists() == false) return;
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     * Counts the number of bytes left in the inputstream by reading
     * through all of them. Only supports files <= 2gb. NO BYTES ARE AVAILABLE
     * AFTERWARDS.
     * @throws IOException
     */
    public static int GetNumberOfBytesLeft(BufferedInputStream data)
            throws IOException {
        int numBytes = 0;
        data.mark(Integer.MAX_VALUE); // Hopefully no files > 2gb...
        while (data.read() != -1) numBytes += 1;
        data.reset();
        return numBytes;
    }

    /**
     * Given an inputstream, reads the given number of bytes from it and returns
     * an array of those bytes.
     * @throws IOException
     * @throws DataStreamEndedEarlyException
     */
    public static byte[] ReadFromInputStream(BufferedInputStream inputStream,
                                             int bytes)
            throws IOException, DataStreamEndedEarlyException {
        byte[] buffer = new byte[bytes];
        int bytesRead = inputStream.read(buffer, 0, bytes);
        if (bytesRead == -1)
            throw new DataStreamEndedEarlyException("File ended prematurely.");
        else if (bytesRead != bytes)
            throw new DataStreamEndedEarlyException();
        else return buffer;
    }

    /**
     * Same as ReadFromInputStream(inputStream, 1) except that it returns a byte
     * @throws IOException
     * @throws DataStreamEndedEarlyException
     */
    public static byte ReadByteFromInputStream(BufferedInputStream inputStream)
            throws IOException, DataStreamEndedEarlyException {
        byte[] bytes = ReadFromInputStream(inputStream, 1);
        return bytes[0];
    }

    /**
     * Same as ReadFromInputStream(inputStream, 2) except that it returns a char.
     * @throws IOException
     * @throws DataStreamEndedEarlyException
     */
    public static char ReadCharFromInputStream(BufferedInputStream inputStream)
            throws IOException, DataStreamEndedEarlyException {
        byte[] bytes = ReadFromInputStream(inputStream, 2);
        return Utils.CharFromByteArray(bytes, 0, StorageHelper.ENDIAN);
    }

    /**
     * Same as ReadFromInputStream(inputStream, 4) except that it returns an int.
     * @throws IOException
     * @throws DataStreamEndedEarlyException
     */
    public static int ReadIntFromInputStream(BufferedInputStream inputStream)
            throws IOException, DataStreamEndedEarlyException {
        byte[] bytes = ReadFromInputStream(inputStream, 4);
        return Utils.IntFromByteArray(bytes, 0, StorageHelper.ENDIAN);
    }

    /**
     * Same as ReadFromInputStream(inputStream, 8) except that it returns a long.
     * @throws IOException
     * @throws DataStreamEndedEarlyException
     */
    public static long ReadLongFromInputStream(BufferedInputStream inputStream)
            throws IOException, DataStreamEndedEarlyException {
        byte[] bytes = ReadFromInputStream(inputStream, 8);
        return Utils.LongFromByteArray(bytes, 0, StorageHelper.ENDIAN);
    }

    /**
     * Reads chars from the input stream until a null character is encountered.
     * @throws IOException
     * @throws DataStreamEndedEarlyException
     */
    public static String ReadStringFromInputStream(BufferedInputStream inputStream)
            throws IOException, DataStreamEndedEarlyException {
        StringBuilder string = new StringBuilder();
        while (true) {
            char c = ReadCharFromInputStream(inputStream);
            if (c == '\u0000') break;
            else string.append(c);
        }
        return string.toString();
    }

    /**
     * Given a bytebuffer, writes everything remaining in the bytebuffer to the
     * output stream.
     * @throws IOException
     */
    public static void WriteBuffToOutputStream(BufferedOutputStream outputStream,
                                               ByteBuffer bytes) throws IOException {
        int length = bytes.remaining();
        byte[] bArray = new byte[length];
        bytes.get(bArray);
        outputStream.write(bArray);
    }

    /**
     * Given an outputStream and a long, writes the value to the output stream.
     * @throws IOException
     */
    public static void WriteLongToOutputStream(BufferedOutputStream outputStream,
                                               long l) throws IOException {
        byte[] bytes = Utils.LongToByteArray(l, StorageHelper.ENDIAN);
        outputStream.write(bytes);
    }

    /**
     * Given an outputStream and a int, writes the value to the output stream.
     * @throws IOException
     */
    public static void WriteIntToOutputStream(BufferedOutputStream outputStream,
                                              int i) throws IOException {
        byte[] bytes = Utils.IntToByteArray(i, StorageHelper.ENDIAN);
        outputStream.write(bytes);
    }

    /**
     * Given an outputStream and a char, writes the value to the output stream.
     * @throws IOException
     */
    public static void WriteCharToOutputStream(BufferedOutputStream outputStream,
                                               char c) throws IOException {
        byte[] bytes = Utils.CharToByteArray(c, StorageHelper.ENDIAN);
        outputStream.write(bytes);
    }

    /**
     * Given an outputStream and a byte, writes the value to the output stream.
     * @throws IOException
     */
    public static void WriteByteToOutputStream(BufferedOutputStream outputStream,
                                               byte b) throws IOException {
        byte[] bytes = new byte[]{b};
        outputStream.write(bytes);
    }


}