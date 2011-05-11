package edu.jhuapl.near.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Configuration;


/**
 * This class represents a database storing information about all the
 * data. It also provides functions for querying the database.
 *
 * @author kahneg1
 *
 */
abstract public class QueryBase
{
    public ArrayList<ArrayList<String>> doQuery(String phpScript, String data)
    {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

        try
        {
            URL u = new URL(Configuration.getQueryRootURL() + "/" + phpScript);
            URLConnection conn = u.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader in = new BufferedReader(isr);

            String line;

            while ((line = in.readLine()) != null)
            {
                line = line.trim();
                if (line.length() == 0)
                    continue;

                String[] tokens = line.split("\\s+");
                ArrayList<String> words = new ArrayList<String>();
                for (String word : tokens)
                    words.add(word);
                results.add(words);
            }

            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return results;
    }

    public String constructUrlArguments(HashMap<String, String> args)
    {
        String str = "";

        boolean firstKey = true;
        for (String key : args.keySet())
        {
            if (firstKey == true)
                firstKey = false;
            else
                str += "&";

            str += key + "=" + args.get(key);
        }

        return str;
    }
}
