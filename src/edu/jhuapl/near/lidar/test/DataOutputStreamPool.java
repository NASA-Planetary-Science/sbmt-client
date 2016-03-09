package edu.jhuapl.near.lidar.test;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.collect.Maps;

class DataOutputStreamPool {
    Map<Path,DataOutputStream> streamMap;

    int maxNumFiles;

    public DataOutputStreamPool(int maxNumFiles)
    {
        this.maxNumFiles=maxNumFiles;
        streamMap=Maps.newHashMapWithExpectedSize(maxNumFiles);
    }

    public DataOutputStream getStream(Path path) throws IOException {
        DataOutputStream stream=streamMap.get(path);
        return (stream==null)?newStream(path):stream;
    }

    DataOutputStream newStream(Path path) throws IOException {
        if (streamMap.size()==maxNumFiles)
            closeStream(streamMap.keySet().iterator().next());    // remove a random entry in the map
        DataOutputStream stream=new DataOutputStream(new FileOutputStream(path.toFile(),true));
        streamMap.put(path, stream);
        return stream;
    }

    public void closeStream(Path path) throws IOException {
        DataOutputStream stream=streamMap.get(path);
        if (stream!=null) {
            stream.close();
            streamMap.remove(path);
        }
    }

    public void closeAllStreams() throws IOException {
        while (!streamMap.isEmpty())
            closeStream(streamMap.keySet().iterator().next());  // close random entries until empty
    }

    public int getNumberOfStreams() {
        return streamMap.size();
    }

}