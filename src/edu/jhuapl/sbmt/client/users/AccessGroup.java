package edu.jhuapl.sbmt.client.users;

import java.util.LinkedHashSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.SafeURLPaths;

public final class AccessGroup
{
    public static AccessGroup of(String id, Iterable<String> filePaths)
    {
        Preconditions.checkNotNull(filePaths);

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String filePath : filePaths)
        {
            builder.add(SafeURLPaths.instance().getString(filePath));
        }

        return new AccessGroup(id, new LinkedHashSet<>(builder.build()));
    }

    private final String id;
    private final LinkedHashSet<String> filePaths;

    private AccessGroup(String id, LinkedHashSet<String> filePaths)
    {
        this.id = Preconditions.checkNotNull(id);
        this.filePaths = filePaths;
    }

    public String getId()
    {
        return id;
    }

    public ImmutableList<String> getAuthorizedFilePaths()
    {
        return ImmutableList.copyOf(filePaths);
    }

    @Override
    public String toString()
    {
        return "Group " + getId();
    }

}
