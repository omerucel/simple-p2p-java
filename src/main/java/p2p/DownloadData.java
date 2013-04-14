package p2p;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

public class DownloadData {
    private WindowClient windowClient;
    private int totalPart;
    private int fileSize;
    private File downloadPath;
    private ArrayList<Integer> pendingParts;
    private ArrayList<Integer> downloadingParts;
    private ArrayList<Integer> downloadedParts;

    public DownloadData(WindowClient windowClient, int fileSize, int totalPart, File downloadPath)
    {
        this.fileSize = fileSize;
        this.totalPart = totalPart;
        this.downloadPath = downloadPath;
        pendingParts = new ArrayList<Integer>();
        downloadedParts = new ArrayList<Integer>();
        downloadingParts = new ArrayList<Integer>();

        for(int i=0; i<totalPart;i++)
            pendingParts.add(i+1);
    }

    public synchronized Integer getRandomPendingPart()
    {
        Integer randomNumber = (new Random()).nextInt(pendingParts.size());
        Integer part = pendingParts.remove(randomNumber.intValue());
        downloadingParts.add(part);
        return part;
    }

    public synchronized Boolean isCompleted()
    {
        return pendingParts.isEmpty();
    }

    public synchronized Boolean saveDownloadedPartAndReturnIsCompleted(Integer part, ArrayList<Integer> data)
    {
        try {
            Boolean setFileSize = false;
            if (!downloadPath.isFile())
                setFileSize = true;
            RandomAccessFile raf = new RandomAccessFile(downloadPath, "rw");
            if (setFileSize)
            {
                System.out.println("Dosya boyutlandırılıyor");
                raf.setLength(fileSize);
            }

            raf.seek(part*512);
            for(Integer i : data)
                raf.writeInt(i);
            raf.close();
            downloadingParts.remove(part);
            downloadedParts.add(part);
        } catch (IOException ex) {
            downloadingParts.remove(part);
            pendingParts.add(part);
        }

        return pendingParts.isEmpty();
    }
}
