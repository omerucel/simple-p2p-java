package p2p;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

public class DownloadData {
    private DownloadManager downloadManager;
    private String fileHash;
    private int totalPart;
    private int fileSize;
    private File downloadPath;
    private ArrayList<Integer> pendingParts;
    private ArrayList<Integer> downloadingParts;
    private ArrayList<Integer> downloadedParts;

    public DownloadData(DownloadManager downloadManager, String fileHash, int fileSize, int totalPart, File downloadPath)
    {
        this.downloadManager = downloadManager;
        this.fileSize = fileSize;
        this.totalPart = totalPart;
        this.downloadPath = downloadPath;
        this.fileHash = fileHash;
        pendingParts = new ArrayList<Integer>();
        downloadedParts = new ArrayList<Integer>();
        downloadingParts = new ArrayList<Integer>();

        for(int i=0; i<totalPart;i++)
            pendingParts.add(i+1);
    }

    public DownloadManager getDownloadManager()
    {
        return downloadManager;
    }

    public synchronized ArrayList<Integer> getRandomPendingPart(int max)
    {
        ArrayList<Integer> parts = new ArrayList<Integer>();

        for(int i=0;i<max;i++)
        {
            if (pendingParts.isEmpty()) break;

            Integer randomNumber = 0;
            if (!pendingParts.isEmpty())
                randomNumber = (new Random()).nextInt(pendingParts.size());

            Integer part = pendingParts.remove(randomNumber.intValue());
            downloadingParts.add(part);
            parts.add(part);
        }

        return parts;
    }

    public synchronized void saveDownloadedPartAndReturnIsCompleted(Integer part, Integer[] data) throws FileNotFoundException
    {
        try {
            Boolean setFileSize = false;
            if (!downloadPath.isFile())
                setFileSize = true;
            RandomAccessFile raf = new RandomAccessFile(downloadPath, "rw");
            if (setFileSize)
                raf.setLength(fileSize);

            int skipIndex = ((part-1)*Config.PART_LIMIT);
            byte[] bytes = new byte[data.length];
            for(int i=0;i<data.length;i++)
                bytes[i] = data[i].byteValue();

            /*
            System.out.println("--- START ---");
            System.out.println("Part : " + part);
            System.out.println("Seek : " + skipIndex);
            System.out.println("Size : " + fileSize);
            System.out.println("Data : " + bytes.length);
            System.out.println("--- END ---");
            * */

            raf.seek(skipIndex);
            raf.write(bytes);
            raf.close();
            downloadingParts.remove(part);
            downloadedParts.add(part);

            downloadManager.incrementDownloadedFilePartNumberOnTable(fileHash);
        } catch (IOException ex) {
            downloadingParts.remove(part);
            pendingParts.add(part);
        }

        if (pendingParts.isEmpty() && downloadingParts.isEmpty()
                && downloadedParts.size() == totalPart)
        {
            downloadManager.downloadComplete(fileHash);
        }
    }
}
