package edu.jhuapl.sbmt.client.users;

import java.util.LinkedHashSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class User
{
    private static final User UnauthorizedUser = User.of("", ImmutableList.of());

    public static User of(String id, Iterable<String> groupIds)
    {
        return new User(id, new LinkedHashSet<>(ImmutableList.copyOf(groupIds)));
    }

    public static User ofUnauthorized()
    {
        return UnauthorizedUser;
    }

    private final String id;
    private final LinkedHashSet<String> groupIds;

    protected User(String id, LinkedHashSet<String> groupIds)
    {
        this.id = Preconditions.checkNotNull(id);
        this.groupIds = groupIds;
    }

    public String getId()
    {
        return id;
    }

    public boolean isInGroup(String groupId)
    {
        return groupIds.contains(groupId);
    }

    public ImmutableList<String> getGroupIds()
    {
        return ImmutableList.copyOf(groupIds);
    }

    @Override
    public String toString()
    {
        return "User " + getId();
    }

}
